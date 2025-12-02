package com.tubes.kantincepat.client.net;

import java.io.*;
import java.net.Socket;

public class ChatClient {

    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8080;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    // listener supaya UI bisa diberi callback
    public interface ChatListener {
        void onChatMessage(int senderId, String message);
    }

    private ChatListener listener;
    private int currentUserId;

    public ChatClient(int currentUserId) throws IOException {
        this.currentUserId = currentUserId;
        this.socket = new Socket(SERVER_IP, SERVER_PORT);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);

        // identitas ke server (sederhana, tidak pakai password lagi)
        out.println("CHAT_LOGIN:" + currentUserId);

        // mulai thread listener
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

                    if (line.startsWith("CHAT_MESSAGE:")) {
                        // format: CHAT_MESSAGE:senderId:message
                        String[] parts = line.split(":", 3);
                        int senderId = Integer.parseInt(parts[1]);
                        String message = parts[2];

                        if (listener != null) {
                            listener.onChatMessage(senderId, message);
                        }
                    } else {
                        // kalau ada respons lain, untuk sekarang log saja
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


    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}
