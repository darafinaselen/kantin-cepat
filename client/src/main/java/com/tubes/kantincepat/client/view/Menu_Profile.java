package com.tubes.kantincepat.client.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.tubes.kantincepat.client.ClientApp;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Menu_Profile extends JPanel {

    private ClientApp ClientApp;

    public Menu_Profile(ClientApp app) {
        this.ClientApp = app;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)); // Susunan Vertikal
        setBackground(ClientApp.COLOR_BG);
        
        // Padding halaman (Atas, Kiri, Bawah, Kanan)
        setBorder(new EmptyBorder(40, 20, 20, 20));

        // 1. Bagian Foto & Info User
        add(createProfileInfo());
        
        add(Box.createVerticalStrut(40)); // Jarak antara profil dan menu

        // 2. Bagian Menu Options
        add(createMenuOption("Language", "Globe.png"));
        add(Box.createVerticalStrut(15));
        add(createMenuOption("Payment Method", "card.png"));
        add(Box.createVerticalStrut(15));
        add(createMenuOption("Notification", "notifications.png"));

        // Push sisa konten ke atas
        add(Box.createVerticalGlue());
    }

    private JPanel createProfileInfo() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ClientApp.COLOR_BG);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT); // Rata tengah secara horizontal

        // A. Container Foto Profil (Kotak Pink Rounded)
        // Radius 35 agar sudutnya tumpul seperti di gambar (Squircle)
        JPanel photoContainer = new RoundedPanel(35, ClientApp.COLOR_ACCENT); 
        photoContainer.setPreferredSize(new Dimension(100, 100));
        photoContainer.setMaximumSize(new Dimension(100, 100));
        photoContainer.setLayout(new GridBagLayout()); // Center image inside
        
        // Load Gambar Profil User
        ImageIcon originalIcon = new ImageIcon("profile.png"); // Pastikan file ini ada
        Image scaledImage = originalIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        JLabel lblPhoto = new JLabel(new ImageIcon(scaledImage));
        
        photoContainer.add(lblPhoto);

        // B. Info Teks
        int currentUserId = 3; 
        User currentUser = UserServices.getUserById(currentUserId);

        // 2. Siapkan Variabel Default (Jaga-jaga jika DB error/data kosong)
        String displayName = "Guest User";
        String displayPhone = "-";

        if (currentUser != null) {
            // Gunakan field yang baru (fullName & phoneNumber)
            displayName = currentUser.fullName; 
            displayPhone = currentUser.phoneNumber;
        }

        // 3. Tampilkan ke Label
        JLabel lblName = new JLabel(displayName);
        lblName.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblPhone = new JLabel(displayPhone);
        lblPhone.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblPhone.setForeground(Color.GRAY);
        lblPhone.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Susun komponen
        panel.add(photoContainer);
        panel.add(Box.createVerticalStrut(15)); // Jarak foto ke nama
        panel.add(lblName);
        panel.add(Box.createVerticalStrut(5));  // Jarak nama ke nomor
        panel.add(lblPhone);

        return panel;
    }

    private JPanel createMenuOption(String text, String iconPath) {
        // Container Baris Menu
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(ClientApp.COLOR_BG); // Transparan/Warna dasar
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60)); // Tinggi fix 60px
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 1. Icon Kiri (Gambar dalam lingkaran/kotak rounded)
        JPanel iconContainer = new RoundedPanel(50, ClientApp.COLOR_ACCENT); // Bulat penuh atau Rounded
        iconContainer.setPreferredSize(new Dimension(45, 45));
        iconContainer.setLayout(new GridBagLayout());
        
        ImageIcon menuIcon = GUIUtils.loadImageIcon(iconPath, 24, 24);
        if (menuIcon != null) {
            iconContainer.add(new JLabel(menuIcon));
        } else {
            iconContainer.add(new JLabel("IMG"));
        }
        // 2. Teks Tengah
        JLabel lblText = new JLabel(text);
        lblText.setFont(new Font("SansSerif", Font.BOLD, 14));

        // 3. Panah Kanan
        JLabel lblArrow = new JLabel("›"); // Simbol chevron
        lblArrow.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblArrow.setForeground(Color.GRAY);
        lblArrow.setBorder(new EmptyBorder(0, 0, 0, 10)); // Padding kanan

        panel.add(iconContainer, BorderLayout.WEST);
        panel.add(lblText, BorderLayout.CENTER);
        panel.add(lblArrow, BorderLayout.EAST);
        
        // Efek Hover sederhana (Opsional)
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                // Saat hover teks jadi warna primary
                lblText.setForeground(ClientApp.COLOR_PRIMARY);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                // Saat keluar balik jadi hitam
                lblText.setForeground(Color.BLACK);
            }
        });

        return panel;
    }
}
