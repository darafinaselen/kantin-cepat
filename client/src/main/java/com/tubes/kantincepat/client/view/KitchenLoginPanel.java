package com.tubes.kantincepat.client.view;

import com.tubes.kantincepat.client.KitchenApp;
import com.tubes.kantincepat.client.net.ClientSocket;
import javax.swing.*;
import java.awt.*;

public class KitchenLoginPanel extends JPanel {

    public KitchenLoginPanel(KitchenApp mainApp) {
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

        JTextField txtIdentitas = GUIUtils.createRoundedTextFieldForKitchen("Email/Username");
        JPasswordField txtPass = GUIUtils.createRoundedPasswordFieldForKitchen();
        JButton btnLogin = GUIUtils.createStyledButton("Sign In");

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
                String[] p = response.split(":");
                String role     = p[1];
                String fullname = p[2];
                int userId      = Integer.parseInt(p[3]);

                mainApp.setCurrentUser(userId, role, fullname);

                JOptionPane.showMessageDialog(this, "Welcome, " + fullname);

                if ("KITCHEN".equals(role)) {
                    mainApp.switchToKitchenView();
                } else {
                    // CUSTOMER dan ADMIN akan masuk ke LiveChat
                    // mainApp.getLiveChatPanel().initChatForCurrentUser();
                    mainApp.showView("LIVE_CHAT");
                }
            }else {
                JOptionPane.showMessageDialog(this, "Login Gagal: " + response);
            }

        });

        // Susun Layout
        formPanel.add(title);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        GUIUtils.addLabelAndFieldForKitchen(formPanel, "Email or Username", txtIdentitas);
        GUIUtils.addLabelAndFieldForKitchen(formPanel, "Password", txtPass);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        formPanel.add(btnLogin);
        formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        add(formPanel);
    }

}