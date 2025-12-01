package com.tubes.kantincepat.client.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.tubes.kantincepat.client.ClientApp;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Menu_Bag extends JPanel {

    private ClientApp mainApp;
    private JPanel contentList;
    private JLabel lblTotalNominal;
    private JTextArea notesArea;

    public Menu_Bag(ClientApp app) {
        this.mainApp = app;
        setLayout(new BorderLayout());
        setBackground(GUIUtils.COLOR_BG2);

        // 1. Header
        add(createBagHeader(), BorderLayout.NORTH);

        // 2. Setup Content List (Wadah Item)
        contentList = new JPanel();
        contentList.setLayout(new BoxLayout(contentList, BoxLayout.Y_AXIS));
        contentList.setBackground(GUIUtils.COLOR_BG2);
        contentList.setBorder(new EmptyBorder(10, 20, 10, 20));

        // 3. Scroll Pane
        JScrollPane scrollPane = new JScrollPane(contentList);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // 4. Footer
        add(createBagFooter(), BorderLayout.SOUTH);

        // 5. Load Data Pertama Kali
        refreshCartData();
    }

    // --- METHOD UPDATE DATA KERANJANG ---
    public void refreshCartData() {
        contentList.removeAll();
        long totalPrice = 0;

        // PERBAIKAN: Menggunakan getCartItems() (Getter)
        if (mainApp.getCartItems().isEmpty()) {
            JLabel emptyLabel = new JLabel("Keranjang masih kosong");
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyLabel.setFont(GUIUtils.getCustomFont("Lato-Regular.ttf", 12f));
            contentList.add(Box.createVerticalStrut(50));
            contentList.add(emptyLabel);
        } else {
            // Grouping logic (Item yang sama disatukan jumlahnya)
            Map<String, Integer> qtyMap = new HashMap<>();
            Map<String, MenuItem> itemMap = new HashMap<>();

            for (MenuItem item : mainApp.getCartItems()) {
                qtyMap.put(item.name, qtyMap.getOrDefault(item.name, 0) + 1);
                itemMap.putIfAbsent(item.name, item);
                totalPrice += item.price;
            }

            for (String itemName : qtyMap.keySet()) {
                int qty = qtyMap.get(itemName);
                MenuItem item = itemMap.get(itemName);
                contentList.add(createCartItem(item, qty));
                contentList.add(Box.createVerticalStrut(15));
            }

            // Area Catatan
            contentList.add(Box.createVerticalStrut(10));
            contentList.add(createNoteArea());
            contentList.add(Box.createVerticalStrut(20));
        }

        // Update Total Harga di Bawah
        if (lblTotalNominal != null) {
            lblTotalNominal.setText("Rp. " + String.format("%,d", totalPrice).replace(',', '.'));
        }

        contentList.revalidate();
        contentList.repaint();
    }

    // --- LOGIKA TAMPILAN PER ITEM ---
    private JPanel createCartItem(MenuItem item, int qty) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(GUIUtils.COLOR_BG2);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        // Gambar Menu
        RoundedPanel imgContainer = new RoundedPanel(20, GUIUtils.COLOR_ACCENT);
        imgContainer.setPreferredSize(new Dimension(80, 80));
        imgContainer.setLayout(new GridBagLayout());
        
        // Load Gambar Aman (Anti Crash)
        ImageIcon icon = GUIUtils.loadImageIcon(item.imagePath, 60, 60);
        if (icon != null) {
            imgContainer.add(new JLabel(icon));
        } else {
            imgContainer.add(new JLabel("IMG"));
        }

        // Info Tengah (Nama & Harga)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(GUIUtils.COLOR_BG2);

        JLabel lblName = new JLabel(item.name);
        lblName.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 14f));
        
        JLabel lblPrice = new JLabel(item.getFormattedPrice());
        lblPrice.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 12f));

        // Panel Qty (+ dan -)
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        qtyPanel.setBackground(GUIUtils.COLOR_BG2);
        qtyPanel.setBorder(new EmptyBorder(5, -5, 0, 0));
        qtyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel btnMinus = createQtyButton("-");
        btnMinus.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { updateItemQuantity(item, -1); }
        });

        JLabel lblQty = new JLabel(String.valueOf(qty));
        lblQty.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 14f));

        JLabel btnPlus = createQtyButton("+");
        btnPlus.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { updateItemQuantity(item, 1); }
        });

        qtyPanel.add(btnMinus);
        qtyPanel.add(lblQty);
        qtyPanel.add(btnPlus);

        centerPanel.add(lblName);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(lblPrice);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(qtyPanel);

        // Tombol Hapus (Silang Merah)
        JLabel btnDelete = new JLabel("⊗");
        btnDelete.setForeground(Color.RED);
        btnDelete.setFont(new Font("SansSerif", Font.PLAIN, 24));
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Hapus semua item dengan nama yang sama
                mainApp.getCartItems().removeIf(i -> i.name.equals(item.name));
                refreshCartData();
            }
        });

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(GUIUtils.COLOR_BG2);
        rightPanel.add(btnDelete, BorderLayout.NORTH);

        panel.add(imgContainer, BorderLayout.WEST);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    // Logic Tambah/Kurang Item
    private void updateItemQuantity(MenuItem itemRef, int delta) {
        if (delta > 0) {
            // Tambah: Masukkan duplikat ke list
            mainApp.getCartItems().add(itemRef);
        } else {
            // Kurang: Cari satu item yg namanya sama, hapus satu aja
            List<MenuItem> cart = mainApp.getCartItems();
            for (int i = 0; i < cart.size(); i++) {
                if (cart.get(i).name.equals(itemRef.name)) {
                    cart.remove(i);
                    break;
                }
            }
        }
        refreshCartData();
    }

    // --- FOOTER (TOTAL & CHECKOUT) ---
    private JPanel createBagFooter() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(GUIUtils.COLOR_BG2);
        panel.setBorder(new EmptyBorder(10, 20, 20, 20));

        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBackground(GUIUtils.COLOR_BG2);
        JLabel lblTotal = new JLabel("Total:");
        lblTotal.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 14f));
        
        lblTotalNominal = new JLabel("Rp. 0");
        lblTotalNominal.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 18f));
        
        totalPanel.add(lblTotal, BorderLayout.WEST);
        totalPanel.add(lblTotalNominal, BorderLayout.EAST);

        RoundedPanel btnCheckout = new RoundedPanel(20, GUIUtils.COLOR_PRIMARY);
        btnCheckout.setPreferredSize(new Dimension(100, 50));
        btnCheckout.setLayout(new GridBagLayout());
        btnCheckout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel lblCheckout = new JLabel("Checkout");
        lblCheckout.setForeground(Color.WHITE);
        lblCheckout.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 16f));
        btnCheckout.add(lblCheckout);

        // AKSI CHECKOUT (PENTING!)
        btnCheckout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (mainApp.getCartItems().isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Keranjang kosong!");
                    return;
                }

                long totalPriceInt = 0;
                for (MenuItem item : mainApp.getCartItems()) totalPriceInt += item.price;
                
                String notesContent = (notesArea != null) ? notesArea.getText() : "-";
                
                // 1. Ambil User ID (Sementara Hardcode dulu kalau login belum simpan ID)
                int currentUserId = 1; 
                if (mainApp.getCurrentUser() != null) {
                    currentUserId = mainApp.getCurrentUser().getId();
                }

                // 2. Panggil OrderServices (Lewat Socket)
                int newOrderId = OrderServices.saveOrder(currentUserId, totalPriceInt, notesContent, mainApp.getCartItems());

                // 3. Cek Hasil
                if (newOrderId != -1) {
                    JOptionPane.showMessageDialog(null, "Order Berhasil! ID Pesanan: " + newOrderId);
                    
                    // Reset Keranjang & Notes
                    mainApp.getCartItems().clear();
                    if(notesArea != null) notesArea.setText("");
                    refreshCartData();
                    
                    // Kembali ke Home
                    mainApp.showView("HOME");
                } else {
                    JOptionPane.showMessageDialog(null, "Gagal Order. Cek koneksi server.");
                }
            }
        });

        panel.add(totalPanel, BorderLayout.NORTH);
        panel.add(btnCheckout, BorderLayout.CENTER);
        return panel;
    }

    // --- HELPER METHODS (KOSMETIK UI) ---
    private JLabel createQtyButton(String text) {
        JLabel btn = new JLabel(text, SwingConstants.CENTER);
        btn.setPreferredSize(new Dimension(24, 24));
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setForeground(GUIUtils.COLOR_PRIMARY);
        btn.setBorder(BorderFactory.createLineBorder(GUIUtils.COLOR_PRIMARY, 1, true));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JPanel createBagHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(GUIUtils.COLOR_BG2);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel btnBack = new JLabel("←");
        btnBack.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 24f));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) { mainApp.showView("HOME"); }
        });

        JLabel title = new JLabel("My Bag", SwingConstants.CENTER);
        title.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 18f));
        
        panel.add(btnBack, BorderLayout.WEST);
        panel.add(title, BorderLayout.CENTER);
        panel.add(new JLabel("   "), BorderLayout.EAST);
        return panel;
    }

    private JPanel createNoteArea() {
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(GUIUtils.COLOR_BG2);
        mainContainer.setBorder(new EmptyBorder(0, 5, 0, 0)); 
        mainContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120)); 

        JLabel lblNotes = new JLabel("Notes");
        lblNotes.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 14f));
        JPanel labelWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        labelWrap.setBackground(GUIUtils.COLOR_BG2);
        labelWrap.add(lblNotes);

        RoundedPanel roundedContainer = new RoundedPanel(20, Color.WHITE);
        roundedContainer.setLayout(new BorderLayout());
        roundedContainer.setPreferredSize(new Dimension(100, 80)); 
        roundedContainer.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Garis pinggir

        notesArea = new JTextArea();
        notesArea.setFont(GUIUtils.getCustomFont("Lato-Regular.ttf", 12f));
        notesArea.setBorder(new EmptyBorder(10, 15, 10, 15)); 
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setOpaque(false);
        roundedContainer.add(notesArea, BorderLayout.CENTER);

        mainContainer.add(labelWrap);
        mainContainer.add(Box.createVerticalStrut(8));
        mainContainer.add(roundedContainer);
        return mainContainer;
    }
}