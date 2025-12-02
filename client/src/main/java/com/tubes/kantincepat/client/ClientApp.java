package com.tubes.kantincepat.client;

import com.tubes.kantincepat.client.view.*;
import com.tubes.kantincepat.client.view.MenuItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientApp extends JFrame {

    public static final int MOBILE_WIDTH = 350;
    public static final int MOBILE_HEIGHT = 650;

    private List<MenuItem> cartItems = new ArrayList<>();
    private User currentUser;

    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel bottomNavPanel;

    private Map<String, RoundedPanel> navButtonsMap = new HashMap<>();

    private Menu_Utama menuUtamaPanel;
    private Menu_Bag menuBagPanel;

    public ClientApp() {
        setTitle("Kantin Pintar - Customer App");
        setSize(MOBILE_WIDTH, MOBILE_HEIGHT);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Setup CardLayout (Tumpukan Halaman)
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        menuUtamaPanel = new Menu_Utama(this);
        menuBagPanel = new Menu_Bag(this);

        // 'this' dilempar ke panel supaya panel bisa panggil method showView()
        mainPanel.add(new LoginPanel(this), "LOGIN");
        mainPanel.add(new RegisterPanel(this), "REGISTER");
        mainPanel.add(menuUtamaPanel, "HOME");
        mainPanel.add(menuBagPanel, "BAG");

        bottomNavPanel = createBottomNav();
        bottomNavPanel.setVisible(false);

        add(mainPanel, BorderLayout.CENTER);
        add(bottomNavPanel, BorderLayout.SOUTH);

        showView("LOGIN");
    }

    // Method Publik agar Panel anak bisa minta ganti halaman
    public void showView(String viewName) {
        cardLayout.show(mainPanel, viewName);

        if (viewName.equals("LOGIN") || viewName.equals("REGISTER")) {
            bottomNavPanel.setVisible(false);
        } else {
            bottomNavPanel.setVisible(true); // Munculkan di halaman lain
            
            // Update warna tombol navbar aktif
            updateNavColors(viewName);
        }

        if (viewName.equals("BAG")) {
            menuBagPanel.refreshCartData(); // Pastikan method ini ada di Menu_Bag
       }
    }

    public void addToCart(MenuItem item) {
        cartItems.add(item);
        System.out.println("Item ditambahkan: " + item.name + " | Total: " + cartItems.size());
        
        // Notifikasi popup sederhana
        JOptionPane.showMessageDialog(this, item.name + " masuk keranjang!");
    }

    public void showMenuDetail(MenuItem item) {
        JOptionPane.showMessageDialog(this, 
           "Detail Menu:\n" + item.name + "\n" + item.description, 
           "Detail Info", 
           JOptionPane.INFORMATION_MESSAGE);
   }

    public List<MenuItem> getCartItems() {
        return cartItems;
    }

    public void setCurrentUser(int id, String username, String email, String fullName, String phone, String role) {
        this.currentUser = new User(id, username, email, fullName, phone, role);
        System.out.println("Login sebagai: " + username + " (" + role + ")");
    }

    public User getCurrentUser() {
        return currentUser;
    }
    
    public void logout() {
        cartItems.clear();
        currentUser = null;
        showView("LOGIN");
    }

    private JPanel createBottomNav() {
        JPanel navPanel = new JPanel(new GridLayout(1, 4, 15, 0)); 
        navPanel.setBackground(Color.WHITE);
        navPanel.setPreferredSize(new Dimension(getWidth(), 70)); 
        navPanel.setBorder(new EmptyBorder(10, 15, 10, 15)); 

        // Tambahkan tombol-tombol
        // Pastikan nama file gambar ("nav_home.png") ada di folder resources/assets/
        navPanel.add(createNavItem("home.png", "HOME", true));
        navPanel.add(createNavItem("bag.png", "BAG", false));
        navPanel.add(createNavItem("orders.png", "ORDERS", false));
        navPanel.add(createNavItem("profile.png", "PROFILE", false));

        return navPanel;
    }

    private JPanel createNavItem(String imagePath, String pageId, boolean isDefaultActive) {
        // Warna background tombol
        Color activeColor = GUIUtils.COLOR_PRIMARY;
        Color inactiveColor = GUIUtils.COLOR_ACCENT;
        
        RoundedPanel container = new RoundedPanel(20, isDefaultActive ? activeColor : inactiveColor);
        container.setLayout(new GridBagLayout()); 
        
        navButtonsMap.put(pageId, container); // Simpan ke Map biar bisa diakses nanti

        // Load Icon
        ImageIcon icon = GUIUtils.loadImageIcon(imagePath, 25, 25);
        if (icon != null) {
            container.add(new JLabel(icon));
        } else {
            container.add(new JLabel(pageId)); // Teks cadangan kalau gambar gak ada
        }
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
                entry.getValue().setColor(GUIUtils.COLOR_PRIMARY); // Aktif
            } else {
                entry.getValue().setColor(GUIUtils.COLOR_ACCENT); // Tidak Aktif
            }
            entry.getValue().repaint();
        }
    }
    public void showInvoice(Order order) {
        // Nanti kita buat panel Invoice beneran.
        // Sekarang pakai Popup dulu biar ga error.
        JOptionPane.showMessageDialog(this, 
            "INVOICE #" + order.orderId + "\n" +
            "Tanggal: " + order.date + "\n" +
            "Total: Rp " + order.totalPrice + "\n\n" +
            "Items:\n" + order.itemsSummary,
            "Invoice Digital",
            JOptionPane.INFORMATION_MESSAGE);
    }


    public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> {
            new ClientApp().setVisible(true);
        });
    }
}