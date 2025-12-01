package com.tubes.kantincepat.client.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.tubes.kantincepat.client.ClientApp;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Menu_Profile extends JPanel {

    private ClientApp mainApp; // Nama variabel diperbaiki

    public Menu_Profile(ClientApp app) {
        this.mainApp = app;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); 
        setBackground(GUIUtils.COLOR_BG2);
        
        setBorder(new EmptyBorder(40, 20, 20, 20));

        // 1. Bagian Foto & Info User
        add(createProfileInfo());
        
        add(Box.createVerticalStrut(40)); 

        // 2. Bagian Menu Options
        add(createMenuOption("Language", "Globe.png"));
        add(Box.createVerticalStrut(15));
        add(createMenuOption("Payment Method", "card.png"));
        add(Box.createVerticalStrut(15));
        add(createMenuOption("Notification", "notifications.png"));

        // Tombol Logout (Tambahan)
        add(Box.createVerticalStrut(30));
        JPanel logoutPanel = createMenuOption("Sign Out", "logout.png"); // Pastikan ada icon logout.png atau ganti yg lain
        logoutPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int confirm = JOptionPane.showConfirmDialog(null, "Yakin ingin keluar?", "Logout", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    mainApp.logout(); // Panggil method logout di ClientApp
                }
            }
        });
        add(logoutPanel);

        add(Box.createVerticalGlue());
    }

    private JPanel createProfileInfo() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(GUIUtils.COLOR_BG2);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // A. Foto Profil
        RoundedPanel photoContainer = new RoundedPanel(35, GUIUtils.COLOR_ACCENT); 
        photoContainer.setPreferredSize(new Dimension(100, 100));
        photoContainer.setMaximumSize(new Dimension(100, 100));
        photoContainer.setLayout(new GridBagLayout());
        
        ImageIcon originalIcon = GUIUtils.loadImageIcon("profile.png", 80, 80);
        if (originalIcon != null) {
            photoContainer.add(new JLabel(originalIcon));
        } else {
            photoContainer.add(new JLabel("IMG"));
        }
        
        panel.add(photoContainer);

        // B. Info Teks (Ambil dari ClientApp yang sudah login)
        String displayName = "Guest User";
        String displayPhone = "-";

        // PERBAIKAN: Ambil User dari mainApp (Session lokal), gak perlu panggil Service lagi biar cepet
        if (mainApp.getCurrentUser() != null) {
            User u = mainApp.getCurrentUser();
            // PERBAIKAN: Gunakan Getter!
            displayName = u.getFullName(); 
            displayPhone = u.getPhoneNumber();
        }

        // Tampilkan ke Label
        JLabel lblName = new JLabel(displayName);
        lblName.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 18f));
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPhone = new JLabel(displayPhone);
        lblPhone.setFont(GUIUtils.getCustomFont("Lato-Regular.ttf", 14f));
        lblPhone.setForeground(Color.GRAY);
        lblPhone.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(Box.createVerticalStrut(15)); 
        panel.add(lblName);
        panel.add(Box.createVerticalStrut(5));  
        panel.add(lblPhone);

        return panel;
    }

    private JPanel createMenuOption(String text, String iconPath) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(GUIUtils.COLOR_BG2);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); 
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 1. Icon Kiri
        RoundedPanel iconContainer = new RoundedPanel(50, GUIUtils.COLOR_ACCENT); 
        iconContainer.setPreferredSize(new Dimension(45, 45));
        iconContainer.setLayout(new GridBagLayout());
        
        ImageIcon menuIcon = GUIUtils.loadImageIcon(iconPath, 24, 24);
        if (menuIcon != null) {
            iconContainer.add(new JLabel(menuIcon));
        } else {
            iconContainer.add(new JLabel("?"));
        }

        // 2. Teks Tengah
        JLabel lblText = new JLabel(text);
        lblText.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 14f));

        // 3. Panah Kanan
        JLabel lblArrow = new JLabel("›"); 
        lblArrow.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 20f));
        lblArrow.setForeground(Color.GRAY);
        lblArrow.setBorder(new EmptyBorder(0, 0, 0, 10)); 

        panel.add(iconContainer, BorderLayout.WEST);
        panel.add(lblText, BorderLayout.CENTER);
        panel.add(lblArrow, BorderLayout.EAST);
        
        // Efek Hover
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                lblText.setForeground(GUIUtils.COLOR_PRIMARY);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                lblText.setForeground(Color.BLACK);
            }
        });

        return panel;
    }
}