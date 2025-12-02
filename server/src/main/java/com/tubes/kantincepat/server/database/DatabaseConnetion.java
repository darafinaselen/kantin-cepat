package com.tubes.kantincepat.server.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnetion {
    private static final String URL = "jdbc:postgresql://localhost:5432/kantin_cepat_db";
    private static final String USER = "postgres"; 
    private static final String PASSWORD = "pw"; 
    
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL Driver not found!");
        }
    }
}
