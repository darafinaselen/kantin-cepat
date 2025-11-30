package com.tubes.kantincepat.client.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.tubes.kantincepat.client.ClientApp;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
// import java.util.Collections;
import java.util.List;

public class Menu_Orders extends JPanel {

    private ClientApp ClientApp;
    private JPanel contentList;
    
    public Menu_Orders(ClientApp app) {
        this.ClientApp = app;
        setLayout(new BorderLayout());
        setBackground(GUIUtils.COLOR_BG2);

        add(createHeader(), BorderLayout.NORTH);

        // Setup Content List
        contentList = new JPanel();
        contentList.setLayout(new BoxLayout(contentList, BoxLayout.Y_AXIS));
        contentList.setBackground(GUIUtils.COLOR_BG2);
        contentList.setBorder(new EmptyBorder(10, 20, 10, 20));

        JScrollPane scrollPane = new JScrollPane(contentList);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Load data awal
        refreshOrderData();
    }

    public void refreshOrderData() {
        contentList.removeAll();

        List<Order> dbOrders = OrderServices.getOrdersByCustomer(3); 
        ClientApp.orderHistory = dbOrders; 

        if (dbOrders.isEmpty()) {
            JLabel emptyLabel = new JLabel("Belum ada riwayat pesanan");
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentList.add(Box.createVerticalStrut(50));
            contentList.add(emptyLabel);
        } else {
            for (Order order : dbOrders) {
                contentList.add(createOrderCard(order)); 
                contentList.add(Box.createVerticalStrut(20));
            }
        }

        contentList.revalidate();
        contentList.repaint();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(GUIUtils.COLOR_BG2);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));
        panel.setPreferredSize(new Dimension(getWidth(), 60));

        JLabel title = new JLabel("My Orders", SwingConstants.CENTER);
        title.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 18f));
        
        panel.add(title, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createOrderCard(Order order) {
        // Container Kartu
        RoundedPanel card = new RoundedPanel(20, GUIUtils.COLOR_ACCENT);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(15, 15, 15, 15));
        card.setMaximumSize(new Dimension(300, 160)); 
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));

        // --- BAGIAN ATAS: TANGGAL & STATUS ---
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        
        JLabel lblDate = new JLabel(order.date); 
        lblDate.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblDate.setForeground(Color.GRAY);

        JLabel lblStatus = createStatusBadge(order.status);

        topRow.add(lblDate, BorderLayout.WEST);
        topRow.add(lblStatus, BorderLayout.EAST);

        // --- BAGIAN TENGAH: ITEM & HARGA ---
        JPanel midPanel = new JPanel();
        midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));
        midPanel.setOpaque(false);
        midPanel.setBorder(new EmptyBorder(5, 0, 5, 0));

        String fullText = order.itemsSummary;
        String displayText = fullText;

        if (fullText.length() > 35) {
            displayText = fullText.substring(0, 32) + "...";
        }

        JLabel lblItems = new JLabel(displayText);
        lblItems.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 14f));
        lblItems.setToolTipText(fullText);
        
        JLabel lblPrice = new JLabel(order.totalPrice);
        lblPrice.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblPrice.setForeground(GUIUtils.COLOR_PRIMARY);

        midPanel.add(lblItems);
        midPanel.add(Box.createVerticalStrut(3));
        midPanel.add(lblPrice);

        // --- BAGIAN BAWAH: TOMBOL ACTION ---
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottomRow.setOpaque(false);
        bottomRow.setBorder(new EmptyBorder(5, 0, 0, 0));

        // Tombol 1: Invoice (Outline Style)
        JLabel btnInvoice = createActionButton("Invoice", false);

        btnInvoice.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Panggil method di ClientApp sambil membawa data 'order' ini
                ClientApp.showInvoice(order);
            }
        });
        
        // Tombol 2: Re-order (Solid Style)
        JLabel btnReorder = createActionButton("Re-order", true);

        // --- LOGIKA RE-ORDER (VERSI AMAN) ---
        btnReorder.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Tombol Reorder ditekan..."); // Cek di console

                // 1. Cek apakah ada data barang yang disimpan?
                if (order.savedItems == null || order.savedItems.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Maaf, data menu pada riwayat ini tidak lengkap.");
                    return;
                }

                try {
                    // 2. Kosongkan keranjang saat ini
                    ClientApp.cartItems.clear();

                    // 3. Masukkan barang dari riwayat ke keranjang utama
                    ClientApp.cartItems.addAll(order.savedItems);
                    
                    System.out.println("Berhasil menyalin " + order.savedItems.size() + " item ke keranjang.");

                    // 4. Pindah ke halaman Keranjang
                    // Pastikan tulisan "BAG" sesuai dengan yang ada di ClientApp (huruf besar/kecil berpengaruh!)
                    ClientApp.showView("BAG"); 
                    
                } catch (Exception ex) {
                    ex.printStackTrace(); // Lihat error di console jika ada
                    JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat Re-order.");
                }
            }
        });

        bottomRow.add(btnInvoice);
        bottomRow.add(btnReorder);

        // Susun ke Card
        card.add(topRow, BorderLayout.NORTH);
        card.add(midPanel, BorderLayout.CENTER);
        card.add(bottomRow, BorderLayout.SOUTH);

        return card;
    }

    // Helper untuk membuat Badge Status berwarna-warni
    private JLabel createStatusBadge(String status) {
        JLabel lbl = new JLabel(status, SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 10));
        lbl.setOpaque(true); // Agar background warna muncul
        lbl.setBorder(new EmptyBorder(5, 10, 5, 10)); // Padding dalam badge

        // Logika Warna berdasarkan Status
        if (status.equalsIgnoreCase("Selesai")) {
            lbl.setBackground(new Color(220, 255, 220)); // Hijau Muda
            lbl.setForeground(new Color(0, 100, 0));     // Hijau Tua
        } else if (status.equalsIgnoreCase("Dibatalkan")) {
            lbl.setBackground(new Color(255, 220, 220)); // Merah Muda
            lbl.setForeground(Color.RED);                // Merah
        } else {
            // Sedang Disiapkan / Lainnya
            lbl.setBackground(new Color(255, 245, 200)); // Kuning Muda
            lbl.setForeground(new Color(200, 100, 0));   // Oranye
        }

        return lbl;
    }

    // Helper untuk membuat Tombol Kecil (Invoice / Re-order)
    private JLabel createActionButton(String text, boolean isPrimary) {
        JLabel btn = new JLabel(text, SwingConstants.CENTER);
        btn.setPreferredSize(new Dimension(80, 30));
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);

        if (isPrimary) {
            // Gaya Tombol Utama (Pink Penuh)
            btn.setBackground(GUIUtils.COLOR_PRIMARY);
            btn.setForeground(Color.WHITE);
        } else {
            // Gaya Tombol Sekunder (Putih border abu)
            btn.setBackground(Color.WHITE);
            btn.setForeground(Color.GRAY);
            btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
        }
        return btn;
    }
}
