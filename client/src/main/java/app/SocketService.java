package app;

import java.io.*;
import java.net.Socket;

public class SocketService {
    
    // Ganti IP ini kalau server ada di laptop teman (misal: "192.168.1.5")
    // Kalau run di laptop sendiri pakai "localhost" atau "127.0.0.1"
    private static final String SERVER_IP = "127.0.0.1";
    private static final int SERVER_PORT = 12345; // Pastikan port sama dengan temanmu

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    // Method untuk membuka koneksi (Dipanggil saat login berhasil)
    public void connect() throws IOException {
        socket = new Socket(SERVER_IP, SERVER_PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Berhasil terhubung ke Server!");
    }

    // Method inti: Kirim pesan -> Tunggu balasan
    public String sendRequest(String message) {
        if (socket == null || socket.isClosed()) {
            try {
                connect(); // Coba konek ulang otomatis kalau putus
            } catch (IOException e) {
                return "ERROR: Gagal koneksi ke server.";
            }
        }

        try {
            out.println(message); // 1. Kirim pesan ke Temanmu
            return in.readLine(); // 2. Baca balasan dari Temanmu
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