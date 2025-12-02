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
                System.out.println("[SERVER] RAW: " + message);

                String[] parts   = message.split(":", 4);
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

                this.userId = userId;

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
            stmt.setString(1, parts[1]);
            stmt.setString(2, parts[2]);
            stmt.setString(3, parts[3]);
            stmt.setString(4, parts[4]);
            stmt.setString(5, parts[5]);

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
        System.out.println("[DEBUG] SEND_CHAT parts.length = " + parts.length);
        for (int i = 0; i < parts.length; i++) {
            System.out.println("[DEBUG] parts[" + i + "] = " + parts[i]);
        }

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

                // broadcast dengan status is_read = false
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

        for (ClientHandler ch : clients) {
            if (ch.out != null) {
                ch.out.println(payload);
            }
        }
    }

    private void handleMarkRead(String[] parts) {
        if (parts.length < 2) {
            return;
        }

        try (Connection conn = DatabaseConnetion.getConnection()) {
            int senderId = Integer.parseInt(parts[1]);

            System.out.println("[SERVER] MARK_READ for senderId: " + senderId + " by userId: " + this.userId);

            // Update semua pesan dari sender yang belum dibaca
            String sql = "UPDATE chat_messages SET is_read = TRUE WHERE sender_id = ? AND is_read = FALSE";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, senderId);

            int updated = stmt.executeUpdate();
            System.out.println("[SERVER] Updated " + updated + " messages to read");
            
            if (updated > 0) {
                // Broadcast update ke semua client
                broadcastReadStatus(senderId);
            }

        } catch (SQLException | NumberFormatException e) {
            System.out.println("SQL ERROR di MARK_READ: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void handleMarkAllRead() {
        try (Connection conn = DatabaseConnetion.getConnection()) {
            System.out.println("[SERVER] MARK_ALL_READ by userId: " + this.userId);

            // Update semua pesan yang diterima oleh user ini (bukan yang dikirim)
            String sql = "UPDATE chat_messages SET is_read = TRUE WHERE sender_id != ? AND is_read = FALSE";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, this.userId);

            int updated = stmt.executeUpdate();
            System.out.println("[SERVER] Updated " + updated + " messages to read");
            
            if (updated > 0) {
                // Broadcast ke semua client bahwa pesan mereka sudah dibaca
                // Kita perlu tahu siapa saja yang pernah kirim pesan
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
            System.out.println("SQL ERROR di MARK_ALL_READ: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void broadcastReadStatus(int senderId) {
        String payload = "READ_STATUS:" + senderId;
        System.out.println("[SERVER] Broadcasting: " + payload);

        for (ClientHandler ch : clients) {
            if (ch.out != null) {
                ch.out.println(payload);
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