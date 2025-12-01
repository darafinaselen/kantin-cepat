package com.tubes.kantincepat.client.view;

import com.tubes.kantincepat.client.net.ClientSocket;

public class UserServices {

    public static User getUserById(int userId) {
        String payload = "GET_USER:" + userId;
        String response = ClientSocket.getInstance().sendRequest(payload);

        // Format: USER_DATA:ID:Username:Email:FullName:Phone:Role
        if (response != null && response.startsWith("USER_DATA:")) {
            try {
                String[] parts = response.split(":");
                // parts[0] = HEADER, parts[1] = ID, parts[2] = Username, dst...
                
                return new User(
                    Integer.parseInt(parts[1]), // ID
                    parts[2],                   // Username
                    parts[3],                   // Email
                    parts[4],                   // FullName
                    parts[5],                   // Phone
                    parts[6]                    // Role
                );
            } catch (Exception e) {
                System.err.println("Gagal parse user data");
            }
        }
        return null;
    }
}