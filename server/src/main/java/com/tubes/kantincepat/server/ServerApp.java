package com.tubes.kantincepat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.tubes.kantincepat.server.handler.ClientHandler;

public class ServerApp {
    private static final int PORT = 8080;
    public static void main(String[] args) {
        System.out.println("✅ Server Kantin Cepat starting on port " + PORT + "...");
        
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("🔗 New client connected: " + clientSocket.getInetAddress());

                // Lempar ke thread baru (ClientHandler)
                ClientHandler handler = new ClientHandler(clientSocket);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
