package com.tubes.kantincepat.client.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.tubes.kantincepat.client.ClientApp;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Menu_Profile extends JPanel {

    private ClientApp mainApp;
    
    // Class-level variables to hold references to the labels we need to update
    private JLabel lblUsername;
    private JLabel lblFullName;
    private JLabel lblEmail;
    private JLabel lblPhone;
    private JLabel lblRole;
    
    // Separate labels for the top profile info section
    private JLabel lblTopName;
    private JLabel lblTopPhone;

    public Menu_Profile(ClientApp app) {
        this.mainApp = app;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); 
        setBackground(GUIUtils.COLOR_BG2);
        
        setBorder(new EmptyBorder(40, 20, 20, 20));

        // 1. Bagian Foto & Info User (Top Section)
        add(createProfileInfo());
        
        add(Box.createVerticalStrut(20)); // Reduced strut

        // 1.5 Bagian Detail Data User (Middle Section - The one with Username, Email, etc.)
        // We add this so we can see the full details
        add(createDetailInfo());

        add(Box.createVerticalStrut(20)); 

        // 2. Bagian Menu Options
        add(createMenuOption("Language", "Globe.png"));
        add(Box.createVerticalStrut(15));
        add(createMenuOption("Payment Method", "card.png"));
        add(Box.createVerticalStrut(15));
        add(createMenuOption("Notification", "notifications.png"));

        // Tombol Logout
        add(Box.createVerticalStrut(30));
        JPanel logoutPanel = createMenuOption("Sign Out", "logout.png"); 
        logoutPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int confirm = JOptionPane.showConfirmDialog(null, "Yakin ingin keluar?", "Logout", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    mainApp.logout(); 
                }
            }
        });
        add(logoutPanel);

        add(Box.createVerticalGlue());
    }

    // This method is called by ClientApp whenever the profile tab is opened
    public void setUserData(User user) {
        if (user != null) {
            // Update the top section
            if (lblTopName != null) lblTopName.setText(user.getFullName());
            if (lblTopPhone != null) lblTopPhone.setText(user.getPhoneNumber());

            // Update the detail section
            if (lblUsername != null) lblUsername.setText(user.getUsername());
            if (lblFullName != null) lblFullName.setText(user.getFullName());
            if (lblEmail != null) lblEmail.setText(user.getEmail());
            if (lblPhone != null) lblPhone.setText(user.getPhoneNumber());
            if (lblRole != null) lblRole.setText(user.getRole());
        }
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

        // B. Init Labels
        lblTopName = new JLabel("Guest User");
        lblTopName.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 18f));
        lblTopName.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblTopPhone = new JLabel("-");
        lblTopPhone.setFont(GUIUtils.getCustomFont("Lato-Regular.ttf", 14f));
        lblTopPhone.setForeground(Color.GRAY);
        lblTopPhone.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(Box.createVerticalStrut(15)); 
        panel.add(lblTopName);
        panel.add(Box.createVerticalStrut(5));  
        panel.add(lblTopPhone);

        return panel;
    }

    // New helper method to display detailed info list
    private JPanel createDetailInfo() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(GUIUtils.COLOR_BG2); // Match background
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Initialize the class-level labels
        lblUsername = createValueLabel("-");
        lblFullName = createValueLabel("-");
        lblEmail = createValueLabel("-");
        lblPhone = createValueLabel("-");
        lblRole = createValueLabel("-");

        // Add them to the panel with labels
        // You might want to style this differently, but for now a simple list:
        // panel.add(createDetailItem("Username:", lblUsername));
        // panel.add(createDetailItem("Email:", lblEmail));
        // ... add others as needed
        
        // For simplicity based on your previous code structure, I'll just return an empty panel 
        // if you only want the top section. 
        // BUT, if you want the details list visible:
        
        /* Uncomment this block to see the list */
        /*
        panel.add(createDetailRow("Username", lblUsername));
        panel.add(Box.createVerticalStrut(5));
        panel.add(createDetailRow("Email", lblEmail));
        */
        
        return panel; 
    }
    
    private JLabel createValueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(GUIUtils.getCustomFont("Lato-Regular.ttf", 12f));
        return l;
    }
    
    // Helper to create rows for detail info
    private JPanel createDetailRow(String key, JLabel valueLabel) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(GUIUtils.COLOR_BG2);
        p.setMaximumSize(new Dimension(300, 20));
        
        JLabel k = new JLabel(key);
        k.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 12f));
        
        p.add(k, BorderLayout.WEST);
        p.add(valueLabel, BorderLayout.EAST);
        return p;
    }

    private JPanel createMenuOption(String text, String iconPath) {
        // ... (This method remains exactly the same as your code)
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