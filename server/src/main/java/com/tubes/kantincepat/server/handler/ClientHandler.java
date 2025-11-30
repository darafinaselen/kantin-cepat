package com.tubes.kantincepat.server.handler;

import com.tubes.kantincepat.server.database.DatabaseConnetion;
import java.io.*;
import java.net.Socket;
import java.sql.*;

public class ClientHandler implements Runnable{
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
                
                String[] parts = message.split(":");
                String command = parts[0];

                switch (command) {
                    case "LOGIN":
                        handleLogin(parts);
                        break;
                    case "REGISTER":
                        handleRegister(parts);
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
                String role = rs.getString("role"); 
                String fullname = rs.getString("full_name");
                out.println("LOGIN_SUCCESS:" + role + ":" + fullname);
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
    
}
