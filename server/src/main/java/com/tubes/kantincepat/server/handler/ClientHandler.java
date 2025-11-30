package com.tubes.kantincepat.server.handler;

// Sesuaikan nama package database kamu (DatabaseConnetion atau Database)
import com.tubes.kantincepat.server.database.DatabaseConnetion; 
import java.io.*;
import java.net.Socket;
import java.sql.*;

public class ClientHandler implements Runnable {
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
                
                // PENTING: Split pakai ";" agar aman (karena Client pakai ";")
                // Kalau Client pakai ":", ganti split(":")
                // Tapi saran saya pakai ";" karena di Client saya set pakai ";"
                String[] parts = message.split(";"); 
                String command = parts[0];

                switch (command) {
                    case "LOGIN":
                        handleLogin(parts);
                        break;
                    case "REGISTER":
                        handleRegister(parts);
                        break;
                    // --- TAMBAHAN BARU ---
                    case "ADD_MENU":
                        handleAddMenu(parts);
                        break;
                    case "ADD_USER":
                        handleAddUser(parts);
                        break;
                    // ---------------------
                    default:
                        out.println("ERROR:Unknown Command");
                }
            }
        } catch (IOException e) {
            System.out.println("❌ Client disconnected");
        }
    }

    private void handleLogin(String[] parts) {
        // Format Client Lama mungkin pakai ":" -> Sesuaikan Split di atas
        // Asumsi format: LOGIN;username;password
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
        } catch (SQLException e) {
            e.printStackTrace();
            out.println("LOGIN_ERROR:Database Error");
        }
    }

    private void handleRegister(String[] parts) {
        if (parts.length < 6) { out.println("REGISTER_FAILED:Format Salah"); return; }

        try (Connection conn = DatabaseConnetion.getConnection()) {
            String sql = "INSERT INTO users (username, email, password, full_name, phone_number, role) VALUES (?, ?, ?, ?, ?, 'CUSTOMER')";
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

    // --- FITUR BARU: ADD MENU ---
    private void handleAddMenu(String[] parts) {
        // Format: ADD_MENU;Nama;Deskripsi;Harga;Kategori;Status;Gambar
        if (parts.length < 6) { out.println("ERROR:Data Menu Tidak Lengkap"); return; }

        try (Connection conn = DatabaseConnetion.getConnection()) {
            // CASTING KE ENUM POSTGRESQL (PENTING!)
            String sql = "INSERT INTO menu_items (name, description, price, category, is_available, image_path) VALUES (?, ?, ?, ?::menu_category, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            stmt.setString(1, parts[1]); // Nama
            stmt.setString(2, parts[2]); // Deskripsi
            
            // Harga (Parse Double)
            try {
                stmt.setDouble(3, Double.parseDouble(parts[3])); 
            } catch (NumberFormatException e) {
                out.println("ERROR:Harga harus angka");
                return;
            }

            stmt.setString(4, parts[4]); // Kategori (MEALS, DRINK, SNACK)
            
            // Status (TRUE/FALSE)
            boolean isAvailable = parts[5].toUpperCase().startsWith("TRUE");
            stmt.setBoolean(5, isAvailable);
            
            // Gambar (Default jika kosong)
            String img = (parts.length > 6) ? parts[6] : "assets/no-image.png";
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
}