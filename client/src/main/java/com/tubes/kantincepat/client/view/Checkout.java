package com.tubes.kantincepat.client.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import com.tubes.kantincepat.client.ClientApp;


public class Checkout extends JPanel {

    private ClientApp ClientApp;

    public Checkout(ClientApp app) {
        this.ClientApp = app;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE); // Background Putih bersih
        setBorder(new EmptyBorder(40, 30, 40, 30)); // Padding besar

        // --- 1. TEKS JUDUL ---
        JLabel lblYum = new JLabel("Yum!");
        lblYum.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblYum.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitle = new JLabel("Your order is in the work.");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- 2. TEKS DESKRIPSI (Multi-line) ---
        // Gunakan HTML tag agar teks bisa wrap (turun baris) otomatis dan rata tengah
        JLabel lblDesc = new JLabel("<html><center>" +
                "We will notify you once your order is ready. " +
                "Please pick up your order at the cashier!" +
                "</center></html>");
        lblDesc.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblDesc.setForeground(Color.GRAY);
        lblDesc.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Batasi lebar teks agar tidak terlalu melebar
        lblDesc.setMaximumSize(new Dimension(300, 60)); 

        // --- 3. GAMBAR ILUSTRASI ---
        JLabel imageLabel = new JLabel();
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        try {
            // Ganti "illustration_success.png" dengan nama file gambarmu
            ImageIcon originalIcon = GUIUtils.loadImageIcon("checkout.png", 240, 240);
            imageLabel.setIcon(new ImageIcon(originalIcon.getImage()));
        } catch (Exception e) {
            imageLabel.setText("[Gambar Ilustrasi]");
        }

        // --- 4. TOMBOL BACK TO HOME ---
        JPanel btnHome = createButton("Back to Home");
        // Aksi Klik
        btnHome.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Kembali ke Home
                ClientApp.showView("HOME");
            }
        });

        // --- SUSUN TATA LETAK (LAYOUTING) ---
        add(Box.createVerticalStrut(20)); // Spasi atas
        add(lblYum);
        add(Box.createVerticalStrut(10));
        add(lblTitle);
        add(Box.createVerticalStrut(15));
        add(lblDesc);
        
        add(Box.createVerticalGlue()); // Dorong gambar ke tengah vertikal
        add(imageLabel);
        add(Box.createVerticalGlue()); // Dorong tombol ke bawah
        
        add(btnHome);
        add(Box.createVerticalStrut(20)); // Spasi paling bawah
    }

    private JPanel createButton(String text) {
        // Container tombol rounded warna Pink
        RoundedPanel panel = new RoundedPanel(25, GUIUtils.COLOR_PRIMARY);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55)); // Tinggi tombol
        panel.setPreferredSize(new Dimension(300, 55));
        panel.setLayout(new GridBagLayout()); // Center text
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setForeground(Color.WHITE);
        
        panel.add(label);
        return panel;
    }
}