package com.tubes.kantincepat.client.view;
import com.tubes.kantincepat.client.view.RoundedPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.tubes.kantincepat.client.ClientApp;
import com.tubes.kantincepat.client.net.ClientSocket;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.NumberFormat;
import java.util.Locale;
// import java.awt.event.ActionListener;
// import java.awt.event.ActionEvent;

public class Menu_Utama extends JPanel {

    private ClientApp clientApp; 

    private List<MenuItem> allMenuItems = new ArrayList<>();
    private JPanel menuGridPanel; 
    private Map<String, RoundedPanel> categoryButtons = new HashMap<>();
    
    
    public Menu_Utama(ClientApp app) {
        this.clientApp = app;
        setLayout(new BorderLayout());
        loadMenuFromServer();

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(GUIUtils.COLOR_BG2); // Akses warna dari ClientApp
        content.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Add Components
        content.add(createHeader());
        content.add(Box.createVerticalStrut(20));
        content.add(createSearchBar());
        content.add(Box.createVerticalStrut(20));
        content.add(createCategories());
        content.add(Box.createVerticalStrut(20));

        // Label Menu
        JLabel lblMenu = new JLabel("Our Menu");
        lblMenu.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 18f));
        JPanel titleWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        titleWrapper.setBackground(GUIUtils.COLOR_BG2);
        titleWrapper.add(lblMenu);
        titleWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        content.add(titleWrapper);
        content.add(Box.createVerticalStrut(15));

        // Grid Menu
        menuGridPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        menuGridPanel.setBackground(GUIUtils.COLOR_BG2);
        content.add(menuGridPanel);
        filterMenu("All");

        // ScrollPane
        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        this.setFocusable(true); 
        
        SwingUtilities.invokeLater(() -> {
            this.requestFocusInWindow();
        });
    }

    private void loadMenuFromServer() {
        allMenuItems.clear();
        
        // Kirim request ke Server
        String response = ClientSocket.getInstance().sendRequest("GET_MENU");
        
        // Format Response Server:
        // "MENU_DATA:ID|Nama|Harga|Kategori|Gambar|Desc;ID|Nama|..."
        if (response != null && response.startsWith("MENU_DATA:")) {
            String dataPart = response.substring(10); 
            if (!dataPart.isEmpty()) {
                String[] items = dataPart.split(";");
                for (String itemStr : items) {
                    String[] fields = itemStr.split("\\|");
                    if (fields.length >= 6) {
                        try {
                            int id = Integer.parseInt(fields[0]);
                            String name = fields[1];
                            int price = Integer.parseInt(fields[2]);
                            String cat = fields[3];
                            String img = fields[4];
                            String desc = fields[5];
                            
                            // Mapping kategori (Server kirim UPPERCASE, UI butuh Title Case)
                            String appCat = toTitleCase(cat);
                            if (img.equals("null")) img = getDefaultImage(appCat);

                            allMenuItems.add(new MenuItem(id, name, desc, price, appCat, img, true));
                        } catch (Exception e) {
                            System.err.println("Gagal parsing item: " + itemStr);
                        }
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Gagal mengambil menu dari server.");
        }
    }

    private String getDefaultImage(String cat) {
        if (cat.equalsIgnoreCase("Drink")) return "drink.png";
        if (cat.equalsIgnoreCase("Snack")) return "snack.png";
        return "meals.png";
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(GUIUtils.COLOR_BG);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel lblWelcome = new JLabel("Welcome Back!!");
        lblWelcome.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 18f));

        ImageIcon originalIcon = GUIUtils.loadImageIcon("icon_chat.png", 30, 30);
        JLabel btnChat;

        // 2. Pengecekan Anti-Crash (Null Check)
        if (originalIcon != null) {
            ImageIcon scaledImage = new ImageIcon(originalIcon.getImage());
            btnChat = new JLabel(scaledImage);
        } else {
            System.err.println("Peringatan: icon_chat.png tidak ditemukan, pakai teks pengganti.");
            btnChat = new JLabel("Chat"); 
            btnChat.setForeground(GUIUtils.COLOR_PRIMARY);
        }
        
        // Agar kursor berubah jadi tangan
        btnChat.setCursor(new Cursor(Cursor.HAND_CURSOR)); 

        // TAMBAHKAN MOUSE LISTENER INI:
        btnChat.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Panggil ClientApp untuk ganti halaman ke "CHAT"
                clientApp.showView("CHAT");
            }
        });

        panel.add(lblWelcome, BorderLayout.WEST);
        panel.add(btnChat, BorderLayout.EAST);
        return panel;
    }

    private JPanel createSearchBar() {
        // Container Rounded
        RoundedPanel searchPanel = new RoundedPanel(30, Color.WHITE);
        searchPanel.setLayout(new BorderLayout(10, 0)); 
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        searchPanel.setBorder(new EmptyBorder(5, 15, 5, 10)); 

        // Icon dan TextField
        JLabel iconSearch = new JLabel("🔍");
        String placeholder = "Mau makan apa hari ini?";
        JTextField searchInput = new JTextField(placeholder);
        searchInput.setBorder(null); 
        searchInput.setOpaque(false); 
        searchInput.setForeground(Color.GRAY);

        // --- LOGIKA PLACEHOLDER ---
        searchInput.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchInput.getText().equals(placeholder)) {
                    searchInput.setText("");
                    searchInput.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchInput.getText().isEmpty()) {
                    searchInput.setText(placeholder);
                    searchInput.setForeground(Color.GRAY);
                }
            }
        });

        searchInput.addActionListener(e -> {
            String keyword = searchInput.getText();
            
            // Cek kondisi:
            // 1. Keyword tidak boleh sama dengan placeholder
            // 2. Keyword tidak boleh kosong
            if (!keyword.equals(placeholder) && !keyword.isEmpty()) {
                // Lakukan pencarian
                performSearch(keyword);
            } else {
                // Jika kosong/placeholder di-enter, reset tampilan ke "All"
                filterMenu("All");
            }
        });

        searchPanel.add(iconSearch, BorderLayout.WEST);
        searchPanel.add(searchInput, BorderLayout.CENTER);

        return searchPanel;
    }

    // private void initMenuData() {
    //     allMenuItems.clear();

    //     // QUERY BARU: Langsung dari tabel 'menu_items'
    //     String sql = "SELECT menu_id, name, description, price, category, image_path, is_available " +
    //                  "FROM menu_items " +
    //                  "WHERE is_available = TRUE " +
    //                  "ORDER BY menu_id ASC";

    //     try (Connection conn = koneksiDB.getConnection();
    //          PreparedStatement ps = conn.prepareStatement(sql);
    //          ResultSet rs = ps.executeQuery()) {

    //         while (rs.next()) {
    //             int id = rs.getInt("menu_id");
    //             String name = rs.getString("name");
    //             String desc = rs.getString("description");
                
    //             // Database pakai DECIMAL, kita ambil int-nya saja
    //             int rawPrice = rs.getInt("price");                
    //             String categoryDb = rs.getString("category"); // "MEALS", "DRINK", "SNACK"
    //             String imgPath = rs.getString("image_path");
    //             boolean avail = rs.getBoolean("is_available");

    //             String appCategory = toTitleCase(categoryDb);
    //             // Handle gambar kosong
    //             if (imgPath == null || imgPath.isEmpty()) {
    //                 if (appCategory.equalsIgnoreCase("Drink")) imgPath = "drink.png";
    //                 else if (appCategory.equalsIgnoreCase("Snack")) imgPath = "snack.png";
    //                 else imgPath = "meals.png";
    //             }

    //             // Buat Object MenuItem
    //             MenuItem item = new MenuItem(id, name, desc, rawPrice, appCategory, imgPath, avail);
    //             allMenuItems.add(item);
    //         }

    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         JOptionPane.showMessageDialog(this, "Gagal memuat menu: " + e.getMessage());
    //     }
    // }

    private String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return "Meals";
        // Ambil huruf pertama, ubah ke kapital + sisa huruf kecil
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }

    // --- LOGIKA FILTER MENU ---
    private void filterMenu(String category) {
       menuGridPanel.removeAll();

        for (MenuItem item : allMenuItems) {
            if (category.equals("All") || item.category.equalsIgnoreCase(category)) {
                menuGridPanel.add(createMenuCard(item));
            }
        }

        menuGridPanel.revalidate();
        menuGridPanel.repaint();
        updateCategoryButtons(category);
    }

    private void updateCategoryButtons(String activeCategory) {
        for (Map.Entry<String, RoundedPanel> entry : categoryButtons.entrySet()) {
            String key = entry.getKey();
            RoundedPanel btn = entry.getValue();

            // Jika activeCategory null (sedang search), matikan semua tombol
            if (activeCategory != null && key.equals(activeCategory)) {
                btn.setColor(GUIUtils.COLOR_PRIMARY); 
            } else {
                btn.setColor(GUIUtils.COLOR_ACCENT); 
            }
            btn.repaint();
        }
    }

    private JPanel createCategories() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 0));
        panel.setBackground(GUIUtils.COLOR_BG2);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        // Tambahkan tombol dengan ID Kategori
        panel.add(createCategoryItem("All", "client\\src\\main\\resources\\assets\\all.png", "All"));
        panel.add(createCategoryItem("Meals", "client\\src\\main\\resources\\assets\\meals.png", "Meals"));
        panel.add(createCategoryItem("Drink", "client\\src\\main\\resources\\assets\\drink.png", "Drink"));
        panel.add(createCategoryItem("Snack", "client\\src\\main\\resources\\assets\\snack.png", "Snack"));
        return panel;
    }

    private JPanel createCategoryItem(String name, String imagePath, String categoryKey) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(GUIUtils.COLOR_BG2);

        // Container Tombol (Rounded)
        RoundedPanel iconPanel = new RoundedPanel(50, GUIUtils.COLOR_ACCENT);
        iconPanel.setPreferredSize(new Dimension(50, 50));
        iconPanel.setMaximumSize(new Dimension(50, 50));
        iconPanel.setLayout(new GridBagLayout());

        // Simpan referensi tombol ke Map agar bisa diubah warnanya
        categoryButtons.put(categoryKey, iconPanel);

        // Gambar
        ImageIcon originalIcon = new ImageIcon(imagePath);
        Image scaledImage = originalIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH);
        iconPanel.add(new JLabel(new ImageIcon(scaledImage)));

        // Label Nama
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(GUIUtils.getCustomFont("Lato-Regular.ttf", 11f));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // --- AKSI KLIK TOMBOL KATEGORI ---
        iconPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        iconPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                filterMenu(categoryKey);
            }
        });

        container.add(iconPanel);
        container.add(Box.createVerticalStrut(5));
        container.add(nameLabel);
        return container;
    }

    private JPanel createMenuCard(MenuItem item) {
        RoundedPanel card = new RoundedPanel(20, GUIUtils.COLOR_ACCENT);
        card.setLayout(new BorderLayout());
        card.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Gambar
        JPanel imgPanel = new JPanel(new GridBagLayout());
        imgPanel.setBackground(GUIUtils.COLOR_ACCENT);
        try {
        ImageIcon originalIcon = new ImageIcon(item.imagePath);
        Image scaledImage = originalIcon.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        JLabel imgLabel = new JLabel(new ImageIcon(scaledImage));
        imgPanel.add(imgLabel);
        
        } catch (Exception e) {
            imgPanel.add(new JLabel(item.name)); 
        }

        // Info
        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(GUIUtils.COLOR_ACCENT);
        JLabel lblName = new JLabel(item.name);
        lblName.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 14f));
        JLabel lblPrice = new JLabel(item.getFormattedPrice());
         lblPrice.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 14f));
        infoPanel.add(lblName);
        infoPanel.add(lblPrice);

        // Tombol +
        JPanel bottomRow = new JPanel(new BorderLayout());
        bottomRow.setBackground(GUIUtils.COLOR_ACCENT);
        bottomRow.add(infoPanel, BorderLayout.CENTER);

        JLabel btnAdd = new JLabel("+", SwingConstants.CENTER);
        btnAdd.setOpaque(true);
        btnAdd.setBackground(GUIUtils.COLOR_PRIMARY);
        btnAdd.setForeground(GUIUtils.COLOR_ACCENT);
        btnAdd.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 16f));
        btnAdd.setPreferredSize(new Dimension(25, 25));
        
        // Agar kursor berubah saat dihover
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // --- AKSI KLIK TOMBOL TAMBAH ---
        btnAdd.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 1. Panggil fungsi di ClientApp
                clientApp.addToCart(item);

                // 2. Beri efek visual/notifikasi sederhana
                // JOptionPane.showMessageDialog(null, item.name + " berhasil masuk keranjang!");
            }
        });
        
        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0,0));
        btnWrapper.setBackground(GUIUtils.COLOR_ACCENT);
        btnWrapper.add(btnAdd);
        bottomRow.add(btnWrapper, BorderLayout.SOUTH);

        card.add(imgPanel, BorderLayout.CENTER);
        card.add(bottomRow, BorderLayout.SOUTH);

        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clientApp.showMenuDetail(item);
            }
        });

        return card;
    }

    // --- METHOD UNTUK MENCARI MAKANAN ---
    private void performSearch(String keyword) {
        // 1. Bersihkan Grid Menu saat ini
        menuGridPanel.removeAll();

        // 2. Loop semua data di database (allMenuItems)
        boolean found = false;
        
        for (MenuItem item : allMenuItems) {        
            if (item.name.toLowerCase().contains(keyword.toLowerCase())) {
                // Jika cocok, buatkan kartunya dan tampilkan
                menuGridPanel.add(createMenuCard(item));
                found = true;
            }
        }
        
        // (Opsional) Jika tidak ada yang cocok, tampilkan pesan kosong
        if (!found) {
            JLabel lblEmpty = new JLabel("Menu tidak ditemukan :(", SwingConstants.CENTER);
            lblEmpty.setForeground(Color.GRAY);
            menuGridPanel.add(lblEmpty);
        }

        // 3. Refresh Tampilan agar perubahan terlihat
        menuGridPanel.revalidate();
        menuGridPanel.repaint();
        
        // 4. Reset warna tombol kategori (opsional, agar user tau sedang mode search)
        updateCategoryButtons(null); 
    }

    // private JPanel createMenuGrid() {
    //     menuGridPanel = new JPanel(new GridLayout(0, 2, 15, 15));
    //     menuGridPanel.setBackground(ClientApp.COLOR_BG);
    //     JPanel wrapper = new JPanel(new BorderLayout());
    //     wrapper.setBackground(ClientApp.COLOR_BG);
    //     wrapper.add(menuGridPanel, BorderLayout.NORTH);
        
    //     return wrapper;
    // }

    // private JPanel createMenuCard(String name, String price, Color imgColor, String imgPath) {
    //     RoundedPanel card = new RoundedPanel(20, ClientApp.COLOR_ACCENT);
    //     card.setLayout(new BorderLayout());
    //     card.setBorder(new EmptyBorder(10, 10, 10, 10));

    //     JPanel imgPanel = new JPanel(new GridBagLayout());
    //     imgPanel.setBackground(ClientApp.COLOR_ACCENT);
    //     RoundedPanel imgCircle = new RoundedPanel(100, imgColor);
    //     imgCircle.setPreferredSize(new Dimension(80, 80));
        
    //     ImageIcon originalIcon = new ImageIcon(imgPath);
    //     Image scaledImage = originalIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
    //     imgCircle.add(new JLabel(new ImageIcon(scaledImage)));
    //     imgPanel.add(imgCircle);

    //     JPanel infoPanel = new JPanel(new GridLayout(2, 1));
    //     infoPanel.setBackground(ClientApp.COLOR_ACCENT);
    //     JLabel lblName = new JLabel(name);
    //     lblName.setFont(ClientApp.FONT_BOLD);
    //     JLabel lblPrice = new JLabel(price);
    //     lblPrice.setFont(ClientApp.FONT_BOLD);
    //     infoPanel.add(lblName);
    //     infoPanel.add(lblPrice);

    //     JPanel bottomRow = new JPanel(new BorderLayout());
    //     bottomRow.setBackground(ClientApp.COLOR_ACCENT);
    //     bottomRow.add(infoPanel, BorderLayout.CENTER);

    //     JLabel btnAdd = new JLabel("+", SwingConstants.CENTER);
    //     btnAdd.setOpaque(true);
    //     btnAdd.setBackground(ClientApp.COLOR_PRIMARY);
    //     btnAdd.setForeground(ClientApp.COLOR_ACCENT);
    //     btnAdd.setFont(new Font("SansSerif", Font.BOLD, 16));
    //     btnAdd.setPreferredSize(new Dimension(25, 25));
        
    //     JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0,0));
    //     btnWrapper.setBackground(ClientApp.COLOR_ACCENT);
    //     btnWrapper.add(btnAdd);
    //     bottomRow.add(btnWrapper, BorderLayout.SOUTH);

    //     card.add(imgPanel, BorderLayout.CENTER);
    //     card.add(bottomRow, BorderLayout.SOUTH);
    //     return card;
    // }
}
