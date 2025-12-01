package com.tubes.kantincepat.client.view;

import com.tubes.kantincepat.client.net.ClientSocket;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OrderServices {

    public static int saveOrder(int userId, long totalAmount, String globalNote, List<MenuItem> cartItems) {
        StringBuilder itemsStr = new StringBuilder();

        // Grouping dulu (supaya kalau ada 2 Nasi Goreng jadi satu entry x2)
        Map<Integer, Integer> qtyMap = new HashMap<>();
        for (MenuItem item : cartItems) {
            qtyMap.put(item.id, qtyMap.getOrDefault(item.id, 0) + 1);
        }

        int count = 0;
        for (Map.Entry<Integer, Integer> entry : qtyMap.entrySet()) {
            if (count > 0) itemsStr.append(";"); // Pemisah antar menu
            itemsStr.append(entry.getKey()).append(",").append(entry.getValue());
            count++;
        }

        String payload = "CREATE_ORDER:" + userId + ":" + totalAmount + ":" + globalNote + ":" + itemsStr.toString();
        String response = ClientSocket.getInstance().sendRequest(payload);

        
        if (response != null && response.startsWith("ORDER_SUCCESS")) {
            try {
                String idStr = response.split(":")[1];
                return Integer.parseInt(idStr);
            } catch (Exception e) {
                return -1;
            }
        }
        
        return -1;
    }
    public static List<Order> getOrdersByCustomer(int userId) {
        // 1. Kirim request ke Server
        String payload = "GET_HISTORY:" + userId;
        String response = ClientSocket.getInstance().sendRequest(payload);
        
        List<Order> historyList = new ArrayList<>();

        // 2. Parsing Balasan Server
        // Format Harapan: HISTORY_DATA:ID|Date|Total|Status|ItemsSummary;ID|...
        if (response != null && response.startsWith("HISTORY_DATA:")) {
            
            // Cek apakah datanya kosong (cuma header doang)
            if (response.length() <= 13) return historyList; 

            String dataPart = response.substring(13); // Hapus "HISTORY_DATA:"
            if (!dataPart.isEmpty()) {
                String[] ordersStr = dataPart.split(";");
                for (String ord : ordersStr) {
                    try {
                        String[] fields = ord.split("\\|");
                        // fields[0]=ID, [1]=Date, [2]=Total, [3]=Status, [4]=Summary
                        
                        if (fields.length >= 5) {
                            Order o = new Order(
                                fields[1], // Date
                                fields[4], // Summary
                                fields[2], // Total
                                fields[3], // Status
                                "-",       // Note (Optional)
                                null       // List Item detail
                            );
                            o.orderId = fields[0];
                            historyList.add(o);
                        }
                    } catch (Exception e) {
                        System.err.println("Gagal parse order history: " + ord);
                    }
                }
            }
        }
        return historyList;
    }
}
