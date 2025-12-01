package com.tubes.kantincepat.client.view;

public class User {
    private int id;             
    private String username;
    private String email;
    private String fullName;    
    private String phoneNumber; 
    private String role;      

    // Constructor Lengkap
    public User(int id, String username, String email, String fullName, String phoneNumber, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }
    
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getRole() {
        return role;
    }
}