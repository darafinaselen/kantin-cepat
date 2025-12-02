package com.tubes.kantincepat.server;

import java.io.*;
import java.net.Socket;

public class TestClient {
    public static void main(String[] args) {
        try {
            // 1. Connect ke Server kamu
            System.out.println("Menghubungkan ke server...");
            Socket socket = new Socket("localhost", 8080);
            
            // 2. Siapkan jalur komunikasi
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // 3. Kirim Pesan Login Palsu
            // Format: LOGIN:username:password
            String pesan = "LOGIN:dapur:dapur123";
            System.out.println("Mengirim: " + pesan);
            out.println(pesan);

            // 4. Baca Balasan Server
            String balasan = in.readLine();
            System.out.println("Server Menjawab: " + balasan);

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
