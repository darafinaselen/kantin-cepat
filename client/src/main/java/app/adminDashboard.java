package app;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class AdminDashboard extends JFrame {

    private JPanel mainContentPanel;
    private CardLayout cardLayout;
    private SocketService socketService;

    public AdminDashboard() {
        setTitle("Admin Panel - Kantin Cepat");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. INIT SOCKET
        socketService = new SocketService();
        try {
            socketService.connect();
        } catch (Exception e) {
            System.out.println("Info: Server belum nyala, berjalan dalam mode Offline GUI.");
        }

        // 2. HEADER
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
        
        JButton btnLogout = StyleUtils.createRoundedButton("Logout", AppColor.BTN_RED, Color.WHITE);
        btnLogout.setPreferredSize(new Dimension(100, 40));
        btnLogout.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(this, "Keluar?", "Logout", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION){
                socketService.disconnect();
                new LoginAdmin().setVisible(true); dispose();
            }
        });

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(btnLogout, BorderLayout.EAST);

        // 3. SIDEBAR
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setBackground(Color.WHITE);
        sidebarPanel.setPreferredSize(new Dimension(260, getHeight()));
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBorder(new EmptyBorder(30, 15, 30, 15));

        addSidebarItem(sidebarPanel, "Kelola Menu", "MENU");
        addSidebarItem(sidebarPanel, "Kelola Pengguna", "USER");
        addSidebarItem(sidebarPanel, "Manajemen Pesanan", "ORDER");
        addSidebarItem(sidebarPanel, "Live Chat", "CHAT");

        // 4. MAIN CONTENT
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(AppColor.BACKGROUND);
        mainContentPanel.setBorder(new EmptyBorder(20, 20, 20, 20)); 
        
        mainContentPanel.add(new MenuPanel(socketService), "MENU");
        mainContentPanel.add(new UserPanel(socketService), "USER");
        mainContentPanel.add(new OrderPanel(), "ORDER");
        mainContentPanel.add(new ChatPanel(), "CHAT"); 

        add(headerPanel, BorderLayout.NORTH);
        add(sidebarPanel, BorderLayout.WEST);
        add(mainContentPanel, BorderLayout.CENTER);
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

    public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> new AdminDashboard().setVisible(true));
    }
}