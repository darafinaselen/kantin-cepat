package com.tubes.kantincepat.server.handler;

import com.tubes.kantincepat.server.database.DatabaseConnetion;
import java.io.*;
import java.net.Socket;
import java.sql.*;

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
                    case "GET_USER":
                        handleGetUser(parts);
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
                String role = rs.getString("role"); 
                String fullname = rs.getString("full_name");
                out.println("LOGIN_SUCCESS:" + role + ":" + fullname);
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

        int userId = Integer.parseInt(parts[1]);
        long totalAmount = Long.parseLong(parts[2]);
        String notes = parts[3];
        String itemsData = parts[4];

        String sqlHeader = "INSERT INTO orders (user_id, total_amount, status, notes, order_date) VALUES (?, ?, 'PENDING', ?, NOW())";
        String sqlDetail = "INSERT INTO order_details (order_id, menu_id, quantity, subtotal) VALUES (?, ?, ?, ?)";
        String sqlPrice  = "SELECT price FROM menu_items WHERE menu_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnetion.getConnection();
            conn.setAutoCommit(false); 

            // 1. INSERT HEADER (Tabel orders)
            PreparedStatement psHeader = conn.prepareStatement(sqlHeader, Statement.RETURN_GENERATED_KEYS);
            psHeader.setInt(1, userId);
            psHeader.setInt(2, (int) totalAmount);
            psHeader.setString(3, notes);
            
            psHeader.executeUpdate();

            // Ambil ID Order yang baru dibuat
            ResultSet rsKeys = psHeader.getGeneratedKeys();
            int newOrderId = -1;
            if (rsKeys.next()) {
                newOrderId = rsKeys.getInt(1);
            } else {
                throw new SQLException("Gagal ID");
            }

            // 2. INSERT DETAILS (Looping items)
            String[] itemPairs = itemsData.split(";");
            PreparedStatement psDetail = conn.prepareStatement(sqlDetail);
            PreparedStatement psPrice = conn.prepareStatement(sqlPrice);

            for (String pair : itemPairs) {
                String[] val = pair.split(","); // val[0]=MenuID, val[1]=Qty
                int menuId = Integer.parseInt(val[0]);
                int qty = Integer.parseInt(val[1]);

                // Ambil harga asli dari DB (Biar aman, jangan percaya harga kiriman client)
                psPrice.setInt(1, menuId);
                ResultSet rsPrice = psPrice.executeQuery();
                int price = 0;
                if (rsPrice.next()) price = rsPrice.getInt("price");
                rsPrice.close();

                // Simpan Detail
                psDetail.setInt(1, newOrderId);
                psDetail.setInt(2, menuId);
                psDetail.setInt(3, qty);
                psDetail.setInt(4, price * qty); // Subtotal
                psDetail.addBatch();
            }

            psDetail.executeBatch();
            
            // 3. SELESAI
            conn.commit(); // Simpan permanen
            System.out.println("✅ Order Created ID: " + newOrderId);
            out.println("ORDER_SUCCESS:" + newOrderId);

        } catch (Exception e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {} // Batalkan jika error
            out.println("ORDER_FAILED:Database Error");
        } finally {
             try { if (conn != null) conn.setAutoCommit(true); conn.close(); } catch (SQLException ex) {}
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
                // Kirim balik data lengkap
                // Format: USER_DATA:ID:Username:Email:Fullname:Phone:Role
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
}
