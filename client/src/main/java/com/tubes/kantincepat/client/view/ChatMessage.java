package com.tubes.kantincepat.client.view;

public class ChatMessage {
    public int id;
    public int senderId;
    public String message;
    public String time;

    public ChatMessage(int id, int senderId, String message, String time) {
        this.id = id;
        this.senderId = senderId;
        this.message = message;
        this.time = time;
    }
}