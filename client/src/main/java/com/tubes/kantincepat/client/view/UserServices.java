// package com.tubes.kantincepat.client.view;

// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;

// public class UserServices {

//     // Method untuk mengambil data user berdasarkan ID
//     public static User getUserById(int userId) {
//         User user = null;
//         String sql = "SELECT user_id, full_name, phone_number, role FROM users WHERE user_id = ?";
//         try (Connection conn = koneksiDB.getConnection();
//              PreparedStatement ps = conn.prepareStatement(sql)) {

//             ps.setInt(1, userId);
//             ResultSet rs = ps.executeQuery();

//             if (rs.next()) {
//                 // Mapping nama kolom DB baru ke Object User
//                 String name = rs.getString("full_name"); 
//                 String phone = rs.getString("phone_number"); 
//                 String role = rs.getString("role");
//                 user = new User(userId, name, phone, role);
//             }

//         } catch (Exception e) {
//             e.printStackTrace();
//         }
//         return user;
//     }
// }