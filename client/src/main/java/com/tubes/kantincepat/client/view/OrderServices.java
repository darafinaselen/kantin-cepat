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
        String payload = "GET_HISTORY:" + userId;
        String response = ClientSocket.getInstance().sendRequest(payload);
        
        List<Order> historyList = new ArrayList<>();

        if (response != null && response.startsWith("HISTORY_DATA:")) {
            
            if (response.length() <= 13) return historyList; 

            String dataPart = response.substring(13); 
            if (!dataPart.isEmpty()) {
                // Pisahkan antar Order (pakai ;)
                String[] ordersStr = dataPart.split(";");
                
                for (String ord : ordersStr) {
                    if (ord.trim().isEmpty()) continue;

                    try {
                        // Pisahkan field dalam satu order (pakai |)
                        String[] fields = ord.split("\\|");
                        
                        // KITA BUTUH 7 KOLOM (Termasuk Notes & Detail)
                        if (fields.length >= 7) {
                            String id = fields[0];
                            String date = fields[1];
                            String total = "Rp " + fields[2]; 
                            String status = fields[3];
                            String summary = fields[4];
                            String notes = fields[5];       
                            String rawItems = fields[6];    

                            // --- LOGIKA PARSING DETAIL ITEM ---
                            List<MenuItem> itemList = new ArrayList<>();
                            
                            if (!rawItems.equals("null") && !rawItems.isEmpty()) {
                                // Split antar item menggunakan # (Sesuai update ClientHandler)
                                String[] itemArray = rawItems.split("#");
                                
                                for (String itemStr : itemArray) {
                                    // Split detail: ID,Nama,Harga,Qty
                                    String[] det = itemStr.split(",");
                                    
                                    if (det.length >= 4) {
                                        int menuId = Integer.parseInt(det[0]);
                                        String name = det[1];
                                        int price = Integer.parseInt(det[2]);
                                        int qty = Integer.parseInt(det[3]);
                                        String mImg = det[4].equals("null") ? "" : det[4];

                                        // Buat objek MenuItem sementara
                                        MenuItem mi = new MenuItem(menuId, name, "-", price, "", mImg, true);
                                        
                                        // Masukkan ke list sebanyak qty agar hitungan di Invoice benar
                                        for(int k=0; k<qty; k++) {
                                            itemList.add(mi);
                                        }
                                    }
                                }
                            }
                            // ----------------------------------

                            // Masukkan ke Constructor Order yang BARU (dengan ID, Notes, dan List Item)
                            Order o = new Order(id, date, summary, total, status, notes, itemList);
                            historyList.add(o);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Gagal parse order: " + ord);
                    }
                }
            }
        }
        return historyList;
    }
}
