package com.tubes.kantincepat.client.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.tubes.kantincepat.client.ClientApp;
import com.tubes.kantincepat.client.view.GUIUtils;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class Menu_Bag extends JPanel {

    private ClientApp mainApp;
    private JPanel contentList; // Variabel Global
    private JLabel lblTotalNominal; // Variabel Global untuk Total Harga
    private JTextArea notesArea; // Variabel Global untuk Notes

    public Menu_Bag(ClientApp app) {
        this.mainApp = app;
        setLayout(new BorderLayout());
        setBackground(ClientApp.COLOR_BG);

        // 1. Header
        add(createBagHeader(), BorderLayout.NORTH);

        // 2. Setup Content List (Wadah Item)
        // PERBAIKAN: Jangan pakai 'JPanel contentList = ...', langsung 'contentList = ...'
        contentList = new JPanel(); 
        contentList.setLayout(new BoxLayout(contentList, BoxLayout.Y_AXIS));
        contentList.setBackground(ClientApp.COLOR_BG);
        contentList.setBorder(new EmptyBorder(10, 20, 10, 20));

        // PERBAIKAN: Hapus kode hardcoded (add createCartItem manual) disini.
        // Biarkan refreshCartData() yang mengisi isinya nanti.

        // 3. Scroll Pane
        JScrollPane scrollPane = new JScrollPane(contentList);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER); // Hilangkan scroll horizontal
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // 4. Footer
        add(createBagFooter(), BorderLayout.SOUTH);

        // 5. Load Data Pertama Kali
        refreshCartData();
    }

    // --- METHOD UPDATE DATA KERANJANG ---
    public void refreshCartData() {
        // 1. Bersihkan tampilan lama
        contentList.removeAll();
        
        long totalPrice = 0;

        // 2. Cek apakah ada barang di keranjang MainApp
        if (mainApp.cartItems.isEmpty()) {
            JLabel emptyLabel = new JLabel("Keranjang masih kosong");
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            emptyLabel.setFont(ClientApp.FONT_PLAIN);
            contentList.add(Box.createVerticalStrut(50)); // Spasi atas
            contentList.add(emptyLabel);
        } else {
            // --- 1. LOGIKA GROUPING (PENGELOMPOKAN) ---
            // Map untuk menyimpan: Nama Menu -> Jumlah (Qty)
            Map<String, Integer> qtyMap = new HashMap<>();
            // Map untuk menyimpan: Nama Menu -> Object MenuItem (untuk data gambar/harga)
            Map<String, MenuItem> itemMap = new HashMap<>();

            // Loop semua item di keranjang untuk dihitung
            for (MenuItem item : mainApp.cartItems) {
                // Hitung Qty
                qtyMap.put(item.name, qtyMap.getOrDefault(item.name, 0) + 1);
                // Simpan referensi item (biar kita tau harga/gambarnya nanti)
                itemMap.putIfAbsent(item.name, item);
                totalPrice += item.rawPrice;
            }

            // --- 2. TAMPILKAN ITEM YANG SUDAH DI-GROUP ---
            for (String itemName : qtyMap.keySet()) {
                int qty = qtyMap.get(itemName);
                MenuItem item = itemMap.get(itemName);

                // HAPUS parameter 'index' di belakang
                contentList.add(createCartItem(item, qty)); 
                
                contentList.add(Box.createVerticalStrut(15));
            }

            // Tambahkan Notes Area di paling bawah (hanya jika ada item)
            contentList.add(Box.createVerticalStrut(10));
            contentList.add(createNoteArea());
            contentList.add(Box.createVerticalStrut(20));
        }

        // 4. Update Label Total Harga di Footer
        if (lblTotalNominal != null) {
            lblTotalNominal.setText("Rp. " + String.format("%,d", totalPrice).replace(',', '.'));
        }

        // 5. Refresh UI agar perubahan terlihat
        contentList.revalidate();
        contentList.repaint();
    }

    // Method diubah parameternya menerima (MenuItem item, int qty)
    private JPanel createCartItem(MenuItem item, int qty) {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setBackground(ClientApp.COLOR_BG);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        // Image
        JPanel imgContainer = new RoundedPanel(20, mainApp.COLOR_ACCENT);
        imgContainer.setPreferredSize(new Dimension(80, 80));
        imgContainer.setLayout(new GridBagLayout());
        try {
            ImageIcon icon = new ImageIcon(item.imagePath);
            Image img = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            imgContainer.add(new JLabel(new ImageIcon(img)));
        } catch (Exception e) {
            imgContainer.add(new JLabel("IMG"));
        }

        // Center Info
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(ClientApp.COLOR_BG);

        JLabel lblName = new JLabel(item.name);
        lblName.setFont(ClientApp.FONT_BOLD);
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblPrice = new JLabel(item.getFormattedPrice());
        lblPrice.setFont(ClientApp.FONT_PLAIN);
        lblPrice.setAlignmentX(Component.LEFT_ALIGNMENT);

        // --- PANEL QUANTITY DENGAN TOMBOL AKTIF ---
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        qtyPanel.setBackground(ClientApp.COLOR_BG);
        qtyPanel.setBorder(new EmptyBorder(5, -5, 0, 0));
        qtyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Tombol MINUS
        JLabel btnMinus = createQtyButton("-");
        btnMinus.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMinus.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                updateItemQuantity(item, -1); // Kurangi 1
            }
        });

        // Label Angka Qty
        JLabel lblQty = new JLabel(String.valueOf(qty));
        lblQty.setFont(ClientApp.FONT_BOLD);

        // Tombol PLUS
        JLabel btnPlus = createQtyButton("+");
        btnPlus.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPlus.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                updateItemQuantity(item, 1); // Tambah 1
            }
        });

        qtyPanel.add(btnMinus);
        qtyPanel.add(lblQty);
        qtyPanel.add(btnPlus);
        // ------------------------------------------

        centerPanel.add(lblName);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(lblPrice);
        centerPanel.add(Box.createVerticalStrut(5));
        centerPanel.add(qtyPanel);

        // Tombol Hapus (Silang) - Menghapus SEMUA item dengan nama ini
        JLabel btnDelete = new JLabel("⊗");
        btnDelete.setForeground(Color.RED);
        btnDelete.setFont(new Font("SansSerif", Font.PLAIN, 24));
        btnDelete.setVerticalAlignment(SwingConstants.TOP);
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Hapus semua yang namanya sama
                mainApp.cartItems.removeIf(i -> i.name.equals(item.name));
                refreshCartData();
            }
        });

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(ClientApp.COLOR_BG);
        rightPanel.add(btnDelete, BorderLayout.NORTH);

        panel.add(imgContainer, BorderLayout.WEST);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);
        return panel;
    }

    // Method untuk mengubah jumlah item di List utama
    private void updateItemQuantity(MenuItem itemRef, int delta) {
        if (delta > 0) {
            // TAMBAH: Masukkan satu copy lagi ke list
            mainApp.cartItems.add(itemRef);
        } else {
            // KURANG: Cari item dengan nama yang sama, hapus satu saja
            for (int i = 0; i < mainApp.cartItems.size(); i++) {
                if (mainApp.cartItems.get(i).name.equals(itemRef.name)) {
                    mainApp.cartItems.remove(i);
                    break; // Hapus satu saja, lalu berhenti
                }
            }
        }
        // Refresh tampilan agar angka qty dan total harga berubah
        refreshCartData();
    }

    private JPanel createBagFooter() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(ClientApp.COLOR_BG);
        panel.setBorder(new EmptyBorder(10, 20, 20, 20));

        JPanel totalPanel = new JPanel(new BorderLayout());
        totalPanel.setBackground(ClientApp.COLOR_BG);
        JLabel lblTotal = new JLabel("Total:");
        lblTotal.setFont(ClientApp.FONT_BOLD);
        
        lblTotalNominal = new JLabel("Rp. 0");
        lblTotalNominal.setFont(new Font("SansSerif", Font.BOLD, 18));
        
        totalPanel.add(lblTotal, BorderLayout.WEST);
        totalPanel.add(lblTotalNominal, BorderLayout.EAST);

        RoundedPanel btnCheckout = new RoundedPanel(20, ClientApp.COLOR_PRIMARY);
        btnCheckout.setPreferredSize(new Dimension(100, 50));
        btnCheckout.setLayout(new GridBagLayout());
        btnCheckout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        JLabel lblCheckout = new JLabel("Checkout");
        lblCheckout.setForeground(Color.WHITE);
        lblCheckout.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnCheckout.add(lblCheckout);

        // --- LOGIKA TOMBOL CHECKOUT (FULL CODE) ---
        btnCheckout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 1. CEK KERANJANG KOSONG
                if (mainApp.cartItems.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Keranjang kosong! Silakan pilih menu dulu.");
                    return;
                }

                long totalPriceInt = 0;
                for (MenuItem item : mainApp.cartItems) {
                    totalPriceInt += item.rawPrice;}
                int customerId = 3; 
                
                String notesContent = (notesArea != null) ? notesArea.getText() : "-";
                    if (notesContent.trim().isEmpty()) notesContent = "-";
                int newOrderId = OrderServices.saveOrder(customerId, totalPriceInt, notesContent, mainApp.cartItems);
                // 4. CEK HASIL PENYIMPANAN
                if (newOrderId != -1) {
                    // --- JIKA SUKSES DISIMPAN KE DB ---
                    System.out.println("Order sukses disimpan. ID: " + newOrderId);

                    // A. Siapkan Data untuk Tampilan UI (History & Invoice)
                    String currentDate = new SimpleDateFormat("dd MMM yyyy, HH:mm").format(new Date());
                    
                    // Format Total Rupiah (misal "Rp. 50.000")
                    NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
                    String formattedTotal = format.format(totalPriceInt).replace(",00", "");

                    // Buat String Ringkasan Item (Grouping)
                    Map<String, Integer> qtyMap = new HashMap<>();
                    for (MenuItem item : mainApp.cartItems) {
                        qtyMap.put(item.name, qtyMap.getOrDefault(item.name, 0) + 1);
                    }
                    
                    StringBuilder summaryBuilder = new StringBuilder();
                    int count = 0;
                    for (Map.Entry<String, Integer> entry : qtyMap.entrySet()) {
                        if (count > 0) summaryBuilder.append(", ");
                        summaryBuilder.append(entry.getKey()).append(" (x").append(entry.getValue()).append(")");
                        count++;
                    }
                    String itemsSummary = summaryBuilder.toString();

                    // Ambil Notes dari TextArea
                    
                    // B. Buat Objek Order Lokal
                    // Kita buat salinan list (new ArrayList) agar tidak ikut terhapus saat cart di-clear
                    Order newLocalOrder = new Order(
                            currentDate, 
                            itemsSummary, 
                            formattedTotal, 
                            "Diterima",
                            notesContent,
                            new ArrayList<>(mainApp.cartItems)
                    );
                    
                    // C. Update ID Order Lokal dengan ID Asli dari Database
                    // Agar di Invoice nanti No. Pesaannya sesuai dengan Database
                    newLocalOrder.orderId = String.valueOf(newOrderId);

                    // D. Masukkan ke Riwayat Lokal Aplikasi
                    mainApp.orderHistory.add(newLocalOrder);

                    // E. Bersihkan Keranjang & Refresh UI
                    mainApp.cartItems.clear();
                    if (notesArea != null) notesArea.setText(""); // Reset notes
                    refreshCartData(); // Keranjang jadi kosong di layar

                    // F. Pindah ke Halaman Sukses
                    mainApp.showView("SUCCESS");

                } else {
                    // --- JIKA GAGAL ---
                    JOptionPane.showMessageDialog(null, 
                        "Gagal menyimpan pesanan ke database.\nCek koneksi internet atau server.", 
                        "Error Transaksi", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        
    });

        panel.add(totalPanel, BorderLayout.NORTH);
        panel.add(btnCheckout, BorderLayout.CENTER);
        return panel;
    }

    // --- Helper Methods Lainnya (Qty, Header, Note) TETAP SAMA ---
    
    private JLabel createQtyButton(String text) {
        JLabel btn = new JLabel(text, SwingConstants.CENTER);
        btn.setPreferredSize(new Dimension(24, 24));
        btn.setFont(new Font("SansSerif", Font.BOLD, 16));
        btn.setForeground(mainApp.COLOR_PRIMARY);
        btn.setBorder(BorderFactory.createLineBorder(mainApp.COLOR_PRIMARY, 1, true));
        return btn;
    }

    private JPanel createBagHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(mainApp.COLOR_BG);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel btnBack = new JLabel("←");
        btnBack.setFont(new Font("SansSerif", Font.BOLD, 24));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainApp.showView("HOME");
            }
        });

        JLabel title = new JLabel("My Bag", SwingConstants.CENTER);
        title.setFont(mainApp.FONT_TITLE);
        
        panel.add(btnBack, BorderLayout.WEST);
        panel.add(title, BorderLayout.CENTER);
        panel.add(new JLabel("   "), BorderLayout.EAST);
        return panel;
    }

    private JPanel createNoteArea() {
        // 1. Panel Induk (Membungkus Label + Kotak)
        JPanel mainContainer = new JPanel();
        mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
        mainContainer.setBackground(mainApp.COLOR_BG);
        // Beri margin kiri sedikit agar sejajar dengan list menu
        mainContainer.setBorder(new EmptyBorder(0, 5, 0, 0)); 
        mainContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120)); // Batasi tinggi total

        // 2. Buat Label "Notes"
        JLabel lblNotes = new JLabel("Notes");
        lblNotes.setFont(mainApp.FONT_BOLD);
        
        // Wrapper Label (Agar rata kiri)
        JPanel labelWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        labelWrap.setBackground(mainApp.COLOR_BG);
        labelWrap.add(lblNotes);

        // 3. Buat Kotak Input (Rounded Logic)
        JPanel roundedContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.setColor(Color.BLACK);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
            }
        };
        roundedContainer.setLayout(new BorderLayout());
        roundedContainer.setOpaque(false);
        // Tinggi kotak input
        roundedContainer.setPreferredSize(new Dimension(100, 80)); 

        notesArea = new JTextArea();
        notesArea.setFont(mainApp.FONT_PLAIN);
        notesArea.setBorder(new EmptyBorder(10, 15, 10, 15)); 
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        notesArea.setOpaque(false);

        roundedContainer.add(notesArea, BorderLayout.CENTER);

        // 4. Susun ke Panel Induk (Label di atas, Kotak di bawah)
        mainContainer.add(labelWrap);
        mainContainer.add(Box.createVerticalStrut(8)); // Jarak dekat (8px) antara teks Notes dan Kotak
        mainContainer.add(roundedContainer);
        
        return mainContainer;
    }
}
