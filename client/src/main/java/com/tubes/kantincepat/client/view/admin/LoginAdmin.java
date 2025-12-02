package com.tubes.kantincepat.client.view.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginAdmin extends JFrame {

    // Komponen Input
    private JTextField txtUser;
    private JPasswordField txtPass;
    private JButton btnLogin;

    public LoginAdmin() {
        setTitle("Login Admin - Kantin Pintar");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2)); // Split 50:50

        // --- PANEL KIRI (GAMBAR FULL + OVERLAY) ---
        JPanel leftPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Load Gambar Full Cover
                try {
                    // Pastikan file 'kantin.jpg' ada di src/main/resources/images/
                    ImageIcon icon = new ImageIcon(getClass().getResource("/images/kantin-pintar.jpg"));
                    Image img = icon.getImage();
                    g2d.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                    
                    // Overlay Gradient Gelap (Bawah ke Atas)
                    GradientPaint gp = new GradientPaint(0, getHeight(), new Color(0,0,0, 200), 0, 0, new Color(0,0,0, 0));
                    g2d.setPaint(gp);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                    
                    // Overlay Warna Tema Tipis (Pink Transparan)
                    g2d.setColor(new Color(AppColor.COLOR_PRIMARY.getRed(), AppColor.COLOR_PRIMARY.getGreen(), AppColor.COLOR_PRIMARY.getBlue(), 100));
                    g2d.fillRect(0, 0, getWidth(), getHeight());

                } catch (Exception e) {
                    // Fallback kalau gambar tidak ketemu
                    g2d.setColor(AppColor.COLOR_PRIMARY);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        leftPanel.setLayout(new GridBagLayout());
        
        // Teks Judul Besar di atas Gambar
        JLabel lblBrand = new JLabel("<html><div style='text-align: center; letter-spacing: 5px;'>KANTIN<br>PINTAR</div></html>");
        lblBrand.setFont(new Font("SansSerif", Font.BOLD, 48));
        lblBrand.setForeground(Color.WHITE);
        leftPanel.add(lblBrand);


        // --- PANEL KANAN (FORM LOGIN MODERN) ---
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new GridBagLayout()); // Center Form Vertikal

        // Container Form (Wrapper biar rapi)
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));
        formContainer.setBackground(Color.WHITE);
        formContainer.setBorder(new EmptyBorder(0, 60, 0, 60)); // Padding Kanan Kiri Lega

        // Header Text
        JLabel lblTitle = new JLabel("Selamat Datang");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 28));
        lblTitle.setForeground(AppColor.TEXT_MAIN);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Silakan login untuk melanjutkan");
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblSub.setForeground(Color.GRAY);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Input Username
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(AppColor.FONT_BOLD);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtUser = createModernTextField();
        
        // Input Password
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(AppColor.FONT_BOLD);
        lblPass.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPass = createModernPasswordField();

        // Checkbox Show Password
        JCheckBox chkShow = new JCheckBox("Lihat Password");
        chkShow.setFont(new Font("SansSerif", Font.PLAIN, 12));
        chkShow.setBackground(Color.WHITE);
        chkShow.setForeground(Color.GRAY);
        chkShow.setFocusPainted(false);
        chkShow.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkShow.addActionListener(e -> {
            if(chkShow.isSelected()) txtPass.setEchoChar((char)0);
            else txtPass.setEchoChar('•');
        });

        // Tombol Login Besar (Full Width)
        btnLogin = new JButton("MASUK SEKARANG");
        styleBigButton(btnLogin);
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // --- LOGIC LOGIN ---
        btnLogin.addActionListener(e -> {
            String user = txtUser.getText();
            String pass = new String(txtPass.getPassword());

            // Logic Sederhana (Nanti bisa diganti Socket)
            if (user.equals("admin") && pass.equals("admin123")) {
                new AdminDashboard().setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Username/Password Salah!", "Gagal Login", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Menyusun Komponen (Layouting dengan Spasi)
        formContainer.add(lblTitle);
        formContainer.add(Box.createRigidArea(new Dimension(0, 5)));
        formContainer.add(lblSub);
        formContainer.add(Box.createRigidArea(new Dimension(0, 40)));
        
        formContainer.add(lblUser);
        formContainer.add(Box.createRigidArea(new Dimension(0, 5)));
        formContainer.add(txtUser);
        formContainer.add(Box.createRigidArea(new Dimension(0, 20)));
        
        formContainer.add(lblPass);
        formContainer.add(Box.createRigidArea(new Dimension(0, 5)));
        formContainer.add(txtPass);
        formContainer.add(Box.createRigidArea(new Dimension(0, 5)));
        formContainer.add(chkShow);
        
        formContainer.add(Box.createRigidArea(new Dimension(0, 40)));
        formContainer.add(btnLogin);

        rightPanel.add(formContainer);

        // Add Panels to Frame
        add(leftPanel);
        add(rightPanel);
    }

    // --- HELPER METODE DESIGN (PRIVATE) ---

    private JTextField createModernTextField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(300, 45));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(15), // Border Melengkung
            new EmptyBorder(0, 15, 0, 15) // Padding Teks Dalam
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private JPasswordField createModernPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setPreferredSize(new Dimension(300, 45));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(15), 
            new EmptyBorder(0, 15, 0, 15)
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private void styleBigButton(JButton btn) {
        btn.setPreferredSize(new Dimension(300, 50));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        btn.setBackground(AppColor.COLOR_PRIMARY); // Warna Pink dari AppColor
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efek Hover
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(AppColor.COLOR_PRIMARY.darker()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(AppColor.COLOR_PRIMARY); }
        });
    }

    // Class Border Rounded Custom (Inner Class)
    private static class RoundedBorder extends javax.swing.border.AbstractBorder {
        private int radius;
        public RoundedBorder(int radius) { this.radius = radius; }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.LIGHT_GRAY); // Warna Garis Pinggir
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginAdmin().setVisible(true));
    }
}