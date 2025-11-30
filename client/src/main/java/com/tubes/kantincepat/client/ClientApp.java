package com.tubes.kantincepat.client;

import com.tubes.kantincepat.client.view.GUIUtils;
import com.tubes.kantincepat.client.view.RoundedPanel;
import com.tubes.kantincepat.client.view.LoginPanel;
import com.tubes.kantincepat.client.view.RegisterPanel;
import com.tubes.kantincepat.client.view.Menu_Utama;
import com.tubes.kantincepat.client.view.Menu_Bag;  
import com.tubes.kantincepat.client.view.Menu_Orders;
import com.tubes.kantincepat.client.view.Menu_Profile;
import com.tubes.kantincepat.client.view.Menu_Detail;
import com.tubes.kantincepat.client.view.Menu_Invoice;
import com.tubes.kantincepat.client.view.LiveChat;
import com.tubes.kantincepat.client.view.Order;
import com.tubes.kantincepat.client.view.MenuItem;
import com.tubes.kantincepat.client.view.OrderServices;
import com.tubes.kantincepat.client.view.User;
import com.tubes.kantincepat.client.view.Checkout;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.MouseEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.util.List;

public class ClientApp extends JFrame {

    // --- KONFIGURASI LAYAR ---
    public static final int MOBILE_WIDTH = 350;
    public static final int MOBILE_HEIGHT = 650;

    // --- DATA GLOBAL ---
    public List<MenuItem> cartItems = new ArrayList<>(); 
    public List<Order> orderHistory = new ArrayList<>();
    private User currentUser;

    // --- KOMPONEN UI ---
    private CardLayout cardLayout;
    private JPanel mainContentPanel; // Panel tengah (isi halaman)
    private JPanel bottomNavPanel;   // Panel bawah (navbar)
    private Map<String, RoundedPanel> navButtonsMap = new HashMap<>();

    // --- REFERENSI PANEL (Agar bisa direfresh) ---
    // (Ubah tipe data panel-panel ini agar menerima ClientApp di constructornya)
    private Menu_Bag menuBagPanel;
    private Menu_Orders menuOrdersPanel;
    private Menu_Invoice invoicePanel;
    private Menu_Detail menuDetailPanel;

    public ClientApp() {
        setTitle("Kantin Pintar");
        setSize(MOBILE_WIDTH, MOBILE_HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. SETUP CONTAINER UTAMA
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        // 2. INISIALISASI SEMUA PANEL
        // Perhatikan: Kita mengirim 'this' (ClientApp) ke constructor panel
        menuBagPanel = new Menu_Bag(this);
        menuOrdersPanel = new Menu_Orders(this);
        invoicePanel = new Menu_Invoice(this);
        menuDetailPanel = new Menu_Detail(this);

        // 3. DAFTARKAN HALAMAN KE CARD LAYOUT
        // Halaman Auth
        mainContentPanel.add(new LoginPanel(this), "LOGIN");
        mainContentPanel.add(new RegisterPanel(this), "REGISTER");

        // Halaman Utama Aplikasi
        mainContentPanel.add(new Menu_Utama(this), "HOME"); 
        mainContentPanel.add(menuBagPanel, "BAG");
        mainContentPanel.add(menuOrdersPanel, "ORDERS");
        mainContentPanel.add(new Menu_Profile(this), "PROFILE");
        
        // Halaman Detail/Fitur
        mainContentPanel.add(new Checkout(this), "SUCCESS"); 
        mainContentPanel.add(new LiveChat(this), "CHAT");
        mainContentPanel.add(invoicePanel, "INVOICE");
        mainContentPanel.add(menuDetailPanel, "DETAIL");

        // 4. SETUP NAVBAR (Disembunyikan dulu di awal)
        bottomNavPanel = createBottomNav();
        bottomNavPanel.setVisible(false); // Default hide (karena awal buka Login)

        // 5. SUSUN KE FRAME
        add(mainContentPanel, BorderLayout.CENTER);
        add(bottomNavPanel, BorderLayout.SOUTH);

        // Tampilkan Login saat pertama buka
        showView("LOGIN");
    }

    // --- METHOD NAVIGASI UTAMA ---
    public void showView(String viewName) {
        cardLayout.show(mainContentPanel, viewName);
        
        // Logika Navbar: Hanya muncul di halaman utama
        if (viewName.equals("LOGIN") || viewName.equals("REGISTER")) {
            bottomNavPanel.setVisible(false);
        } else {
            // Jika masuk ke halaman aplikasi (HOME, BAG, dll), munculkan navbar
            bottomNavPanel.setVisible(true);
            
            // Update warna tombol navbar jika halaman tersebut ada di navbar
            if (navButtonsMap.containsKey(viewName)) {
                updateNavColors(viewName);
            }
        }

        // Logika Refresh Data Khusus
        if (viewName.equals("BAG")) {
            menuBagPanel.refreshCartData();
        } else if (viewName.equals("ORDERS")) {
            menuOrdersPanel.refreshOrderData();
        }
    }

    // --- Helper Methods Lain (Pindahan dari MainApp) ---

    public void showInvoice(Order order) {
        invoicePanel.setOrderData(order);
        showView("INVOICE");
    }

    public void showMenuDetail(MenuItem item) {
        menuDetailPanel.showMenu(item);
        showView("DETAIL");
    }

    public void addToCart(MenuItem item) {
        cartItems.add(item);
        System.out.println("Menambahkan: " + item.name);
        menuBagPanel.refreshCartData(); 
    }

    // --- NAVBAR LOGIC ---

    private JPanel createBottomNav() {
        JPanel navPanel = new JPanel(new GridLayout(1, 4, 15, 0)); 
        navPanel.setBackground(Color.WHITE);
        navPanel.setPreferredSize(new Dimension(getWidth(), 60)); 
        navPanel.setBorder(new EmptyBorder(10, 15, 10, 15)); 

        navPanel.add(createNavItem("nav_home.png", "HOME", true));
        navPanel.add(createNavItem("nav_bag.png", "BAG", false));
        navPanel.add(createNavItem("nav_orders.png", "ORDERS", false));
        navPanel.add(createNavItem("nav_profile.png", "PROFILE", false));

        return navPanel;
    }

    private JPanel createNavItem(String imagePath, String pageId, boolean isDefaultActive) {
        Color initialColor = isDefaultActive ? COLOR_PRIMARY : COLOR_ACCENT;
        RoundedPanel container = new RoundedPanel(20, initialColor);

        container.setLayout(new GridBagLayout()); 
        
        navButtonsMap.put(pageId, container);

        JPanel contentBox = new JPanel();
        contentBox.setLayout(new BoxLayout(contentBox, BoxLayout.Y_AXIS));
        contentBox.setOpaque(false);

        try {
            ImageIcon originalIcon = new ImageIcon(imagePath);
            Image scaledImage = originalIcon.getImage().getScaledInstance(25, 25, Image.SCALE_SMOOTH);
            contentBox.add(new JLabel(new ImageIcon(scaledImage)));
        } catch (Exception e) {
            contentBox.add(new JLabel("IMG"));
        }
        
        container.add(contentBox);

        container.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showView(pageId); 
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                container.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        });
        return container;
    }

    private void updateNavColors(String activePageId) {
        for (Map.Entry<String, RoundedPanel> entry : navButtonsMap.entrySet()) {
            if (entry.getKey().equals(activePageId)) {
                entry.getValue().setColor(COLOR_PRIMARY);
            } else {
                entry.getValue().setColor(COLOR_ACCENT);
            }
            entry.getValue().repaint();
        }
    }

    public void setCurrentUser(int id, String name, String phone) {
        // Simpan data user yang login ke object User
        // Role default CUSTOMER dulu, nanti bisa diambil dari response juga kalau perlu
        this.currentUser = new User(id, name, phone, "CUSTOMER"); 
        
        System.out.println("User Login: ID=" + id + ", Name=" + name);
        
        // Opsional: Refresh data halaman lain jika perlu
        // menuProfilePanel.refreshData(); 
    }

    // --- 3. METHOD UNTUK GET USER SAAT TRANSAKSI ---
    public User getCurrentUser() {
        return currentUser;
    }
    
    // --- 4. UPDATE METHOD LOGOUT ---
    public void logout() {
        cartItems.clear();
        orderHistory.clear();
        this.currentUser = null; // Hapus session
        showView("LOGIN");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ClientApp().setVisible(true);
        });
    }
}