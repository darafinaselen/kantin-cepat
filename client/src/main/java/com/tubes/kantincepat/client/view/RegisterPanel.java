package com.tubes.kantincepat.client.view;

import com.tubes.kantincepat.client.KitchenApp;
import com.tubes.kantincepat.client.net.ClientSocket;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RegisterPanel extends JPanel {

    public RegisterPanel(KitchenApp mainApp) {
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

        JLabel title = new JLabel("Sign Up");
        title.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 24f));
        title.setForeground(GUIUtils.COLOR_TEXT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField txtUser = GUIUtils.createRoundedTextField("Username");
        JTextField txtEmail = GUIUtils.createRoundedTextField("Email");
        JPasswordField txtPass = GUIUtils.createRoundedPasswordField();
        JTextField txtName = GUIUtils.createRoundedTextField("Full Name");
        JTextField txtPhone = GUIUtils.createRoundedTextField("Phone");
        JButton btnReg = GUIUtils.createStyledButton("Sign Up");

        JLabel linkLogin = new JLabel("Already have an account? Sign In");
        linkLogin.setForeground(GUIUtils.COLOR_BTN);
        linkLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkLogin.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- Logic Pindah ke Login ---
        linkLogin.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                mainApp.showView("LOGIN");
            }
        });

        // --- Logic Register ---
        btnReg.addActionListener(e -> {
            if (txtUser.getText().isEmpty() || txtEmail.getText().isEmpty() || 
                txtName.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Lengkapi data!");
                return;
            }
            
            String payload = "REGISTER:" + txtUser.getText() + ":" + txtEmail.getText() + ":" + 
                             new String(txtPass.getPassword()) + ":" + txtName.getText() + ":" + txtPhone.getText();
            
            String resp = ClientSocket.getInstance().sendRequest(payload);
            if ("REGISTER_SUCCESS".equals(resp)) {
                JOptionPane.showMessageDialog(this, "Sukses! Silakan Login.");
                mainApp.showView("LOGIN");
            } else {
                JOptionPane.showMessageDialog(this, "Gagal Register.");
            }
        });

        formPanel.add(title);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        GUIUtils.addLabelAndField(formPanel, "Username", txtUser);
        GUIUtils.addLabelAndField(formPanel, "Email", txtEmail);
        GUIUtils.addLabelAndField(formPanel, "Password", txtPass);
        GUIUtils.addLabelAndField(formPanel, "Full Name", txtName);
        GUIUtils.addLabelAndField(formPanel, "Phone", txtPhone);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(btnReg);
        formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        formPanel.add(linkLogin);

        add(formPanel);
    }
}