package com.tubes.kantincepat.client.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.tubes.kantincepat.client.ClientApp;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Menu_Detail extends JPanel {

    private ClientApp ClientApp;
    
    // Komponen Global
    private JLabel lblImageContainer;
    private JLabel lblName;
    private JLabel lblPrice;
    private JTextArea txtDescription;
    private JScrollPane scrollPane;
    
    private MenuItem currentItem;

    public Menu_Detail(ClientApp app) {
        this.ClientApp = app;
        setLayout(new BorderLayout());
        setBackground(GUIUtils.COLOR_BG2);

        // 1. HEADER (Tombol Back)
        add(createHeader(), BorderLayout.NORTH);

        // 2. CONTENT (Scrollable)
        this.scrollPane = new JScrollPane(createContentPanel());
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // 3. BOTTOM ACTION
        add(createBottomActionPanel(), BorderLayout.SOUTH);
    }
    
    // --- METHOD UTAMA: Show Menu ---
    public void showMenu(MenuItem item) {
        this.currentItem = item;

        // Update Text
        lblName.setText(item.name);
        lblPrice.setText(item.getFormattedPrice());
        txtDescription.setText(item.description != null ? item.description : "Deskripsi menu belum tersedia.");

        // Update Image
        lblImageContainer.setIcon(null); 
        try {
            ImageIcon icon = new ImageIcon(item.imagePath);
            int targetWidth = 300; 
            int targetHeight = 200; 
            Image img = icon.getImage().getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            lblImageContainer.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            lblImageContainer.setText("Image not found");
            lblImageContainer.setHorizontalAlignment(SwingConstants.CENTER);
        }
        
        SwingUtilities.invokeLater(() -> {
            if (scrollPane != null) scrollPane.getVerticalScrollBar().setValue(0);
        });
    }

    // ================= HELPER METHODS UNTUK LAYOUT =================

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(GUIUtils.COLOR_BG2);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel btnBack = new JLabel("←");
        btnBack.setFont(new Font("SansSerif", Font.BOLD, 24));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ClientApp.showView("HOME");
            }
        });

        panel.add(btnBack, BorderLayout.WEST);
        return panel;
    }

    // --- INI METHOD UTAMA YANG DIREVISI TOTAL ---
    private JPanel createContentPanel() {
        // Panel Induk (Vertikal Stack)
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(GUIUtils.COLOR_BG2);
        // Padding Kiri-Kanan 20px agar konten tidak mepet layar
        mainPanel.setBorder(new EmptyBorder(0, 20, 30, 20)); 

        // 1. Masukkan Wrapper Gambar
        mainPanel.add(createImageWrapper());
        mainPanel.add(Box.createVerticalStrut(25)); 

        // 2. Masukkan Wrapper Header (Nama & Harga)
        mainPanel.add(createTitlePriceWrapper());
        mainPanel.add(Box.createVerticalStrut(20));

        // 3. Masukkan Wrapper Deskripsi
        mainPanel.add(createDescriptionWrapper());

        return mainPanel;
    }

    // --- WRAPPER 1: GAMBAR ---
    private JPanel createImageWrapper() {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrapper.setBackground(GUIUtils.COLOR_BG2);
        
        // Container Rounded Putih
        RoundedPanel imageContainer = new RoundedPanel(30, Color.WHITE);
        imageContainer.setLayout(new BorderLayout());
        imageContainer.setPreferredSize(new Dimension(300, 200)); 
        imageContainer.setMaximumSize(new Dimension(300, 200));
        
        lblImageContainer = new JLabel();
        imageContainer.add(lblImageContainer, BorderLayout.CENTER);
        
        wrapper.add(imageContainer);
        return wrapper;
    }

    // --- WRAPPER 2: NAMA & HARGA ---
    private JPanel createTitlePriceWrapper() {
        // Gunakan BorderLayout agar Nama di Kiri, Harga di Kanan
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(GUIUtils.COLOR_BG2);
        // Batasi tinggi wrapper agar tidak terlalu tinggi
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40)); 

        lblName = new JLabel("Menu Name");
        lblName.setFont(new Font("SansSerif", Font.BOLD, 22));

        lblPrice = new JLabel("Rp. 0");
        lblPrice.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblPrice.setForeground(GUIUtils.COLOR_PRIMARY);

        wrapper.add(lblName, BorderLayout.WEST);
        wrapper.add(lblPrice, BorderLayout.EAST);
        
        return wrapper;
    }

    // --- WRAPPER 3: DESKRIPSI ---
    private JPanel createDescriptionWrapper() {
        // Gunakan BoxLayout Vertikal untuk Judul + Teks
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(GUIUtils.COLOR_BG2);
        
        // Judul "Description"
        JLabel lblDescTitle = new JLabel("Description");
        lblDescTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        lblDescTitle.setAlignmentX(Component.LEFT_ALIGNMENT); // Rata Kiri
        
        // Isi Deskripsi
        txtDescription = new JTextArea();
        txtDescription.setFont(GUIUtils.getCustomFont("Lato-Regular.ttf", 12f));
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setEditable(false); 
        txtDescription.setOpaque(false);
        txtDescription.setMargin(new Insets(0, 0, 0, 0));
        txtDescription.setAlignmentX(Component.LEFT_ALIGNMENT); // Rata Kiri
        
        // Gabungkan Judul & Teks
        content.add(lblDescTitle);
        content.add(Box.createVerticalStrut(10));
        content.add(txtDescription);

        // Bungkus lagi dengan FlowLayout LEFT agar panel content menempel di kiri layar
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        wrapper.setBackground(GUIUtils.COLOR_BG2);
        // Atur lebar wrapper agar teks bisa wrapping (turun baris) dengan benar
        // Kita set lebar preferensi 300px (sama dengan lebar gambar)
        content.setPreferredSize(new Dimension(300, 200)); 
        
        wrapper.add(content);
        
        return wrapper;
    }

    private JPanel createBottomActionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(GUIUtils.COLOR_BG2);
        panel.setBorder(new EmptyBorder(20, 20, 30, 20)); 

        RoundedPanel btnAddToCart = new RoundedPanel(25, GUIUtils.COLOR_PRIMARY);
        btnAddToCart.setPreferredSize(new Dimension(100, 55));
        btnAddToCart.setLayout(new GridBagLayout());
        btnAddToCart.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblBtnText = new JLabel("Add to Cart");
        lblBtnText.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 18f));
        lblBtnText.setForeground(Color.WHITE);
        btnAddToCart.add(lblBtnText);

        btnAddToCart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (currentItem != null) {
                    ClientApp.addToCart(currentItem);
                    JOptionPane.showMessageDialog(null, currentItem.name + " berhasil ditambahkan ke keranjang!");
                }
            }
        });

        panel.add(btnAddToCart, BorderLayout.CENTER);
        return panel;
    }
}
