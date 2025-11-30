package com.tubes.kantincepat.client.view;

import java.sql.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class OrderServices {

    public static int saveOrder(int userId, long totalAmount, String globalNote, List<MenuItem> cartItems) {
        int generatedOrderId = -1;
        
        // 1. Insert Header ke tabel 'orders'
        String insertOrderSQL = "INSERT INTO orders (user_id, total_amount, status) VALUES (?, ?, ?::order_status)";
        
        // 2. Insert Detail ke tabel 'order_details'
        String insertDetailSQL = "INSERT INTO order_details (order_id, menu_id, quantity, subtotal, notes) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = koneksiDB.getConnection()) {
            conn.setAutoCommit(false); // Transaksi ON

            // --- EKSEKUSI HEADER ---
            PreparedStatement psOrder = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS);
            psOrder.setInt(1, userId);
            psOrder.setBigDecimal(2, java.math.BigDecimal.valueOf(totalAmount));
            psOrder.setString(3, "PENDING"); // Default status enum
            psOrder.executeUpdate();

            ResultSet rs = psOrder.getGeneratedKeys();
            if (rs.next()) {
                generatedOrderId = rs.getInt(1);
            } else {
                throw new SQLException("Gagal mendapatkan Order ID.");
            }

            // --- EKSEKUSI DETAIL ---
            // Grouping item
            Map<Integer, Integer> qtyMap = new HashMap<>();
            Map<Integer, Integer> priceMap = new HashMap<>();

            for (MenuItem item : cartItems) {
                qtyMap.put(item.id, qtyMap.getOrDefault(item.id, 0) + 1);
                priceMap.put(item.id, item.rawPrice);
            }

            PreparedStatement psDetail = conn.prepareStatement(insertDetailSQL);
            for (Map.Entry<Integer, Integer> entry : qtyMap.entrySet()) {
                int menuId = entry.getKey();
                int qty = entry.getValue();
                int price = priceMap.get(menuId);
                long subtotal = (long) price * qty; // Hitung subtotal

                psDetail.setInt(1, generatedOrderId);
                psDetail.setInt(2, menuId);
                psDetail.setInt(3, qty);
                psDetail.setBigDecimal(4, java.math.BigDecimal.valueOf(subtotal));
                psDetail.setString(5, globalNote); // Simpan note di setiap item
                
                psDetail.addBatch();
            }
            psDetail.executeBatch();

            conn.commit(); // Simpan permanen

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        return generatedOrderId;
    }

    public static List<Order> getOrdersByCustomer(int userId) {
        List<Order> orderList = new ArrayList<>();
        
        // Ambil Header
        String sqlOrder = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC";
        
        // Ambil Detail (Join ke menu_items)
        String sqlDetail = "SELECT od.quantity, od.notes, m.menu_id, m.name, m.price, m.image_path, m.category " +
                           "FROM order_details od " +
                           "JOIN menu_items m ON od.menu_id = m.menu_id " +
                           "WHERE od.order_id = ?";

        try (Connection conn = koneksiDB.getConnection();
             PreparedStatement psOrder = conn.prepareStatement(sqlOrder)) {

            psOrder.setInt(1, userId);
            ResultSet rsOrder = psOrder.executeQuery();

            while (rsOrder.next()) {
                int orderId = rsOrder.getInt("order_id");
                int totalAmount = rsOrder.getBigDecimal("total_amount").intValue();
                String status = rsOrder.getString("status");
                Timestamp timestamp = rsOrder.getTimestamp("order_date"); // Kolom baru
                
                SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm");
                String dateStr = (timestamp != null) ? sdf.format(timestamp) : "-";
                
                NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                String priceStr = nf.format(totalAmount).replace(",00", "");

                List<MenuItem> items = new ArrayList<>();
                StringBuilder summaryBuilder = new StringBuilder();
                String retrievedNote = "-"; // Default note
                
                try (PreparedStatement psDetail = conn.prepareStatement(sqlDetail)) {
                    psDetail.setInt(1, orderId);
                    ResultSet rsDetail = psDetail.executeQuery();
                    
                    int count = 0;
                    while (rsDetail.next()) {
                        int realMenuId = rsDetail.getInt("menu_id");
                        String menuName = rsDetail.getString("name");
                        int menuPrice = rsDetail.getBigDecimal("price").intValue();
                        int qty = rsDetail.getInt("quantity");
                        String imgPath = rsDetail.getString("image_path");
                        String catName = rsDetail.getString("category");
                        
                        // Ambil note dari salah satu item (karena kita simpan sama semua)
                        if (count == 0) retrievedNote = rsDetail.getString("notes");

                        // Reconstruct untuk Re-order
                        for(int k=0; k<qty; k++) {
                            items.add(new MenuItem(realMenuId, menuName, "", menuPrice, catName, imgPath, true));
                        }

                        if (count > 0) summaryBuilder.append(", ");
                        summaryBuilder.append(menuName).append(" (x").append(qty).append(")");
                        count++;
                    }
                }
                
                Order order = new Order(dateStr, summaryBuilder.toString(), priceStr, status, retrievedNote, items);
                order.orderId = String.valueOf(orderId);
                
                orderList.add(order);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orderList;
    }
}
