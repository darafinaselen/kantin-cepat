package app;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Dashboard Admin Kantin Cepat (Modern UI)
 * - Tab "Menu" : CRUD menu (in-memory)
 * - Tab "Pengguna" : CRUD user (in-memory)
 */
public class adminDashboard extends JFrame {

    // Warna tema konsisten dengan login
    private static final Color COLOR_PRIMARY   = new Color(0x2E7D32);
    private static final Color COLOR_PRIMARY_D = new Color(0x1B5E20);
    private static final Color COLOR_BG        = new Color(0xF5F5F5);
    private static final Color COLOR_TEXT_DARK = new Color(0x263238);

    private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

    // ======== Komponen untuk tab MENU =========
    private final DefaultTableModel menuTableModel = new DefaultTableModel(
            new Object[]{"Nama Menu", "Harga", "Kategori", "Status"}, 0
    );
    private final JTable menuTable = new JTable(menuTableModel);

    private final JTextField tfMenuName = new JTextField(20);
    private final JTextField tfMenuPrice = new JTextField(10);
    private final JTextField tfMenuCategory = new JTextField(15);
    private final JComboBox<String> cbMenuStatus =
            new JComboBox<>(new String[]{"Tersedia", "Habis"});

    // ======== Komponen untuk tab PENGGUNA =========
    private final DefaultTableModel userTableModel = new DefaultTableModel(
            new Object[]{"Username", "Role"}, 0
    );
    private final JTable userTable = new JTable(userTableModel);

    private final JTextField tfUserName = new JTextField(15);
    private final JComboBox<String> cbUserRole =
            new JComboBox<>(new String[]{"ADMIN", "DAPUR"});

    public adminDashboard() {
        super("Kantin Cepat — Admin Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 620);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildMainContent(), BorderLayout.CENTER);
    }

    // =================== HEADER ATAS ===================
    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, COLOR_PRIMARY,
                        getWidth(), getHeight(), COLOR_PRIMARY_D);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(1000, 70));
        header.setBorder(new EmptyBorder(10, 20, 10, 20));
        header.setOpaque(false);

        // kiri: icon + judul
        JLabel title = new JLabel("Admin Panel — Kantin Cepat");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));

        JLabel subtitle = new JLabel("Kelola menu kantin, pengguna, dan operasional harian.");
        subtitle.setForeground(new Color(230, 245, 230));
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JPanel leftText = new JPanel();
        leftText.setOpaque(false);
        leftText.setLayout(new BoxLayout(leftText, BoxLayout.Y_AXIS));
        leftText.add(title);
        leftText.add(Box.createVerticalStrut(2));
        leftText.add(subtitle);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        left.setOpaque(false);
        JLabel icon = new JLabel("\uD83C\uDF7D"); // 🍽
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        left.add(icon);
        left.add(leftText);

        // kanan: tombol logout
        JButton btnLogout = new JButton("Logout");
        stylePrimaryOutlineButton(btnLogout);
        btnLogout.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> new loginAdmin().setVisible(true));
            dispose();
        });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        right.setOpaque(false);
        right.add(btnLogout);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    // =================== MAIN CONTENT ===================
    private JComponent buildMainContent() {
        // root panel di bawah header
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(COLOR_BG);
        root.setBorder(new EmptyBorder(10, 10, 10, 10));

        // ========== SIDEBAR ==========
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(new EmptyBorder(16, 14, 16, 14));
        sidebar.setPreferredSize(new Dimension(220, 0)); // fix width

        JLabel lblMenu = new JLabel("Navigasi");
        lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMenu.setForeground(COLOR_TEXT_DARK);
        lblMenu.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnTabMenu = createSidebarButton("Kelola Menu");
        JButton btnTabUser = createSidebarButton("Kelola Pengguna");

        btnTabMenu.addActionListener(e -> tabbedPane.setSelectedIndex(0));
        btnTabUser.addActionListener(e -> tabbedPane.setSelectedIndex(1));

        sidebar.add(lblMenu);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(btnTabMenu);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(btnTabUser);
        sidebar.add(Box.createVerticalGlue());

        // ========== CARD KANAN (FULL WIDTH SISA LAYAR) ==========
        JPanel cardWrapper = new RoundedPanel(20);
        cardWrapper.setLayout(new BorderLayout());
        cardWrapper.setBackground(Color.WHITE);
        cardWrapper.setBorder(new EmptyBorder(12, 12, 12, 12));

        tabbedPane.setBorder(null);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabbedPane.addTab("Menu", buildMenuPanel());
        tabbedPane.addTab("Pengguna", buildUserPanel());

        cardWrapper.add(tabbedPane, BorderLayout.CENTER);

        // bungkus card supaya nempel ke atas & full sisa lebar
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(new EmptyBorder(0, 10, 0, 0)); // jarak dari sidebar
        centerWrapper.add(cardWrapper, BorderLayout.CENTER);

        // masukkan ke root
        root.add(sidebar, BorderLayout.WEST);
        root.add(centerWrapper, BorderLayout.CENTER);

        return root;
    }

    private JButton createSidebarButton(String text) {
    JButton btn = new JButton(text);
    btn.setAlignmentX(Component.LEFT_ALIGNMENT);
    btn.setFocusPainted(false);
    
    // Warna default
    Color normal = new Color(0xE8F5E9);      // hijau muda
    Color hover  = new Color(0xC8E6C9);      // hover lebih terang
    Color press  = new Color(0xA5D6A7);      // lebih gelap saat ditekan

    btn.setBackground(normal);
    btn.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
    btn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    btn.setHorizontalAlignment(SwingConstants.LEFT);

    btn.addChangeListener(e -> {
        if (btn.getModel().isPressed()) {
            btn.setBackground(press);
        } else if (btn.getModel().isRollover()) {
            btn.setBackground(hover);
        } else {
            btn.setBackground(normal);
        }
    });

    return btn;
}


    private void stylePrimaryOutlineButton(JButton btn) {
    btn.setFocusPainted(false);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    btn.setOpaque(true);

    Color normal = new Color(0xD32F2F);   // merah utama (#D32F2F)
    Color hover  = new Color(0xE53935);   // merah hover
    Color press  = new Color(0xB71C1C);   // merah gelap

    btn.setBackground(normal);
    btn.setForeground(Color.WHITE);
    btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
    btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));

    btn.getModel().addChangeListener(e -> {
        if (btn.getModel().isPressed()) {
            btn.setBackground(press);
        } else if (btn.getModel().isRollover()) {
            btn.setBackground(hover);
        } else {
            btn.setBackground(normal);
        }
    });
}

    // =================== TAB MENU ===================
    private JComponent buildMenuPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JLabel title = new JLabel("Daftar Menu Kantin");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(COLOR_TEXT_DARK);
        panel.add(title, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.35);
        split.setBorder(null);

        // ===== Form input =====
        JPanel formCard = new RoundedPanel(14);
        formCard.setLayout(new GridBagLayout());
        formCard.setBackground(new Color(0xFAFAFA));
        formCard.setBorder(new EmptyBorder(10, 12, 10, 12));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0.5;

        c.gridx = 0; c.gridy = 0;
        formCard.add(new JLabel("Nama Menu"), c);
        c.gridx = 1;
        formCard.add(tfMenuName, c);

        c.gridx = 0; c.gridy = 1;
        formCard.add(new JLabel("Harga (Rp)"), c);
        c.gridx = 1;
        formCard.add(tfMenuPrice, c);

        c.gridx = 0; c.gridy = 2;
        formCard.add(new JLabel("Kategori"), c);
        c.gridx = 1;
        formCard.add(tfMenuCategory, c);

        c.gridx = 0; c.gridy = 3;
        formCard.add(new JLabel("Status"), c);
        c.gridx = 1;
        formCard.add(cbMenuStatus, c);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnAdd = createPrimaryButton("Tambah");
        JButton btnUpdate = createFlatButton("Ubah");
        JButton btnDelete = createFlatButton("Hapus");
        JButton btnClear = createFlatButton("Reset");

        btnRow.setOpaque(false);
        btnRow.add(btnClear);
        btnRow.add(btnDelete);
        btnRow.add(btnUpdate);
        btnRow.add(btnAdd);

        c.gridx = 0; c.gridy = 4;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.EAST;
        formCard.add(btnRow, c);

        split.setTopComponent(formCard);

        // ===== Tabel =====
        menuTable.setFillsViewportHeight(true);
        menuTable.setRowHeight(22);
        menuTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        menuTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane scroll = new JScrollPane(menuTable);
        scroll.setBorder(BorderFactory.createTitledBorder("Data Menu"));
        split.setBottomComponent(scroll);

        panel.add(split, BorderLayout.CENTER);

        // ACTION
        btnAdd.addActionListener(e -> addMenuRow());
        btnUpdate.addActionListener(e -> updateMenuRow());
        btnDelete.addActionListener(e -> deleteMenuRow());
        btnClear.addActionListener(e -> clearMenuForm());

        menuTable.getSelectionModel().addListSelectionListener(e -> {
            int row = menuTable.getSelectedRow();
            if (row >= 0) {
                tfMenuName.setText(String.valueOf(menuTableModel.getValueAt(row, 0)));
                tfMenuPrice.setText(String.valueOf(menuTableModel.getValueAt(row, 1)));
                tfMenuCategory.setText(String.valueOf(menuTableModel.getValueAt(row, 2)));
                cbMenuStatus.setSelectedItem(menuTableModel.getValueAt(row, 3));
            }
        });

        return panel;
    }

    private JButton createPrimaryButton(String text) {
    JButton btn = new JButton(text);

    // MATIKAN semua UI default
    btn.setFocusPainted(false);
    btn.setBorderPainted(false);
    btn.setContentAreaFilled(false);
    btn.setOpaque(true);

    // Warna dasar tombol
    Color normal = new Color(0x2E7D32);   // hijau utama
    Color hover  = normal.brighter();     // hover lebih terang
    Color press  = normal.darker();       // pressed lebih gelap

    btn.setBackground(normal);
    btn.setForeground(Color.WHITE);
    btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
    btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));

    // efek hover & pressed
    btn.getModel().addChangeListener(e -> {
        if (btn.getModel().isPressed()) {
            btn.setBackground(press);
        } else if (btn.getModel().isRollover()) {
            btn.setBackground(hover);
        } else {
            btn.setBackground(normal);
        }
    });

    return btn;
}


    private JButton createFlatButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        btn.setForeground(new Color(0x555555));
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return btn;
    }

    private void addMenuRow() {
        String name = tfMenuName.getText().trim();
        String price = tfMenuPrice.getText().trim();
        String cat = tfMenuCategory.getText().trim();
        String status = (String) cbMenuStatus.getSelectedItem();

        if (name.isEmpty() || price.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Nama dan harga menu wajib diisi.", "Validasi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        menuTableModel.addRow(new Object[]{name, price, cat, status});
        clearMenuForm();
    }

    private void updateMenuRow() {
        int row = menuTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Pilih dulu baris menu yang ingin diubah.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        menuTableModel.setValueAt(tfMenuName.getText().trim(), row, 0);
        menuTableModel.setValueAt(tfMenuPrice.getText().trim(), row, 1);
        menuTableModel.setValueAt(tfMenuCategory.getText().trim(), row, 2);
        menuTableModel.setValueAt(cbMenuStatus.getSelectedItem(), row, 3);
    }

    private void deleteMenuRow() {
        int row = menuTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Pilih dulu baris menu yang ingin dihapus.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        menuTableModel.removeRow(row);
        clearMenuForm();
    }

    private void clearMenuForm() {
        tfMenuName.setText("");
        tfMenuPrice.setText("");
        tfMenuCategory.setText("");
        cbMenuStatus.setSelectedIndex(0);
        menuTable.clearSelection();
    }

    // =================== TAB PENGGUNA ===================
    private JComponent buildUserPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JLabel title = new JLabel("Daftar Pengguna Sistem");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(COLOR_TEXT_DARK);
        panel.add(title, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setResizeWeight(0.35);
        split.setBorder(null);

        JPanel formCard = new RoundedPanel(14);
        formCard.setLayout(new GridBagLayout());
        formCard.setBackground(new Color(0xFAFAFA));
        formCard.setBorder(new EmptyBorder(10, 12, 10, 12));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        c.gridx = 0; c.gridy = 0;
        formCard.add(new JLabel("Username"), c);
        c.gridx = 1;
        formCard.add(tfUserName, c);

        c.gridx = 0; c.gridy = 1;
        formCard.add(new JLabel("Role"), c);
        c.gridx = 1;
        formCard.add(cbUserRole, c);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        JButton btnAdd = createPrimaryButton("Tambah");
        JButton btnDelete = createFlatButton("Hapus");
        JButton btnClear = createFlatButton("Reset");
        btnRow.add(btnClear);
        btnRow.add(btnDelete);
        btnRow.add(btnAdd);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; c.anchor = GridBagConstraints.EAST;
        formCard.add(btnRow, c);

        split.setTopComponent(formCard);

        userTable.setFillsViewportHeight(true);
        userTable.setRowHeight(22);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getTableHeader().setReorderingAllowed(false);
        JScrollPane scroll = new JScrollPane(userTable);
        scroll.setBorder(BorderFactory.createTitledBorder("Data Pengguna"));
        split.setBottomComponent(scroll);

        panel.add(split, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> addUserRow());
        btnDelete.addActionListener(e -> deleteUserRow());
        btnClear.addActionListener(e -> clearUserForm());

        userTable.getSelectionModel().addListSelectionListener(e -> {
            int row = userTable.getSelectedRow();
            if (row >= 0) {
                tfUserName.setText(String.valueOf(userTableModel.getValueAt(row, 0)));
                cbUserRole.setSelectedItem(userTableModel.getValueAt(row, 1));
            }
        });

        return panel;
    }

    private void addUserRow() {
        String user = tfUserName.getText().trim();
        String role = (String) cbUserRole.getSelectedItem();

        if (user.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Username wajib diisi.", "Validasi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        userTableModel.addRow(new Object[]{user, role});
        clearUserForm();
    }

    private void deleteUserRow() {
        int row = userTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this,
                    "Pilih dulu user yang ingin dihapus.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        userTableModel.removeRow(row);
        clearUserForm();
    }

    private void clearUserForm() {
        tfUserName.setText("");
        cbUserRole.setSelectedIndex(0);
        userTable.clearSelection();
    }

    // =================== PANEL ROUNDED GENERIK ===================
    static class RoundedPanel extends JPanel {
        private final int radius;

        public RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
