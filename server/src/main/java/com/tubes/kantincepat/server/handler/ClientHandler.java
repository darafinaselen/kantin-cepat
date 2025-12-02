package com.tubes.kantincepat.server.handler;

import com.tubes.kantincepat.server.database.DatabaseConnetion;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class ClientHandler implements Runnable {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private int userId = -1;

    private static final Set<ClientHandler> clients =
        ConcurrentHashMap.newKeySet();

    public ClientHandler(Socket socket) {
        this.socket = socket;
        clients.add(this);
    }

    @Override
    public void run() {
        try {
            in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {
                message = message.trim();
                if (message.isEmpty()) continue;

                System.out.println("📩 Request: " + message);

                System.out.println("[SERVER] RAW: " + message); // <--- TAMBAHKAN INI

                String[] parts   = message.split(":", 4); // Ubah jadi 4 biar bisa baca message yang ada ":"
                String command   = parts[0];

                switch (command) {
                    case "LOGIN":
                        handleLogin(parts);
                        break;
                    case "REGISTER":
                        handleRegister(parts);
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
                    case "GET_CHAT_MESSAGES":
                        handleGetChatMessages(parts);
                        break;
                    case "CHAT_LOGIN":
                        handleChatLogin(parts);
                        break;
                    default:
                        out.println("ERROR:Unknown Command");
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected");
        } finally {
            clients.remove(this);
            try {
                if (socket != null) socket.close();
            } catch (IOException ignored) {}
        }
    }

    // ================== AUTH ==================

    private void handleLogin(String[] parts) {
        if (parts.length < 3) {
            out.println("LOGIN_FAILED:Invalid Format");
            return;
        }

        String inputIdentitas = parts[1];
        String password       = parts[2];

        try (Connection conn = DatabaseConnetion.getConnection()) {
            String sql = "SELECT * FROM users WHERE (username = ? OR email = ?) AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, inputIdentitas);
            stmt.setString(2, inputIdentitas);
            stmt.setString(3, password);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String role     = rs.getString("role");
                String fullname = rs.getString("full_name");
                int userId      = rs.getInt("user_id");

                this.userId = userId; // simpan biar handler tahu ini user siapa (buat chat)

                // FORMAT BARU:
                // LOGIN_SUCCESS:ROLE:FULLNAME:USERID
                out.println("LOGIN_SUCCESS:" + role + ":" + fullname + ":" + userId);
            } else {
                out.println("LOGIN_FAILED:Wrong credentials");
            }


        } catch (SQLException e) {
            e.printStackTrace();
            out.println("LOGIN_ERROR:Database Error");
        }
    }

    private void handleRegister(String[] parts) {
        if (parts.length < 6) {
            out.println("REGISTER_FAILED:Format Salah");
            return;
        }

        try (Connection conn = DatabaseConnetion.getConnection()) {
            String sql = "INSERT INTO users (username, email, password, full_name, phone_number, role) " +
                         "VALUES (?, ?, ?, ?, ?, 'CUSTOMER')";
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

    // ================== KITCHEN ORDERS ==================

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
                out.println("STATUS_UPDATED");
            } else {
                out.println("STATUS_FAILED:NOT_FOUND");
            }

        } catch (SQLException | NumberFormatException e) {
            System.out.println("SQL ERROR di UPDATE_ORDER_STATUS: " + e.getMessage());
            e.printStackTrace();
            out.println("STATUS_FAILED:DB_ERROR");
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
        // Format baru: SEND_CHAT:message

        // Debug biar kelihatan di console server
        System.out.println("[DEBUG] SEND_CHAT parts.length = " + parts.length);
        for (int i = 0; i < parts.length; i++) {
            System.out.println("[DEBUG] parts[" + i + "] = " + parts[i]);
        }

        // Pastikan user sudah punya userId
        if (this.userId <= 0) {
            out.println("CHAT_FAILED:NOT_LOGGED_IN");
            return;
        }

        if (parts.length < 2) {
            out.println("CHAT_FAILED:FORMAT_SALAH");
            return;
        }

        // message di index 1
        String message = parts[1];

        try (Connection conn = DatabaseConnetion.getConnection()) {
            String sql = "INSERT INTO chat_messages (sender_id, message) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, this.userId);
            stmt.setString(2, message);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                out.println("CHAT_SENT");

                // kirim ke semua client yang sedang connect
                broadcastChatSimple(this.userId, message);
            } else {
                out.println("CHAT_FAILED");
            }

        } catch (SQLException e) {
            System.out.println("SQL ERROR di SEND_CHAT: " + e.getMessage());
            e.printStackTrace();
            out.println("CHAT_FAILED:DB_ERROR");
        }
    }

    private static void broadcastChatSimple(int senderId, String message) {
        // jaga protokol, jangan ada ":" diisi liar
        String sanitized = message.replace(":", " ");
        String payload = "CHAT_MESSAGE:" + senderId + ":" + sanitized;

        for (ClientHandler ch : clients) {
            if (ch.out != null) {
                ch.out.println(payload);
            }
        }
    }

    // GET_CHAT_MESSAGES:userId:adminId
    private void handleGetChatMessages(String[] parts) {
        if (parts.length < 3) {
            out.println("NO_MESSAGES");
            return;
        }

        try (Connection conn = DatabaseConnetion.getConnection()) {
            int userId = Integer.parseInt(parts[1]);
            int adminId = Integer.parseInt(parts[2]);

            // Ambil percakapan antara user dan admin (dua arah)
            String sql = """
                    SELECT sender_id, message, sent_at
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

                if (sb.length() == 0) sb.append("MESSAGES:");
                else sb.append(";");

                sb.append(senderId)
                  .append("|").append(sanitize(message))
                  .append("|").append(timestamp);
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