package com.tubes.kantincepat.client.net;

import java.io.*;
import java.net.Socket;

public class ChatClient {

    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8080;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public interface ChatListener {
        void onChatMessage(int senderId, String message, boolean isRead);
    }

    private ChatListener listener;
    private int currentUserId;

    public ChatClient(int currentUserId) throws IOException {
        this.currentUserId = currentUserId;
        this.socket = new Socket(SERVER_IP, SERVER_PORT);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);

        out.println("CHAT_LOGIN:" + currentUserId);

        startListenerThread();
    }

    public void setListener(ChatListener listener) {
        this.listener = listener;
    }

    private void startListenerThread() {
        Thread t = new Thread(() -> {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    System.out.println("[CHAT-CLIENT] Received: " + line);

                    if (line.startsWith("CHAT_MESSAGE:")) {
                        // format: CHAT_MESSAGE:senderId:message:isRead
                        String[] parts = line.split(":", 4);
                        int senderId = Integer.parseInt(parts[1]);
                        String message = parts[2];
                        boolean isRead = parts.length > 3 && "true".equalsIgnoreCase(parts[3]);

                        if (listener != null) {
                            listener.onChatMessage(senderId, message, isRead);
                        }
                    } else if (line.startsWith("READ_STATUS:")) {
                        // format: READ_STATUS:senderId
                        String[] parts = line.split(":");
                        int senderId = Integer.parseInt(parts[1]);
                        
                        System.out.println("[CHAT-CLIENT] READ_STATUS received for senderId: " + senderId + ", my ID: " + currentUserId);
                        
                        // Jika pesan yang dibaca adalah pesan kita, update UI
                        if (senderId == currentUserId && listener != null) {
                            System.out.println("[CHAT-CLIENT] Triggering tick update to blue");
                            // Trigger update UI untuk ubah centang jadi biru
                            listener.onChatMessage(senderId, "", true);
                        }
                    } else {
                        System.out.println("[CHAT] Server: " + line);
                    }
                }
            } catch (IOException e) {
                System.out.println("[CHAT] Connection closed");
            }
        });
        t.setDaemon(true);
        t.start();
    }

    public void sendChat(String message) {
        if (out != null) {
            System.out.println("[Client-Chat] SEND_CHAT:" + message);
            out.println("SEND_CHAT:" + message);
        }
    }

    // Panggil manual dari UI ketika user membuka chat
    public void markAllAsRead() {
        if (out != null) {
            System.out.println("[Client-Chat] MARK_ALL_READ");
            out.println("MARK_ALL_READ");
        }
    }

    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}