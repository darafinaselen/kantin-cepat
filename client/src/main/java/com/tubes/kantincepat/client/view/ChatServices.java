package com.tubes.kantincepat.client.view;

import com.tubes.kantincepat.client.net.ClientSocket;
import java.util.ArrayList;
import java.util.List;

public class ChatServices {

    public static boolean sendMessage(int senderId, String message) {
        // Ganti karakter titik dua (:) atau enter jika ada, agar tidak merusak format protokol
        String cleanMsg = message.replace(":", "").replace("\n", " ");
        
        String payload = "SEND_CHAT:" + senderId + ":" + cleanMsg;
        String response = ClientSocket.getInstance().sendRequest(payload);
        return response != null && response.equals("CHAT_SUCCESS");
    }

    public static List<ChatMessage> getChatHistory(int userId) {
        String payload = "GET_CHAT:" + userId;
        String response = ClientSocket.getInstance().sendRequest(payload);
        
        List<ChatMessage> list = new ArrayList<>();
        
        if (response != null && response.startsWith("CHAT_HISTORY:")) {
            if (response.length() <= 13) return list;

            String data = response.substring(13); // Hapus header
            String[] rows = data.split(";");
            
            for (String row : rows) {
                if (row.isEmpty()) continue;
                String[] parts = row.split("\\|");
                if (parts.length >= 4) {
                    try {
                        int id = Integer.parseInt(parts[0]);
                        int sender = Integer.parseInt(parts[1]);
                        String msg = parts[2];
                        String time = parts[3];
                        list.add(new ChatMessage(id, sender, msg, time));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return list;
    }
}