package com.tubes.kantincepat.model;

import java.io.Serializable;


public class User implements Serializable {
    private static final long serialVersionUID = 1L; 

    private int id;
    private String username;
    private String fullName;
    private String phoneNumber;
    private String role; // "CUSTOMER", "ADMIN", "KITCHEN"

    public User() {}

    public User(int id, String username, String fullName, String phoneNumber, String role) {
        this.id = id;
        this.username = username;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    // Getter dan Setter (untuk akses data)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
