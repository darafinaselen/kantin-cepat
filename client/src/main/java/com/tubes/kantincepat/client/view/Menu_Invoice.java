package com.tubes.kantincepat.client.view;
import javax.swing.*;
import javax.swing.border.*;

import com.tubes.kantincepat.client.ClientApp;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class Menu_Invoice extends JPanel {

    private ClientApp ClientApp;
    
    // --- KOMPONEN GLOBAL (Agar bisa di-update datanya) ---
    private JLabel lblOrderNo;
    private JLabel lblDate;
    private JPanel contentPanel; // Panel utama di dalam scroll
    private JPanel tablePanel;   // Panel tabel (akan dibuat ulang tiap update)

    // --- KONFIGURASI TEMA ---
    private static final Color BG_COLOR = new Color(248, 240, 242); // Pink muda
    private static final Color TABLE_BORDER_COLOR = new Color(200, 180, 190); 
    private static final Color NOTES_BG_COLOR = new Color(220, 220, 220); 
    private static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 20);
    private static final Font FONT_SECTION_TITLE = new Font("SansSerif", Font.BOLD, 16);
    private static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 14);
    private static final Font FONT_PLAIN = new Font("SansSerif", Font.PLAIN, 14);
    private JTextArea txtDisplayNotes;

    public Menu_Invoice(ClientApp app) {
        this.ClientApp = app;
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        // 1. Top Bar (Back Button & Title)
        add(createTopBar(), BorderLayout.NORTH);

        // 2. Main Content
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BG_COLOR);
        contentPanel.setBorder(new EmptyBorder(20, 20, 30, 20));

        // --- RANGKA TAMPILAN AWAL ---
        
        // A. Judul Section 1
        // contentPanel.add(createSectionTitle("Order Summary"));
        // contentPanel.add(Box.createVerticalStrut(10));
        
        // B. Tabel Summary (Awalnya kosong/null)
        tablePanel = createSummaryTable(null); 
        contentPanel.add(tablePanel); // Index komponen: 2
        
        contentPanel.add(Box.createVerticalStrut(30));

        // C. Judul Section 2
        // contentPanel.add(createSectionTitle("Informasi Pesanan"));
        // contentPanel.add(Box.createVerticalStrut(15));
        
        // D. Info Pesanan (No. Pesanan, Tgl, Notes)
        contentPanel.add(createOrderInfoSection());
        contentPanel.add(Box.createVerticalStrut(40));

        // E. Footer
        // contentPanel.add(createFooterSection());

        // Wrap ScrollPane
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);
    }

    // =================================================================================
    // METHOD UTAMA: UPDATE DATA (Dipanggil dari ClientApp)
    // =================================================================================
    public void setOrderData(Order order) {
        if (order == null) return;

        if (lblOrderNo != null) lblOrderNo.setText(order.orderId);
        if (lblDate != null) lblDate.setText(order.date);

        if (txtDisplayNotes != null) {
            txtDisplayNotes.setText(order.notes);
        }
        // Update Tabel
        contentPanel.remove(tablePanel);
        
        tablePanel = createSummaryTable(order); // Ini mengembalikan Wrapper baru
        
        contentPanel.add(tablePanel, 0); 

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // =================================================================================
    // HELPER METHODS (UI COMPONENTS)
    // =================================================================================

    private JPanel createTopBar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel btnBack = new JLabel("←");
        btnBack.setFont(new Font("SansSerif", Font.BOLD, 24));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ClientApp.showView("ORDERS"); // Kembali ke riwayat
            }
        });

        JLabel title = new JLabel("Invoice Order");
        title.setFont(FONT_TITLE);
        title.setBorder(new EmptyBorder(0, 15, 0, 0));

        panel.add(btnBack, BorderLayout.WEST);
        panel.add(title, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createSectionTitle(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_SECTION_TITLE);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    // --- TABEL SUMMARY (GRID BAG LAYOUT) ---
    private JPanel createSummaryTable(Order order) {
        JPanel tablePanel = new JPanel(new GridBagLayout());
        tablePanel.setBackground(BG_COLOR);
        // tablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        tablePanel.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
        // tablePanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, tablePanel.getPreferredSize().height));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;

        // 1. Header Row
        gbc.gridy = 0;
        addTableCell(tablePanel, gbc, 0, "Menu", true, false, true, true, true, true, 0.55);
        addTableCell(tablePanel, gbc, 1, "Jumlah", true, true, true, false, true, true,0.15);
        addTableCell(tablePanel, gbc, 2, "Harga", true, true, true, false, true, true, 0.15);
        
        // 2. Data Rows
        gbc.gridy = 1;
        String totalPriceStr = "Rp. 0";

        if (order != null && order.savedItems != null) {
            // LOGIKA GROUPING: Hitung jumlah item yang sama
            Map<String, Integer> qtyMap = new HashMap<>();
            Map<String, String> priceMap = new HashMap<>();

            for (MenuItem item : order.savedItems) {
                qtyMap.put(item.name, qtyMap.getOrDefault(item.name, 0) + 1);
                priceMap.putIfAbsent(item.name, item.getFormattedPrice());
            }

            // Loop map untuk menampilkan baris
            for (Map.Entry<String, Integer> entry : qtyMap.entrySet()) {
                String itemName = entry.getKey();
                int qty = entry.getValue();
                String price = priceMap.get(itemName);

                addTableCell(tablePanel, gbc, 0, itemName, false, false, false, true, true, true, 0.6);
                addTableCell(tablePanel, gbc, 1, qty + " x", false, true, false, false, true, true, 0.15);
                addTableCell(tablePanel, gbc, 2, price, false, true, false, false, true, true, 0.15);
                gbc.gridy++;
            }
            totalPriceStr = order.totalPrice;
        } else {
            // Dummy row agar layout tidak gepeng saat kosong
            addTableCell(tablePanel, gbc, 0, "-", false, false, false, true, true, true, 0.6);
            addTableCell(tablePanel, gbc, 1, "-", false, true, false, false, true, true, 0.15);
            addTableCell(tablePanel, gbc, 2, "-", false, true, false, false, true, true, 0.15);
            gbc.gridy++;
        }

        // 3. Total Row
        gbc.gridx = 0;
        gbc.gridwidth = 2; // Span 2 kolom
        JLabel lblTotalLabel = new JLabel("TOTAL");
        lblTotalLabel.setFont(FONT_BOLD);
        lblTotalLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 1, 0, TABLE_BORDER_COLOR),
                new EmptyBorder(10, 10, 10, 10)
        ));
        tablePanel.add(lblTotalLabel, gbc);

        gbc.gridx = 2;
        gbc.gridwidth = 1;
        JLabel lblTotalValue = new JLabel(totalPriceStr);
        lblTotalValue.setFont(FONT_BOLD);
        lblTotalValue.setHorizontalAlignment(SwingConstants.RIGHT);
        lblTotalValue.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 1, 1, 1, TABLE_BORDER_COLOR),
                new EmptyBorder(10, 10, 10, 10)
        ));
        tablePanel.add(lblTotalValue, gbc);
        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(BG_COLOR);
        wrapperPanel.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));
        // Harus dibungkus dengan "new Dimension(...)"
        // wrapperPanel.setPreferredSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        
        JLabel lblTitle = createSectionTitle("Order Summary");
        // Beri jarak sedikit di bawah judul (Margin Bawah 10px)
        lblTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        // 2. Masukkan Judul ke ATAS (North)
        wrapperPanel.add(lblTitle, BorderLayout.NORTH);
        
        // 3. Masukkan Tabel ke TENGAH (Center)
        wrapperPanel.add(tablePanel, BorderLayout.CENTER);

        return wrapperPanel;
    }

    private void addTableCell(JPanel panel, GridBagConstraints gbc, int col, String text, 
                              boolean isHeader, boolean alignRight, boolean top, boolean left, boolean bottom, boolean right, double weight) {
        gbc.gridx = col;
        gbc.weightx = weight;

        JLabel lbl = new JLabel(text);
        lbl.setFont(isHeader ? FONT_BOLD : FONT_PLAIN);
        if (alignRight) lbl.setHorizontalAlignment(SwingConstants.RIGHT);

        // Custom Border
        MatteBorder border = BorderFactory.createMatteBorder(
                top ? 1 : 0, left ? 1 : 0, bottom ? 1 : 0, right ? 1 : 0, TABLE_BORDER_COLOR);
        
        lbl.setBorder(BorderFactory.createCompoundBorder(border, new EmptyBorder(8, 10, 8, 10)));
        
        // Agar kolom Item lebih lebar
        gbc.weightx = (col == 0) ? 1.0 : 0.0; 
        
        panel.add(lbl, gbc);
    }

    // --- INFO SECTION ---
    private JPanel createOrderInfoSection() {
        // 1. Panel Inti (Info Section)
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBackground(BG_COLOR);
        
        infoPanel.setPreferredSize(new Dimension(300, 200)); 
        infoPanel.setMaximumSize(new Dimension(300, Integer.MAX_VALUE));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; // Isi penuh lebar 300px
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.insets = new Insets(5, 0, 5, 0); // Margin antar baris

        // Inisialisasi Label Global
        lblOrderNo = createLabel("-", true);
        lblDate = createLabel("-", true);

        // --- Row 1: No Pesanan ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        infoPanel.add(createLabel("No. Pesanan", false), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.NORTHEAST;
        infoPanel.add(lblOrderNo, gbc);

        // --- Row 2: Tanggal ---
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; gbc.anchor = GridBagConstraints.NORTHWEST;
        infoPanel.add(createLabel("Waktu Pesan", false), gbc);

        gbc.gridx = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.NORTHEAST;
        infoPanel.add(lblDate, gbc);

        // --- Row 3: Notes Label ---
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2; gbc.insets = new Insets(15, 0, 5, 0);
        infoPanel.add(createLabel("Notes", false), gbc);

        // --- Row 4: Notes Box (Rounded Gray) ---
        gbc.gridy = 3;
        JPanel notesBox = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(NOTES_BG_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        notesBox.setLayout(new BorderLayout());
        notesBox.setOpaque(false);
        notesBox.setBorder(new EmptyBorder(15, 15, 15, 15));

        txtDisplayNotes = new JTextArea("-"); // Default strip
        txtDisplayNotes.setFont(FONT_PLAIN);
        txtDisplayNotes.setLineWrap(true);
        txtDisplayNotes.setWrapStyleWord(true);
        txtDisplayNotes.setOpaque(false);
        txtDisplayNotes.setEditable(false); // Read Only

        notesBox.add(txtDisplayNotes, BorderLayout.CENTER);
        infoPanel.add(notesBox, gbc);

        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerWrapper.setBackground(BG_COLOR);
        centerWrapper.add(infoPanel);

        // --- 3. WRAPPER UTAMA (GABUNGAN JUDUL + CONTENT) ---
        // Ini bagian barunya: Menyatukan Judul dan Content
        JPanel mainSection = new JPanel(new BorderLayout());
        mainSection.setBackground(BG_COLOR);

        // Buat Judul
        JLabel title = createSectionTitle("Informasi Pesanan");
        title.setBorder(new EmptyBorder(0, 0, 15, 0)); // Margin bawah 15px

        // Susun
        mainSection.add(title, BorderLayout.NORTH);
        mainSection.add(centerWrapper, BorderLayout.CENTER);

        return mainSection;
    }

    private JLabel createLabel(String text, boolean alignRight) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_PLAIN);
        if (alignRight) lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        return lbl;
    }

    
}
