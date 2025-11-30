package com.tubes.kantincepat.client.view;

public class User {
    public int id;
    public String fullName;    // Sesuaikan dengan kolom full_name
    public String phoneNumber; // Sesuaikan dengan kolom phone_number
    public String role;

    public User(int id, String fullName, String phoneNumber, String role) {
        this.id = id;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }
}