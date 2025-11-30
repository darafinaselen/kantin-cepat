package app;

import java.io.*;
import java.net.Socket;

public class SocketService {
    
    // IP "127.0.0.1" atau "localhost" artinya server ada di laptop yang sama.
    // Jika server di laptop teman (beda wifi), ganti ini dengan IP Laptop teman (misal: "192.168.1.XX")
    private static final String SERVER_IP = "127.0.0.1"; 
    
    // PENTING: SUDAH DISESUAIKAN DENGAN SERVER TEMANMU (8080)
    // Lihat screenshot kamu: private static final int PORT = 8080;
    private static final int SERVER_PORT = 8080; 

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    // Method untuk membuka koneksi
    public void connect() throws IOException {
        // Ini langkah "Menelepon" ke Server
        socket = new Socket(SERVER_IP, SERVER_PORT);
        
        // Siapkan jalur bicara (Output) dan jalur dengar (Input)
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        System.out.println("[CLIENT] Berhasil terhubung ke Server di Port " + SERVER_PORT);
    }

    // Method kirim pesan -> Tunggu balasan
    public String sendRequest(String message) {
        if (socket == null || socket.isClosed()) {
            try {
                connect(); // Coba konek ulang otomatis kalau putus
            } catch (IOException e) {
                return "ERROR: Gagal koneksi ke server (Server mungkin mati atau beda Port).";
            }
        }

        try {
            out.println(message); // 1. Kirim pesan ke Server
            return in.readLine(); // 2. Tunggu & Baca balasan dari Server
        } catch (IOException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    // Tutup koneksi saat aplikasi close
    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}