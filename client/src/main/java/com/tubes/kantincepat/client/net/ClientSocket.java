package com.tubes.kantincepat.client.net;

import java.io.*;
import java.net.Socket;

public class ClientSocket {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8080;
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private static ClientSocket instance;

    // Singleton: Biar koneksinya satu saja dipake bareng-bareng
    private ClientSocket() {
        try {
            socket = new Socket(SERVER_IP, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ClientSocket getInstance() {
        if (instance == null) {
            instance = new ClientSocket();
        }
        return instance;
    }

    public String sendRequest(String message) {
        if (out == null) return "ERROR:No Connection";
        System.out.println("[Client] Mengirim: " + message);
        out.println(message);
        
        try {
            String response = in.readLine();
            System.out.println("[Client] Diterima: " + response);
            return response;
        } catch (IOException e) {
            return "ERROR:Read Timeout";
        }
    }
}