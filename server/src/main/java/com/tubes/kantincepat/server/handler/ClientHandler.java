package com.tubes.kantincepat.server.handler;

import com.tubes.kantincepat.server.database.DatabaseConnetion;
import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.text.SimpleDateFormat;

public class ClientHandler implements Runnable{
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("📩 Request: " + message);
                
                String[] parts = message.split(":");
                String command = parts[0];

                switch (command) {
                    case "LOGIN":
                        handleLogin(parts);
                        break;
                    case "REGISTER":
                        handleRegister(parts);
                        break;
                    case "GET_MENU":
                        handleGetMenu();
                        break;
                    case "CREATE_ORDER":
                        handleCreateOrder(parts); 
                        break;
                    case "GET_HISTORY":
                        handleGetHistory(parts);
                        break;
                    case "GET_USER":
                        handleGetUser(parts);
                        break;
                    case "SEND_CHAT":
                        handleSendChat(parts);
                        break;
                    case "GET_CHAT":
                        handleGetChat(parts);
                        break;
                    default:
                        out.println("ERROR:Unknown Command");
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Client disconnected");
        }
    }

    private void handleLogin(String[] parts) {
        // Format: LOGIN:username:password
        if (parts.length < 3) { out.println("LOGIN_FAILED:Invalid Format"); return; }
        
        String inputIdentitas = parts[1];
        String password = parts[2];

        try (Connection conn = DatabaseConnetion.getConnection()) {
            String sql = "SELECT * FROM users WHERE (username = ? OR email = ?) AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, inputIdentitas);
            stmt.setString(2, inputIdentitas);
            stmt.setString(3, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("user_id");
                String role = rs.getString("role");
                String fullname = rs.getString("full_name");
                String username = rs.getString("username");
                String email = rs.getString("email");
                String phone = rs.getString("phone_number");

                if (phone == null) phone = "-";

                out.println("LOGIN_SUCCESS:" + role + ":" + fullname + ":" + id + ":" + username + ":" + email + ":" + phone);
                System.out.println("✅ User Logged In: " + username);
            } else {
                out.println("LOGIN_FAILED:Wrong credentials");
            }
        }catch (SQLException e) {
            e.printStackTrace();
            out.println("LOGIN_ERROR:Database Error");
        }
    }

    private void handleRegister(String[] parts) {
        if (parts.length < 6) { out.println("REGISTER_FAILED:Format Salah"); return; }

        try (Connection conn = DatabaseConnetion.getConnection()) {
            String sql = "INSERT INTO users (username, email, password, full_name, phone_number, role) VALUES (?, ?, ?, ?, ?, 'CUSTOMER')";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, parts[1]); // username
            stmt.setString(2, parts[2]); // email
            stmt.setString(3, parts[3]); // password
            stmt.setString(4, parts[4]); // full_name
            stmt.setString(5, parts[5]); // phone_number

            int rows = stmt.executeUpdate();
            if (rows > 0) out.println("REGISTER_SUCCESS");
            else out.println("REGISTER_FAILED");
            
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("REGISTER_FAILED:Username mungkin sudah dipakai");
        }
    }

    private void handleGetMenu() {
        // Query ambil data (Filter yang is_available = true jika mau menu yg ready saja)
        String sql = "SELECT menu_id, name, price, category, image_path, description FROM menu_items WHERE is_available = true ORDER BY menu_id ASC";

        try (Connection conn = DatabaseConnetion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            StringBuilder sb = new StringBuilder();

            while (rs.next()) {
                // 1. ID
                int id = rs.getInt("menu_id");
                
                // 2. Nama
                String name = rs.getString("name");
                
                // 3. Harga (Di DB numeric, kita ambil int-nya saja biar gampang di client)
                int price = rs.getInt("price");
                
                // 4. Kategori (DB type menu_category dibaca sebagai String aman)
                String category = rs.getString("category");
                
                // 5. Gambar (PENTING: Handle null dan path folder)
                String rawImg = rs.getString("image_path");
                String finalImg = "null";
                
                if (rawImg != null && !rawImg.isEmpty()) {
                    // Kalau di DB tertulis "assets/nasi.png", kita ambil "nasi.png" saja
                    // Supaya GUIUtils tidak bingung (karena dia sudah nambahin /assets/ sendiri)
                    if (rawImg.contains("/")) {
                        finalImg = rawImg.substring(rawImg.lastIndexOf("/") + 1);
                    } else {
                        finalImg = rawImg;
                    }
                }

                // 6. Deskripsi
                String desc = rs.getString("description");
                if (desc == null) desc = "-";

                // GABUNGKAN DATA (Pemisah antar field "|", antar baris ";")
                sb.append(id).append("|")
                  .append(name).append("|")
                  .append(price).append("|")
                  .append(category).append("|")
                  .append(finalImg).append("|") // Gambar yang sudah bersih
                  .append(desc)
                  .append(";");
            }

            // Kirim ke Client
            if (sb.length() > 0) {
                out.println("MENU_DATA:" + sb.toString());
                System.out.println("✅ Menu data sent (" + sb.length() + " chars)");
            } else {
                out.println("MENU_DATA:"); // Kirim kosong jika tidak ada menu
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("❌ Error fetching menu: " + e.getMessage());
            out.println("ERROR:Database Error");
        }
    }

    private void handleCreateOrder(String[] parts) {
        // Format: CREATE_ORDER:UserID:Total:Notes:ItemsData
        // parts[0]=CMD, parts[1]=UserID, parts[2]=Total, parts[3]=Notes, parts[4]=Items
        
        if (parts.length < 5) {
            out.println("ORDER_FAILED:Data Incomplete");
            return;
        }
        try {
            int userId = Integer.parseInt(parts[1]);
            long totalAmount = Long.parseLong(parts[2]);
            String notes = parts[3];
            String itemsData = parts[4];

            Connection conn = DatabaseConnetion.getConnection();
            conn.setAutoCommit(false);

            // 1. INSERT HEADER
            String sqlHeader = "INSERT INTO orders (user_id, total_amount, status, order_date) VALUES (?, ?, 'PENDING', NOW())";
            PreparedStatement psHeader = conn.prepareStatement(sqlHeader, Statement.RETURN_GENERATED_KEYS);
            psHeader.setInt(1, userId);
            psHeader.setBigDecimal(2, java.math.BigDecimal.valueOf(totalAmount));
            psHeader.executeUpdate();

            ResultSet rsKeys = psHeader.getGeneratedKeys();
            int newOrderId = -1;
            if (rsKeys.next()) {
                newOrderId = rsKeys.getInt(1);
            } else {
                throw new SQLException("Gagal ID");
            }


      // 2. INSERT DETAILS
            String sqlDetail = "INSERT INTO order_details (order_id, menu_id, quantity, subtotal, notes) VALUES (?, ?, ?, ?, ?)";
            String sqlPrice  = "SELECT price FROM menu_items WHERE menu_id = ?";
            
            PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
            PreparedStatement psPrice = conn.prepareStatement(sqlPrice);

            String[] itemPairs = itemsData.split(";");
            for (String pair : itemPairs) {
                String[] val = pair.split(","); 
                int menuId = Integer.parseInt(val[0]);
                int qty = Integer.parseInt(val[1]);

                psPrice.setInt(1, menuId);
                ResultSet rsPrice = psPrice.executeQuery();
                int price = 0;
                if (rsPrice.next()) price = rsPrice.getInt("price");
                rsPrice.close();

                psDetail.setInt(1, newOrderId);
                psDetail.setInt(2, menuId);
                psDetail.setInt(3, qty);
                psDetail.setBigDecimal(4, java.math.BigDecimal.valueOf((long)price * qty));
                psDetail.setString(5, notes); 
                
                psDetail.addBatch();
            }

            psDetail.executeBatch();
            conn.commit();
            System.out.println("✅ Order Created ID: " + newOrderId);
            out.println("ORDER_SUCCESS:" + newOrderId);

        } catch (Exception e) {
            e.printStackTrace();
            out.println("ORDER_FAILED:Database Error");
        }
    }

    private void handleGetHistory(String[] parts) {
        if (parts.length < 2) return;
        int userId = Integer.parseInt(parts[1]);

        String sql = "SELECT o.order_id, o.order_date, o.total_amount, o.status, " +
                     "MAX(od.notes) as notes, " +  
                     "string_agg(m.name || ' (x' || od.quantity || ')', ', ') as summary, " +
                     "string_agg(m.menu_id || ',' || m.name || ',' || m.price || ',' || od.quantity, '#') as items_detail " + 
                     "FROM orders o " +
                     "JOIN order_details od ON o.order_id = od.order_id " +
                     "JOIN menu_items m ON od.menu_id = m.menu_id " +
                     "WHERE o.user_id = ? " +
                     "GROUP BY o.order_id " +
                     "ORDER BY o.order_date DESC";

        try (Connection conn = DatabaseConnetion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm");

            while (rs.next()) {
                int id = rs.getInt("order_id");
                String date = sdf.format(rs.getTimestamp("order_date"));
                int total = rs.getBigDecimal("total_amount").intValue();
                String status = rs.getString("status");
                String summary = rs.getString("summary");

                String notes = rs.getString("notes");
                if (notes == null) notes = "-";

                String itemsDetail = rs.getString("items_detail");

                sb.append(id).append("|")
                  .append(date).append("|")
                  .append(total).append("|")
                  .append(status).append("|")
                  .append(summary).append("|")     
                  .append(notes).append("|")       
                  .append(itemsDetail).append(";");
            }
            out.println("HISTORY_DATA:" + sb.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            out.println("ERROR:History DB Error");
        }
    }

    private void handleGetUser(String[] parts) {
        // Format: GET_USER:UserID
        if (parts.length < 2) return;
        
        int userId = Integer.parseInt(parts[1]);
        String sql = "SELECT * FROM users WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnetion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                // Format: USER_DATA:ID:Username:Email:Fullname:Phone:Role
                String fullname = rs.getString("full_name");
                if (fullname == null) fullname = "-";
                
                String phone = rs.getString("phone_number");
                if (phone == null) phone = "-";

                String resp = "USER_DATA:" + 
                              rs.getInt("user_id") + ":" +
                              rs.getString("username") + ":" +
                              rs.getString("email") + ":" +
                              rs.getString("full_name") + ":" +
                              rs.getString("phone_number") + ":" +
                              rs.getString("role");
                out.println(resp);
            } else {
                out.println("ERROR:User Not Found");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("ERROR:DB Error");
        }
    }

    private void handleSendChat(String[] parts) {
        // Format: SEND_CHAT:SenderID:Message
        // Receiver ID kita set NULL (artinya dikirim ke Admin/Store)
        if (parts.length < 3) return;

        int senderId = Integer.parseInt(parts[1]);
        String messageContent = parts[2];

        String sql = "INSERT INTO chat_messages (sender_id, receiver_id, message, sent_at) VALUES (?, NULL, ?, NOW())";

        try (Connection conn = DatabaseConnetion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, senderId);
            stmt.setString(2, messageContent);
            
            int rows = stmt.executeUpdate();
            if (rows > 0) out.println("CHAT_SUCCESS");
            else out.println("CHAT_FAILED");

        } catch (SQLException e) {
            e.printStackTrace();
            out.println("ERROR:DB Error");
        }
    }

    private void handleGetChat(String[] parts) {
        // Format: GET_CHAT:UserID
        if (parts.length < 2) return;
        int userId = Integer.parseInt(parts[1]);

        // Ambil pesan dimana User adalah PENGIRIM atau User adalah PENERIMA
        String sql = "SELECT * FROM chat_messages WHERE sender_id = ? OR receiver_id = ? ORDER BY sent_at ASC";

        try (Connection conn = DatabaseConnetion.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            
            StringBuilder sb = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

            while (rs.next()) {
                // Format Respon: ID|SenderID|Message|Time;
                sb.append(rs.getInt("chat_id")).append("|")
                  .append(rs.getInt("sender_id")).append("|")
                  .append(rs.getString("message")).append("|")
                  .append(sdf.format(rs.getTimestamp("sent_at"))).append(";");
            }
            out.println("CHAT_HISTORY:" + sb.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            out.println("ERROR:DB Error");
        }
    }
}
