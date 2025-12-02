package com.tubes.kantincepat.client;

import javax.swing.SwingUtilities;
// Import LoginAdmin dari package barunya
import com.tubes.kantincepat.client.view.admin.LoginAdmin;

public class AdminApp {
    public static void main(String[] args) {
        // Menjalankan aplikasi di Event Dispatch Thread (Standard Swing)
        SwingUtilities.invokeLater(() -> {
            // Membuka halaman Login Admin pertama kali
            new LoginAdmin().setVisible(true);
        });
    }
}