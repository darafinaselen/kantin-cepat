package com.tubes.kantincepat.client.view;

import com.tubes.kantincepat.client.ClientApp;
import com.tubes.kantincepat.client.net.ClientSocket;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginPanel extends JPanel {
    
    public LoginPanel(ClientApp mainApp) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(GUIUtils.COLOR_BG);

        ImageIcon headerIcon = GUIUtils.loadImageIcon("kantin-pintar.png", 375, 128);
        if (headerIcon != null) {
            JLabel headerLabel = new JLabel(headerIcon);
            headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            add(headerLabel);
        }

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(GUIUtils.COLOR_BG);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        JLabel title = new JLabel("Sign In");
        title.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 24f));
        title.setForeground(GUIUtils.COLOR_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField txtIdentitas = GUIUtils.createRoundedTextField("Email/Username");
        JPasswordField txtPass = GUIUtils.createRoundedPasswordField();
        JButton btnLogin = GUIUtils.createStyledButton("Sign In");

        JLabel linkRegister = new JLabel("Don't have an account? Sign Up");
        linkRegister.setForeground(GUIUtils.COLOR_BTN);
        linkRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkRegister.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- Logic Pindah Halaman ---
        linkRegister.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                // Panggil method di ClientApp buat ganti layar
                mainApp.showView("REGISTER"); 
            }
        });

        // --- Logic Login ---
        btnLogin.addActionListener(e -> {
            String id = txtIdentitas.getText();
            String pass = new String(txtPass.getPassword());
            if (id.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Isi semua data!");
                return;
            }
            
            String response = ClientSocket.getInstance().sendRequest("LOGIN:" + id + ":" + pass);
            if (response != null && response.startsWith("LOGIN_SUCCESS")) {
                String fullname = response.split(":")[2];
                JOptionPane.showMessageDialog(this, "Welcome, " + fullname);
                // TODO: mainApp.showView("DASHBOARD");
            } else {
                JOptionPane.showMessageDialog(this, "Login Gagal!");
            }
        });

        // Susun Layout
        formPanel.add(title);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        GUIUtils.addLabelAndField(formPanel, "Email or Username", txtIdentitas);
        GUIUtils.addLabelAndField(formPanel, "Password", txtPass);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(btnLogin);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(linkRegister);

        add(formPanel);
    }
}