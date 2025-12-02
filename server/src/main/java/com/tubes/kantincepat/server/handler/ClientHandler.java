package com.tubes.kantincepat.server.handler;

import com.tubes.kantincepat.server.database.DatabaseConnetion;
import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClientHandler implements Runnable{
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    private static List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private int userId = 0;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        clients.add(this);
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {
                System.out.println("📩 Request: " + message);
                
                String[] parts;
                if (message.startsWith("CREATE_ORDER")) {
                    parts = message.split(":");
                } else if (message.contains(";")) {
                    parts = message.split(";");
                }else {
                    parts = message.split(":");
                }

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
                    case "GET_KITCHEN_ORDERS":
                        handleGetKitchenOrders();
                        break;
                    case "UPDATE_ORDER_STATUS":
                        handleUpdateOrderStatus(parts);
                        break;
                    case "GET_ORDER_HISTORY":
                        handleGetOrderHistory();
                        break;
                    case "SEND_CHAT":
                        handleSendChat(parts);
                        break;
                    case "GET_CHAT":
                        handleGetChat(parts);
                        break;
                    // ADMIN
                    case "ADD_MENU":
                        handleAddMenu(parts);
                        break;
                    case "ADD_USER":
                        handleAddUser(parts);
                        break;
                    case "GET_USERS":
                        handleGetUsers();
                        break;
                    case "GET_ALL_MENUS":
                        handleGetAllMenus();
                        break;
                    case "GET_ALL_ORDERS":
                        handleGetAllOrders();
                        break;
                    case "UPDATE_STATUS":
                        handleUpdateStatus(parts);
                        break;
                    case "DELETE_MENU":
                        handleDeleteMenu(parts);
                        break;
                    case "GET_CHAT_MESSAGES":
                        handleGetChatMessages(parts);
                        break;
                    case "CHAT_LOGIN":
                        handleChatLogin(parts);
                        break;
                    case "MARK_READ":
                        handleMarkRead(parts);
                        break;
                    case "MARK_ALL_READ":
                        handleMarkAllRead();
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
                     "string_agg(m.menu_id || ',' || m.name || ',' || m.price || ',' || od.quantity || ',' || COALESCE(m.image_path, 'null') || ',' || m.category, '#') as items_detail " +
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

    // private void handleSendChat(String[] parts) {
    //     // Format: SEND_CHAT:SenderID:Message
    //     // Receiver ID kita set NULL (artinya dikirim ke Admin/Store)
    //     if (parts.length < 3) return;

    //     int senderId = Integer.parseInt(parts[1]);
    //     String messageContent = parts[2];

    //     String sql = "INSERT INTO chat_messages (sender_id, receiver_id, message, sent_at) VALUES (?, NULL, ?, NOW())";

    //     try (Connection conn = DatabaseConnetion.getConnection();
    //          PreparedStatement stmt = conn.prepareStatement(sql)) {
            
    //         stmt.setInt(1, senderId);
    //         stmt.setString(2, messageContent);
            
    //         int rows = stmt.executeUpdate();
    //         if (rows > 0) out.println("CHAT_SUCCESS");
    //         else out.println("CHAT_FAILED");

    //     } catch (SQLException e) {
    //         e.printStackTrace();
    //         out.println("ERROR:DB Error");
    //     }
    // }

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

        //ADMIN
        private void handleAddMenu(String[] parts) {
            // Format: ADD_MENU;Nama;Deskripsi;Harga;Kategori;Status;Gambar
            if (parts.length < 6) { 
                out.println("ERROR:Data Menu Tidak Lengkap"); 
                return; 
            }
            try (Connection conn = DatabaseConnetion.getConnection()) {
                // CASTING KE ENUM POSTGRESQL (PENTING!)
                String sql = "INSERT INTO menu_items (name, description, price, category, is_available, image_path) VALUES (?, ?, ?, ?::menu_category, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                
                stmt.setString(1, parts[1]); // Nama
                stmt.setString(2, parts[2]); // Deskripsi
                
                // Harga (Parse Double)
                try {
                    String priceStr = parts[3].replaceAll("[^0-9]", ""); 
                    stmt.setInt(3, Integer.parseInt(priceStr));
                } catch (NumberFormatException e) {
                    out.println("ERROR:Harga harus angka");
                    return;
                }
    
                stmt.setString(4, parts[4]); // Kategori (MEALS, DRINK, SNACK)
                
                // Status (TRUE/FALSE)
                boolean isAvailable = parts[5].toUpperCase().contains("TRUE");
                stmt.setBoolean(5, isAvailable);
                
                // Gambar (Default jika kosong)
                String img = (parts.length > 6 && !parts[6].isEmpty()) ? parts[6] : "client\\src\\main\\resources\\assets\\no-image.png";
                stmt.setString(6, img);
    
                int rows = stmt.executeUpdate();
                if (rows > 0) out.println("SUCCESS");
                else out.println("FAILED:Gagal Insert");
    
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("FAILED:Database Error - " + e.getMessage());
            }
        }
    
        // --- FITUR BARU: ADD USER (DARI ADMIN) ---
        private void handleAddUser(String[] parts) {
            // Format: ADD_USER;Username;Pass;Email;Fullname;Phone;Role
            if (parts.length < 7) { out.println("ERROR:Data User Tidak Lengkap"); return; }
    
            try (Connection conn = DatabaseConnetion.getConnection()) {
                String sql = "INSERT INTO users (username, password, email, full_name, phone_number, role) VALUES (?, ?, ?, ?, ?, ?::user_role)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                
                stmt.setString(1, parts[1]); 
                stmt.setString(2, parts[2]); 
                stmt.setString(3, parts[3]); 
                stmt.setString(4, parts[4]); 
                stmt.setString(5, parts[5]); 
                stmt.setString(6, parts[6]); // Role (ADMIN, KITCHEN, CUSTOMER)
    
                int rows = stmt.executeUpdate();
                if (rows > 0) out.println("SUCCESS");
                else out.println("FAILED");
    
            } catch (SQLException e) {
                e.printStackTrace();
                out.println("FAILED:Username/Email Duplicate");
            }    
    }

    // --- AMBIL SEMUA DATA USER UNTUK TABEL ADMIN ---
    private void handleGetUsers() {
        StringBuilder sb = new StringBuilder();
        String sql = "SELECT user_id, username, email, full_name, phone_number, role FROM users ORDER BY user_id ASC";

        try (Connection conn = DatabaseConnetion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String fullname = rs.getString("full_name");
                if (fullname == null) fullname = "-";

                String phone = rs.getString("phone_number");
                if (phone == null) phone = "-";

                // Format: ID;Username;Email;Nama;HP;Role#
                sb.append(rs.getInt("user_id")).append(";")
                  .append(rs.getString("username")).append(";")
                  .append(rs.getString("email")).append(";")
                  .append(fullname).append(";")
                  .append(phone).append(";")
                  .append(rs.getString("role"))
                  .append("#"); // Pagar sebagai pemisah antar baris (row)
            }

            // Kirim response: LIST_USERS#data...
            if (sb.length() > 0) {
                out.println("LIST_USERS#" + sb.toString());
            } else {
                out.println("LIST_USERS#EMPTY");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            out.println("ERROR:Gagal mengambil data users");
        }
    }

    // --- METHOD BARU: AMBIL SEMUA MENU UNTUK ADMIN ---
    private void handleGetAllMenus() {
        StringBuilder sb = new StringBuilder();
        // Ambil semua data tanpa filter availability
        String sql = "SELECT menu_id, name, description, price, category, is_available, image_path FROM menu_items ORDER BY menu_id ASC";

        try (Connection conn = DatabaseConnetion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Handle null strings
                String desc = rs.getString("description");
                if (desc == null) desc = "-";

                String imgPath = rs.getString("image_path");
                if (imgPath == null) imgPath = "null"; // Kirim string "null" jika kosong

                // Format: ID;Nama;Deskripsi;Harga;Kategori;Available;PathGambar#
                sb.append(rs.getInt("menu_id")).append(";")
                  .append(rs.getString("name")).append(";")
                  .append(desc).append(";")
                  .append(rs.getInt("price")).append(";")
                  .append(rs.getString("category")).append(";")
                  .append(rs.getBoolean("is_available")).append(";") // true/false
                  .append(imgPath)
                  .append("#"); // Pemisah baris
            }

            if (sb.length() > 0) {
                out.println("LIST_MENUS#" + sb.toString());
            } else {
                out.println("LIST_MENUS#EMPTY");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            out.println("ERROR:Gagal mengambil data menu");
        }
    }
    // --- AMBIL SEMUA ORDER UNTUK ADMIN ---
    private void handleGetAllOrders() {
        // Query ini menggabungkan tabel orders, users, order_details, dan menu_items
        // Tujuannya: Menampilkan Siapa yang pesan, Apa yang dipesan (digabung jadi 1 string), Total, Status, dll.
        String sql = "SELECT o.order_id, u.full_name, o.order_date, o.total_amount, o.status, " +
                     "MAX(od.notes) as notes, " +
                     "string_agg(m.name || ' (x' || od.quantity || ')', ', ') as items_summary " +
                     "FROM orders o " +
                     "JOIN users u ON o.user_id = u.user_id " + // Join ke users untuk dapat nama
                     "JOIN order_details od ON o.order_id = od.order_id " +
                     "JOIN menu_items m ON od.menu_id = m.menu_id " +
                     "GROUP BY o.order_id, u.full_name " +
                     "ORDER BY o.order_date DESC";

        try (Connection conn = DatabaseConnetion.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            StringBuilder sb = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            while (rs.next()) {
                String notes = rs.getString("notes");
                if (notes == null) notes = "-";

                // Format: ID;Pelanggan;Menu;Catatan;Total;Status;Tanggal#
                sb.append(rs.getInt("order_id")).append(";")
                  .append(rs.getString("full_name")).append(";")
                  .append(rs.getString("items_summary")).append(";")
                  .append(notes).append(";")
                  .append(rs.getBigDecimal("total_amount").intValue()).append(";")
                  .append(rs.getString("status")).append(";")
                  .append(sdf.format(rs.getTimestamp("order_date")))
                  .append("#");
            }

            if (sb.length() > 0) {
                out.println("ALL_ORDERS#" + sb.toString());
            } else {
                out.println("ALL_ORDERS#EMPTY");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            out.println("ERROR:Gagal mengambil data order");
        }
    }

    private void handleUpdateStatus(String[] parts) {
        // Format: UPDATE_STATUS;OrderID;NewStatus
        if (parts.length < 3) return;

        int orderId = Integer.parseInt(parts[1]);
        String newStatus = parts[2];

        try (Connection conn = DatabaseConnetion.getConnection()) {
            String sql = "UPDATE orders SET status = ?::order_status WHERE order_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                out.println("UPDATE_SUCCESS");
                System.out.println("✅ Order ID " + orderId + " updated to " + newStatus);
            } else {
                out.println("UPDATE_FAILED");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("ERROR:Database Error");
        }
    }

    private void handleGetKitchenOrders() {
        try (Connection conn = DatabaseConnetion.getConnection()) {
            String sql = """
                    SELECT 
                        o.order_id,
                        u.full_name,
                        o.status,
                        COALESCE(
                            STRING_AGG(od.quantity::text || 'x ' || mi.name, ', ' ORDER BY mi.name),
                            ''
                        ) AS items
                    FROM orders o
                    JOIN users u ON u.user_id = o.user_id
                    LEFT JOIN order_details od ON od.order_id = o.order_id
                    LEFT JOIN menu_items mi ON mi.menu_id = od.menu_id
                    WHERE o.status IN ('PENDING', 'COOKING', 'READY')
                    GROUP BY o.order_id, u.full_name, o.status
                    ORDER BY o.order_date DESC;
                    """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            StringBuilder sb = new StringBuilder();
            boolean hasAny  = false;

            while (rs.next()) {
                hasAny = true;
                int orderId     = rs.getInt("order_id");
                String fullname = rs.getString("full_name");
                String status   = rs.getString("status");
                String items    = rs.getString("items");

                if (sb.length() == 0) sb.append("ORDERS:");
                else sb.append(";");

                sb.append(orderId)
                  .append("|").append(sanitize(fullname))
                  .append("|").append(status)
                  .append("|").append(items == null ? "" : sanitize(items));
            }

            if (!hasAny) out.println("NO_ORDERS");
            else out.println(sb.toString());

        } catch (SQLException e) {
            System.out.println("SQL ERROR di GET_KITCHEN_ORDERS: " + e.getMessage());
            e.printStackTrace();
            out.println("NO_ORDERS:DB_ERROR");
        }
    }

    private void handleUpdateOrderStatus(String[] parts) {
        if (parts.length < 3) {
            out.println("STATUS_FAILED:FORMAT_SALAH");
            return;
        }

        try (Connection conn = DatabaseConnetion.getConnection()) {
            int orderId    = Integer.parseInt(parts[1]);
            String newStatus = parts[2];

            String sql = "UPDATE orders SET status = ?::order_status WHERE order_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, newStatus);
            stmt.setInt(2, orderId);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                out.println("UPDATE_SUCCESS");
                System.out.println("✅ Order ID " + orderId + " updated to " + newStatus);
            } else {
                out.println("UPDATE_FAILED");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("ERROR:Database Error");
        }
    }

    private void handleDeleteMenu(String[] parts) {
        // Format: DELETE_MENU;MenuID
        if (parts.length < 2) { 
            out.println("FAILED:ID Missing"); 
            return; 
        }
    
        int id = Integer.parseInt(parts[1]);
    
        try (Connection conn = DatabaseConnetion.getConnection()) {
            String sql = "DELETE FROM menu_items WHERE menu_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
    
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                out.println("SUCCESS");
                System.out.println("✅ Menu Deleted ID: " + id);
            } else {
                out.println("FAILED:ID Not Found");
            }
    
        } catch (SQLException e) {
            e.printStackTrace();
            // Jika gagal hapus karena Foreign Key (menu pernah dipesan), beri pesan error
            out.println("FAILED:Database Error (Mungkin menu sedang digunakan di order)");
        }
    }

    private void handleGetOrderHistory() {
        try (Connection conn = DatabaseConnetion.getConnection()) {
            String sql = """
                    SELECT 
                        o.order_id,
                        u.full_name,
                        o.status,
                        COALESCE(
                            STRING_AGG(od.quantity::text || 'x ' || mi.name, ', ' ORDER BY mi.name),
                            ''
                        ) AS items
                    FROM orders o
                    JOIN users u ON u.user_id = o.user_id
                    LEFT JOIN order_details od ON od.order_id = o.order_id
                    LEFT JOIN menu_items mi ON mi.menu_id = od.menu_id
                    WHERE o.status = 'COMPLETED'
                    GROUP BY o.order_id, u.full_name, o.status
                    ORDER BY o.order_date DESC;
                    """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            StringBuilder sb = new StringBuilder();
            boolean hasAny  = false;

            while (rs.next()) {
                hasAny = true;
                int orderId     = rs.getInt("order_id");
                String fullname = rs.getString("full_name");
                String status   = rs.getString("status");
                String items    = rs.getString("items");

                if (sb.length() == 0) sb.append("ORDERS:");
                else sb.append(";");

                sb.append(orderId)
                  .append("|").append(sanitize(fullname))
                  .append("|").append(status)
                  .append("|").append(items == null ? "" : sanitize(items));
            }

            if (!hasAny) out.println("NO_ORDERS");
            else out.println(sb.toString());

        } catch (SQLException e) {
            System.out.println("SQL ERROR di GET_ORDER_HISTORY: " + e.getMessage());
            e.printStackTrace();
            out.println("NO_ORDERS:DB_ERROR");
        }
    }

    // ================== LIVE CHAT ==================

    private void handleSendChat(String[] parts) {
        if (this.userId <= 0) {
            out.println("CHAT_FAILED:NOT_LOGGED_IN");
            return;
        }
        if (parts.length < 2) {
            out.println("CHAT_FAILED:FORMAT_SALAH");
            return;
        }

        String message = parts[1];

        try (Connection conn = DatabaseConnetion.getConnection()) {
            String sql = "INSERT INTO chat_messages (sender_id, message, is_read) VALUES (?, ?, FALSE)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, this.userId);
            stmt.setString(2, message);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                out.println("CHAT_SENT");
                broadcastChatSimple(this.userId, message, false);
            } else {
                out.println("CHAT_FAILED");
            }
        } catch (SQLException e) {
            System.out.println("SQL ERROR di SEND_CHAT: " + e.getMessage());
            e.printStackTrace();
            out.println("CHAT_FAILED:DB_ERROR");
        }
    }

    private static void broadcastChatSimple(int senderId, String message, boolean isRead) {
        String sanitized = message.replace(":", " ");
        String payload = "CHAT_MESSAGE:" + senderId + ":" + sanitized + ":" + isRead;

        synchronized (clients) {
            for (ClientHandler ch : clients) {
                if (ch.out != null) {
                    ch.out.println(payload);
                }
            }
        }
    }

    private void handleMarkRead(String[] parts) {
        if (parts.length < 2) return;

        try (Connection conn = DatabaseConnetion.getConnection()) {
            int senderId = Integer.parseInt(parts[1]);
            String sql = "UPDATE chat_messages SET is_read = TRUE WHERE sender_id = ? AND is_read = FALSE";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, senderId);

            int updated = stmt.executeUpdate();
            if (updated > 0) {
                broadcastReadStatus(senderId);
            }
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
        }
    }
    
    private void handleMarkAllRead() {
        try (Connection conn = DatabaseConnetion.getConnection()) {
            String sql = "UPDATE chat_messages SET is_read = TRUE WHERE sender_id != ? AND is_read = FALSE";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, this.userId);

            int updated = stmt.executeUpdate();
            if (updated > 0) {
                String sqlSenders = "SELECT DISTINCT sender_id FROM chat_messages WHERE sender_id != ?";
                PreparedStatement stmtSenders = conn.prepareStatement(sqlSenders);
                stmtSenders.setInt(1, this.userId);
                ResultSet rs = stmtSenders.executeQuery();
                while (rs.next()) {
                    int senderId = rs.getInt("sender_id");
                    broadcastReadStatus(senderId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void broadcastReadStatus(int senderId) {
        String payload = "READ_STATUS:" + senderId;
        synchronized (clients) {
            for (ClientHandler ch : clients) {
                if (ch.out != null) {
                    ch.out.println(payload);
                }
            }
        }
    }

    private void handleGetChatMessages(String[] parts) {
        if (parts.length < 3) {
            out.println("NO_MESSAGES");
            return;
        }

        try (Connection conn = DatabaseConnetion.getConnection()) {
            int userId = Integer.parseInt(parts[1]);
            int adminId = Integer.parseInt(parts[2]);

            String sql = """
                    SELECT sender_id, message, sent_at, is_read
                    FROM chat_messages
                    WHERE (sender_id = ? AND receiver_id = ?)
                       OR (sender_id = ? AND receiver_id = ?)
                    ORDER BY sent_at ASC
                    """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, adminId);
            stmt.setInt(3, adminId);
            stmt.setInt(4, userId);

            ResultSet rs = stmt.executeQuery();

            StringBuilder sb = new StringBuilder();
            boolean hasAny = false;

            while (rs.next()) {
                hasAny = true;
                int senderId = rs.getInt("sender_id");
                String message = rs.getString("message");
                String timestamp = rs.getString("sent_at");
                boolean isRead = rs.getBoolean("is_read");

                if (sb.length() == 0) sb.append("MESSAGES:");
                else sb.append(";");

                sb.append(senderId)
                  .append("|").append(sanitize(message))
                  .append("|").append(timestamp)
                  .append("|").append(isRead);
            }

            if (!hasAny) out.println("NO_MESSAGES");
            else out.println(sb.toString());

        } catch (SQLException | NumberFormatException e) {
            System.out.println("SQL ERROR di GET_CHAT_MESSAGES: " + e.getMessage());
            e.printStackTrace();
            out.println("NO_MESSAGES:DB_ERROR");
        }
    }

    private void handleChatLogin(String[] parts) {
        if (parts.length < 2) {
            out.println("CHAT_LOGIN_FAILED");
            return;
        }
        try {
            this.userId = Integer.parseInt(parts[1]);
            out.println("CHAT_LOGIN_OK");
        } catch (NumberFormatException e) {
            out.println("CHAT_LOGIN_FAILED");
        }
    }

    private String sanitize(String s) {
        if (s == null) return "";
        return s.replace("|", "-").replace(";", "-");
    }
}
