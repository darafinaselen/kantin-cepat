package app;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class AdminDashboard extends JFrame {

    private JPanel mainContentPanel;
    private CardLayout cardLayout;

    // --- KOMPONEN GLOBAL MENU ---
    private JTextField txtNamaMenu, txtHargaMenu;
    private JComboBox<String> cbKategoriMenu, cbStatusMenu;
    private JLabel lblImagePreview;
    private ImageIcon selectedMenuIcon = null;

    // --- KOMPONEN GLOBAL USER ---
    private JTextField txtUserApp;
    private JTextField txtPassApp;
    private JComboBox<String> cbRoleApp;

    // --- KOMPONEN GLOBAL CHAT ---
    private JPanel pnlChatMessages;
    private JTextField txtChatInput;
    private JList<String> listContacts;
    private DefaultListModel<String> contactDisplayModel;
    private ArrayList<String> allContactsList;
    private JLabel lblChatTarget;
    private JTextField txtSearchContact; 
    private int customerCounter = 0;

    // --- KOMPONEN GLOBAL ORDER ---
    private JTable orderTable;
    private DefaultTableModel orderModel;
    private TableRowSorter<DefaultTableModel> orderSorter;
    private JTextField txtSearchOrder;

    public AdminDashboard() {
        setTitle("Admin Panel - Kantin Cepat");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. HEADER
        JPanel headerPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth(); int h = getHeight();
                GradientPaint gp = new GradientPaint(0, 0, AppColor.PRIMARY_PURPLE, w, 0, AppColor.DARK_PURPLE);
                g2d.setPaint(gp); g2d.fillRect(0, 0, w, h);
            }
        };
        headerPanel.setPreferredSize(new Dimension(getWidth(), 80));
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(15, 30, 15, 30));

        JLabel titleLabel = new JLabel("Admin Panel — Kantin Cepat");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        titleLabel.setForeground(Color.WHITE);
        
        JButton btnLogout = createRoundedButton("Logout", AppColor.BTN_RED, Color.WHITE);
        btnLogout.setPreferredSize(new Dimension(100, 40));
        btnLogout.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(this, "Keluar?", "Logout", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
                new LoginAdmin().setVisible(true); dispose();
            }
        });

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(btnLogout, BorderLayout.EAST);

        // 2. SIDEBAR
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setBackground(Color.WHITE);
        sidebarPanel.setPreferredSize(new Dimension(260, getHeight()));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(new EmptyBorder(30, 15, 30, 15));

        addSidebarItem(sidebarPanel, "Kelola Menu", "MENU");
        addSidebarItem(sidebarPanel, "Kelola Pengguna", "USER");
        addSidebarItem(sidebarPanel, "Manajemen Pesanan", "ORDER");
        addSidebarItem(sidebarPanel, "Live Chat", "CHAT");

        // 3. MAIN CONTENT
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(AppColor.BACKGROUND);
        mainContentPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); 
        
        mainContentPanel.add(createMenuPanel(), "MENU");
        mainContentPanel.add(createUserPanel(), "USER");
        mainContentPanel.add(createOrderPanel(), "ORDER");
        mainContentPanel.add(createChatPanel(), "CHAT"); 

        add(headerPanel, BorderLayout.NORTH);
        add(sidebarPanel, BorderLayout.WEST);
        add(mainContentPanel, BorderLayout.CENTER);
    }

    // ==================================================================================
    // 1. PANEL MENU
    // ==================================================================================
    private JPanel createMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setOpaque(false);

        JLabel lblHeader = new JLabel("Daftar Menu Makanan & Minuman");
        lblHeader.setFont(AppColor.FONT_HEADER);
        panel.add(lblHeader, BorderLayout.NORTH);

        JPanel contentGrid = new JPanel(new BorderLayout(20, 20));
        contentGrid.setOpaque(false);

        RoundedPanel formCard = new RoundedPanel(20, Color.WHITE);
        formCard.setLayout(new GridBagLayout());
        formCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        txtNamaMenu = createModernTextField();
        JPanel pnlHarga = new RoundedPanel(10, Color.WHITE);
        pnlHarga.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        pnlHarga.setLayout(new BorderLayout());
        JLabel lblRp = new JLabel("  Rp. "); lblRp.setForeground(Color.GRAY);
        txtHargaMenu = new JTextField(); txtHargaMenu.setBorder(null); txtHargaMenu.setOpaque(false);
        txtHargaMenu.addKeyListener(new KeyAdapter() { public void keyTyped(KeyEvent e) { if (!Character.isDigit(e.getKeyChar())) e.consume(); }});
        pnlHarga.add(lblRp, BorderLayout.WEST); pnlHarga.add(txtHargaMenu, BorderLayout.CENTER);
        
        cbKategoriMenu = new JComboBox<>(new String[]{"Makanan Berat", "Minuman", "Snack"});
        cbStatusMenu = new JComboBox<>(new String[]{"Tersedia", "Habis"});
        lblImagePreview = new JLabel("No Image", SwingConstants.CENTER);
        lblImagePreview.setPreferredSize(new Dimension(140, 140));
        lblImagePreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        JButton btnPilihFoto = createRoundedButton("Pilih Foto", AppColor.PRIMARY_PURPLE, Color.WHITE);

        addFormRow(formCard, gbc, 0, "Nama Menu:", txtNamaMenu);
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.1; formCard.add(new JLabel("Harga:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.9; pnlHarga.setPreferredSize(new Dimension(200, 35)); formCard.add(pnlHarga, gbc);
        addFormRow(formCard, gbc, 2, "Kategori:", cbKategoriMenu);
        addFormRow(formCard, gbc, 3, "Status:", cbStatusMenu);
        gbc.gridx = 2; gbc.gridy = 0; gbc.gridheight = 3; gbc.fill = GridBagConstraints.NONE; formCard.add(lblImagePreview, gbc);
        gbc.gridx = 2; gbc.gridy = 3; gbc.gridheight = 1; gbc.fill = GridBagConstraints.HORIZONTAL; formCard.add(btnPilihFoto, gbc);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        JButton btnAdd = createRoundedButton("Tambah", AppColor.BTN_SUCCESS, Color.WHITE);
        JButton btnEdit = createRoundedButton("Ubah", AppColor.BTN_YELLOW, Color.BLACK);
        JButton btnDel = createRoundedButton("Hapus", AppColor.BTN_RED, Color.WHITE);
        JButton btnReset = createRoundedButton("Reset", AppColor.BTN_MAROON, Color.WHITE);
        btnPanel.add(btnAdd); btnPanel.add(btnEdit); btnPanel.add(btnDel); btnPanel.add(btnReset);
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3; formCard.add(btnPanel, gbc);

        String[] columns = {"Foto", "ID", "Nama Menu", "Harga (Rp)", "Kategori", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public Class<?> getColumnClass(int column) { return column == 0 ? ImageIcon.class : Object.class; }
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        styleTable(table);
        table.setRowHeight(70);
        table.getColumnModel().getColumn(0).setCellRenderer(new ImageRenderer());
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        btnPilihFoto.addActionListener(e -> chooseImage());
        btnAdd.addActionListener(e -> { model.addRow(new Object[]{selectedMenuIcon!=null?resizeImage(selectedMenuIcon,60,60):null, model.getRowCount()+1, txtNamaMenu.getText(), txtHargaMenu.getText(), cbKategoriMenu.getSelectedItem(), cbStatusMenu.getSelectedItem()}); clearMenuForm(); });
        btnDel.addActionListener(e -> { if(table.getSelectedRow()>=0) model.removeRow(table.getSelectedRow()); });
        btnReset.addActionListener(e -> clearMenuForm());
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                txtNamaMenu.setText(model.getValueAt(row, 2).toString());
                txtHargaMenu.setText(model.getValueAt(row, 3).toString());
                cbKategoriMenu.setSelectedItem(model.getValueAt(row, 4).toString());
                cbStatusMenu.setSelectedItem(model.getValueAt(row, 5).toString());
                ImageIcon thumb = (ImageIcon) model.getValueAt(row, 0);
                if(thumb!=null) { selectedMenuIcon = resizeImage(thumb, 120, 120); lblImagePreview.setIcon(selectedMenuIcon); lblImagePreview.setText(""); }
                else { lblImagePreview.setIcon(null); lblImagePreview.setText("No Image"); }
            }
        });

        contentGrid.add(formCard, BorderLayout.NORTH);
        contentGrid.add(scrollPane, BorderLayout.CENTER);
        panel.add(contentGrid, BorderLayout.CENTER);
        return panel;
    }

    // ==================================================================================
    // 2. PANEL USER
    // ==================================================================================
    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setOpaque(false);
        JLabel lblHeader = new JLabel("Kelola Pengguna Sistem");
        lblHeader.setFont(AppColor.FONT_HEADER);
        panel.add(lblHeader, BorderLayout.NORTH);

        JPanel contentGrid = new JPanel(new BorderLayout(20, 20));
        contentGrid.setOpaque(false);
        RoundedPanel formCard = new RoundedPanel(20, Color.WHITE);
        formCard.setLayout(new GridBagLayout());
        formCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtUserApp = createModernTextField();
        txtPassApp = createModernTextField();
        cbRoleApp = new JComboBox<>(new String[]{"ADMIN", "DAPUR", "PELANGGAN"});
        addFormRow(formCard, gbc, 0, "Username:", txtUserApp);
        addFormRow(formCard, gbc, 1, "Password:", txtPassApp);
        addFormRow(formCard, gbc, 2, "Role:", cbRoleApp);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        JButton btnAdd = createRoundedButton("Tambah", AppColor.BTN_SUCCESS, Color.WHITE);
        JButton btnEdit = createRoundedButton("Ubah", AppColor.BTN_YELLOW, Color.BLACK);
        JButton btnDel = createRoundedButton("Hapus", AppColor.BTN_RED, Color.WHITE);
        JButton btnReset = createRoundedButton("Reset", AppColor.BTN_MAROON, Color.WHITE);
        btnPanel.add(btnAdd); btnPanel.add(btnEdit); btnPanel.add(btnDel); btnPanel.add(btnReset);
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; formCard.add(btnPanel, gbc);

        String[] cols = {"Username", "Password", "Role"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        model.addRow(new Object[]{"admin", "admin123", "ADMIN"});
        model.addRow(new Object[]{"koki1", "12345", "DAPUR"});
        JTable table = new JTable(model);
        styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                txtUserApp.setText(model.getValueAt(row, 0).toString());
                txtUserApp.setEditable(false);
                cbRoleApp.setSelectedItem(model.getValueAt(row, 2).toString());
                txtPassApp.setText("");
            }
        });
        btnAdd.addActionListener(e -> { model.addRow(new Object[]{txtUserApp.getText(), txtPassApp.getText(), cbRoleApp.getSelectedItem()}); clearUserForm(); });
        btnDel.addActionListener(e -> { if(table.getSelectedRow()>=0) model.removeRow(table.getSelectedRow()); });
        btnEdit.addActionListener(e -> { 
            int r = table.getSelectedRow();
            if(r>=0) { model.setValueAt(cbRoleApp.getSelectedItem(), r, 2); if(!txtPassApp.getText().isEmpty()) model.setValueAt(txtPassApp.getText(), r, 1); clearUserForm(); }
        });
        btnReset.addActionListener(e -> clearUserForm());

        contentGrid.add(formCard, BorderLayout.NORTH);
        contentGrid.add(scroll, BorderLayout.CENTER);
        panel.add(contentGrid, BorderLayout.CENTER);
        return panel;
    }

    // ==================================================================================
    // 3. PANEL PESANAN
    // ==================================================================================
    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setOpaque(false);
        JLabel lblHeader = new JLabel("Daftar Pesanan Masuk");
        lblHeader.setFont(AppColor.FONT_HEADER);
        panel.add(lblHeader, BorderLayout.NORTH);

        RoundedPanel toolBar = new RoundedPanel(15, Color.WHITE);
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        toolBar.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel lblSearch = new JLabel("Cari:"); lblSearch.setFont(AppColor.FONT_BOLD);
        txtSearchOrder = createModernTextField(); txtSearchOrder.setPreferredSize(new Dimension(200, 35));
        JLabel lblFilter = new JLabel("Filter:"); lblFilter.setFont(AppColor.FONT_BOLD);
        JComboBox<String> cbFilter = new JComboBox<>(new String[]{"Semua", "MENUNGGU", "DIPROSES", "SELESAI", "DIBATALKAN"});
        JButton btnCancel = createRoundedButton("Batalkan Pesanan", AppColor.BTN_RED, Color.WHITE);

        toolBar.add(lblSearch); toolBar.add(txtSearchOrder); toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(lblFilter); toolBar.add(cbFilter); toolBar.add(Box.createHorizontalStrut(20)); toolBar.add(btnCancel);

        String[] cols = {"ID Pesanan", "Pelanggan", "Menu", "Total (Rp)", "Status", "Tanggal", "Jam Dipesan", "Jam Diambil"};
        Object[][] data = {
            {"ORD-001", "Budi", "Nasi Goreng (1)", "15000", "MENUNGGU", "30-11-2025", "10:30", "-"},
            {"ORD-002", "Siti", "Es Teh (2)", "10000", "DIPROSES", "30-11-2025", "10:35", "-"},
            {"ORD-003", "Andi", "Mie Goreng (1)", "12000", "SELESAI", "29-11-2025", "09:00", "09:25"}
        };
        orderModel = new DefaultTableModel(data, cols) { public boolean isCellEditable(int r, int c) { return false; }};
        orderTable = new JTable(orderModel);
        styleTable(orderTable);
        orderTable.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        orderSorter = new TableRowSorter<>(orderModel);
        orderTable.setRowSorter(orderSorter);

        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        txtSearchOrder.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = txtSearchOrder.getText();
                if (text.trim().length() == 0) orderSorter.setRowFilter(null);
                else orderSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });
        cbFilter.addActionListener(e -> {
            String selected = (String) cbFilter.getSelectedItem();
            if(selected.equals("Semua")) orderSorter.setRowFilter(null);
            else orderSorter.setRowFilter(RowFilter.regexFilter(selected, 4));
        });
        btnCancel.addActionListener(e -> {
            int r = orderTable.getSelectedRow();
            if(r >= 0) {
                int modelRow = orderTable.convertRowIndexToModel(r);
                String st = orderTable.getValueAt(r, 4).toString();
                if(st.equals("SELESAI") || st.equals("DIBATALKAN")) JOptionPane.showMessageDialog(this, "Tidak bisa dibatalkan.");
                else if(JOptionPane.showConfirmDialog(this, "Batalkan?", "Konfirmasi", JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
                    orderModel.setValueAt("DIBATALKAN", modelRow, 4); orderModel.setValueAt("-", modelRow, 7);
                }
            } else JOptionPane.showMessageDialog(this, "Pilih pesanan!");
        });

        JPanel container = new JPanel(new BorderLayout(0, 15));
        container.setOpaque(false);
        container.add(toolBar, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);
        panel.add(container, BorderLayout.CENTER);
        return panel;
    }

    // ==================================================================================
    // 4. PANEL CHAT (FIX: INSTANT REFRESH & SEARCH)
    // ==================================================================================
    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setOpaque(false);
        JLabel lblHeader = new JLabel("Live Chat - Support Pelanggan");
        lblHeader.setFont(AppColor.FONT_HEADER);
        panel.add(lblHeader, BorderLayout.NORTH);

        JPanel chatContainer = new JPanel(new BorderLayout(15, 0));
        chatContainer.setOpaque(false);

        // LEFT
        RoundedPanel contactPanel = new RoundedPanel(15, Color.WHITE);
        contactPanel.setLayout(new BorderLayout());
        contactPanel.setPreferredSize(new Dimension(280, 0));
        
        JPanel pnlSearch = new JPanel(new BorderLayout());
        pnlSearch.setBackground(Color.WHITE);
        pnlSearch.setBorder(new EmptyBorder(10, 10, 10, 10));
        txtSearchContact = createModernTextField();
        pnlSearch.add(new JLabel("Cari: "), BorderLayout.WEST);
        pnlSearch.add(txtSearchContact, BorderLayout.CENTER);

        contactDisplayModel = new DefaultListModel<>();
        allContactsList = new ArrayList<>();
        addNewChatSession("Budi");
        addNewChatSession("Siti");
        addNewChatSession("Andi");
        
        listContacts = new JList<>(contactDisplayModel);
        listContacts.setFont(new Font("SansSerif", Font.PLAIN, 14));
        listContacts.setFixedCellHeight(50);
        listContacts.setSelectionBackground(AppColor.SIDEBAR_HOVER);
        listContacts.setSelectionForeground(Color.BLACK);
        listContacts.setBorder(new EmptyBorder(5, 5, 5, 5));

        contactPanel.add(pnlSearch, BorderLayout.NORTH);
        contactPanel.add(new JScrollPane(listContacts), BorderLayout.CENTER);

        txtSearchContact.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filterContacts(txtSearchContact.getText()); }
        });

        // RIGHT
        RoundedPanel chatAreaPanel = new RoundedPanel(15, new Color(245, 245, 245));
        chatAreaPanel.setLayout(new BorderLayout());
        chatAreaPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        lblChatTarget = new JLabel(" Pilih kontak untuk mulai chat", SwingConstants.LEFT);
        lblChatTarget.setFont(AppColor.FONT_BOLD);
        lblChatTarget.setPreferredSize(new Dimension(0, 50));
        lblChatTarget.setBorder(new EmptyBorder(0, 15, 0, 0));
        
        pnlChatMessages = new JPanel();
        pnlChatMessages.setLayout(new BoxLayout(pnlChatMessages, BoxLayout.Y_AXIS));
        pnlChatMessages.setBackground(new Color(245, 245, 245));
        
        JScrollPane scrollChat = new JScrollPane(pnlChatMessages);
        scrollChat.setBorder(null);
        scrollChat.getVerticalScrollBar().setUnitIncrement(16);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setOpaque(false);
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        txtChatInput = createModernTextField(); txtChatInput.setPreferredSize(new Dimension(0, 45));
        JButton btnSend = createRoundedButton("Kirim", AppColor.PRIMARY_PURPLE, Color.WHITE);
        btnSend.setPreferredSize(new Dimension(80, 45));
        inputPanel.add(txtChatInput, BorderLayout.CENTER); inputPanel.add(btnSend, BorderLayout.EAST);

        chatAreaPanel.add(lblChatTarget, BorderLayout.NORTH);
        chatAreaPanel.add(scrollChat, BorderLayout.CENTER);
        chatAreaPanel.add(inputPanel, BorderLayout.SOUTH);

        listContacts.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String sel = listContacts.getSelectedValue();
                if(sel != null) {
                    lblChatTarget.setText(sel);
                    pnlChatMessages.removeAll();
                    addMessageToChat("Halo, pesanan saya belum datang.", "10:30", false);
                    addMessageToChat("Mohon ditunggu sebentar ya kak.", "10:31", true);
                }
            }
        });

        ActionListener sendAction = e -> {
            String msg = txtChatInput.getText().trim();
            if (listContacts.getSelectedValue() == null) {
                JOptionPane.showMessageDialog(this, "Pilih kontak pelanggan dulu!", "Info", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if(!msg.isEmpty()) {
                String time = new SimpleDateFormat("HH:mm").format(new Date());
                addMessageToChat(msg, time, true);
                txtChatInput.setText("");
                SwingUtilities.invokeLater(() -> scrollChat.getVerticalScrollBar().setValue(scrollChat.getVerticalScrollBar().getMaximum()));
            }
        };
        btnSend.addActionListener(sendAction);
        txtChatInput.addActionListener(sendAction);

        chatContainer.add(contactPanel, BorderLayout.WEST);
        chatContainer.add(chatAreaPanel, BorderLayout.CENTER);
        panel.add(chatContainer, BorderLayout.CENTER);
        return panel;
    }

    // ==================================================================================
    // HELPERS
    // ==================================================================================
    
    // FIX: Method untuk menambah bubble chat secara instan (revalidate/repaint)
    private void addMessageToChat(String message, String time, boolean isMe) {
        JPanel wrapper = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrapper.setOpaque(false);
        ChatBubble bubble = new ChatBubble(message, time, isMe);
        wrapper.add(bubble);
        pnlChatMessages.add(wrapper);
        pnlChatMessages.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // PENTING: Refresh panel agar bubble muncul seketika
        pnlChatMessages.revalidate();
        pnlChatMessages.repaint();
    }

    private void addNewChatSession(String name) {
        customerCounter++;
        String formattedName = "Pelanggan " + customerCounter + " - " + name;
        allContactsList.add(formattedName);
        filterContacts("");
    }

    private void filterContacts(String query) {
        contactDisplayModel.clear();
        for (String contact : allContactsList) {
            if (contact.toLowerCase().contains(query.toLowerCase())) {
                contactDisplayModel.addElement(contact);
            }
        }
    }

    private void addSidebarItem(JPanel sidebar, String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 15));
        btn.setForeground(AppColor.TEXT_MAIN);
        btn.setBackground(Color.WHITE);
        btn.setBorder(new EmptyBorder(12, 15, 12, 15));
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(AppColor.SIDEBAR_HOVER); btn.setFont(new Font("SansSerif", Font.BOLD, 15)); }
            public void mouseExited(MouseEvent e) { btn.setBackground(Color.WHITE); btn.setFont(new Font("SansSerif", Font.PLAIN, 15)); }
        });
        btn.addActionListener(e -> cardLayout.show(mainContentPanel, cardName));
        sidebar.add(btn); sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.1;
        JLabel lbl = new JLabel(label); lbl.setFont(AppColor.FONT_BOLD);
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.9;
        comp.setPreferredSize(new Dimension(200, 35));
        panel.add(comp, gbc);
    }

    private JTextField createModernTextField() {
        JTextField txt = new JTextField(20) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                super.paintComponent(g); g2.dispose();
            }
            @Override protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.LIGHT_GRAY);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2.dispose();
            }
        };
        txt.setOpaque(false); txt.setBorder(new EmptyBorder(5, 10, 5, 10));
        return txt;
    }

    private JButton createRoundedButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                super.paintComponent(g); g2.dispose();
            }
        };
        btn.setBackground(bg); btn.setForeground(fg); btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setFocusPainted(false); btn.setBorderPainted(false); btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bg.brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bg); }
        });
        return btn;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(35); table.setShowVerticalLines(false); table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setSelectionBackground(AppColor.SIDEBAR_HOVER); table.setSelectionForeground(Color.BLACK);
        JTableHeader header = table.getTableHeader();
        header.setBackground(AppColor.PRIMARY_PURPLE); header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 14)); header.setBorder(null);
    }

    private void clearMenuForm() { txtNamaMenu.setText(""); txtHargaMenu.setText(""); cbKategoriMenu.setSelectedIndex(0); cbStatusMenu.setSelectedIndex(0); lblImagePreview.setIcon(null); lblImagePreview.setText("No Image"); selectedMenuIcon = null; }
    private void clearUserForm() { txtUserApp.setText(""); txtUserApp.setEditable(true); txtPassApp.setText(""); cbRoleApp.setSelectedIndex(0); }
    private ImageIcon resizeImage(ImageIcon icon, int w, int h) { return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH)); }
    private void chooseImage() {
        JFileChooser fc = new JFileChooser(); fc.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png"));
        if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedMenuIcon = new ImageIcon(fc.getSelectedFile().getAbsolutePath());
            lblImagePreview.setIcon(resizeImage(selectedMenuIcon, 120, 120)); lblImagePreview.setText("");
        }
    }

    // --- INNER CLASSES ---
    private class RoundedPanel extends JPanel {
        private int radius; private Color bgColor;
        public RoundedPanel(int radius, Color bgColor) { this.radius = radius; this.bgColor = bgColor; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g); Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor); g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
        }
    }

    private class ChatBubble extends JPanel {
        public ChatBubble(String msg, String time, boolean isMe) {
            setLayout(new BorderLayout()); setOpaque(false);
            setBorder(new EmptyBorder(10, 15, 10, 15));
            setBackground(isMe ? new Color(225, 245, 254) : Color.WHITE);
            JLabel lblMsg = new JLabel("<html><p style='width: 200px'>"+msg+"</p></html>");
            lblMsg.setFont(new Font("SansSerif", Font.PLAIN, 14));
            JLabel lblTime = new JLabel(time);
            lblTime.setFont(new Font("SansSerif", Font.PLAIN, 10)); lblTime.setForeground(Color.GRAY);
            lblTime.setHorizontalAlignment(SwingConstants.RIGHT);
            add(lblMsg, BorderLayout.CENTER); add(lblTime, BorderLayout.SOUTH);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground()); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            super.paintComponent(g);
        }
    }

    private class ImageRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel l = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if(value instanceof ImageIcon) { l.setIcon((ImageIcon)value); l.setText(""); l.setHorizontalAlignment(CENTER); }
            else { l.setIcon(null); l.setText("No Image"); } return l;
        }
    }

    private class StatusRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String s = (String) value;
            if (s.equalsIgnoreCase("SELESAI")) c.setForeground(new Color(46, 125, 50)); 
            else if (s.equalsIgnoreCase("DIBATALKAN")) c.setForeground(Color.RED);
            else if (s.equalsIgnoreCase("DIPROSES")) c.setForeground(Color.BLUE);
            else c.setForeground(new Color(255, 143, 0));
            c.setFont(c.getFont().deriveFont(Font.BOLD)); return c;
        }
    }

    public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> new AdminDashboard().setVisible(true));
    }
}