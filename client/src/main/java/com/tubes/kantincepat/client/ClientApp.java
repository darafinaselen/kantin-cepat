package com.tubes.kantincepat.client;

import com.tubes.kantincepat.client.view.LoginPanel;
import com.tubes.kantincepat.client.view.RegisterPanel;
import javax.swing.*;
import java.awt.*;

public class ClientApp extends JFrame {

    public static final int MOBILE_WIDTH = 350;
    public static final int MOBILE_HEIGHT = 650;

    private CardLayout cardLayout;
    private JPanel mainPanel;

    public ClientApp() {
        setTitle("Kantin Pintar - Customer App");
        setSize(MOBILE_WIDTH, MOBILE_HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Setup CardLayout (Tumpukan Halaman)
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Masukkan Panel-panel yang sudah kita pecah tadi
        // 'this' dilempar ke panel supaya panel bisa panggil method showView()
        mainPanel.add(new LoginPanel(this), "LOGIN");
        mainPanel.add(new RegisterPanel(this), "REGISTER");
        // Nanti bisa tambah: mainPanel.add(new DashboardPanel(this), "DASHBOARD");

        add(mainPanel);
    }

    // Method Publik agar Panel anak bisa minta ganti halaman
    public void showView(String viewName) {
        cardLayout.show(mainPanel, viewName);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClientApp().setVisible(true);
        });
    }
}