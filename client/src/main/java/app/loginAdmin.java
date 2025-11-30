package app;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginAdmin extends JFrame {

    public LoginAdmin() {
        setTitle("Login Admin - Kantin Cepat");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(1, 2)); // Split 50:50

        // --- PANEL KIRI (Ungu & Gambar) ---
        JPanel leftPanel = new JPanel();
        // GANTI: Menggunakan Ungu Utama agar senada
        leftPanel.setBackground(AppColor.PRIMARY_PURPLE); 
        leftPanel.setLayout(new GridBagLayout()); // Untuk centering konten

        // Card Putih di dalam panel ungu
        JPanel imageCard = new JPanel();
        imageCard.setPreferredSize(new Dimension(300, 400));
        imageCard.setBackground(Color.WHITE);
        imageCard.setLayout(new BorderLayout()); 
        
        // Load Gambar kantin.jpg
        JLabel imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        try {
            // Mengambil gambar dari resources
            ImageIcon icon = new ImageIcon(getClass().getResource("/images/kantin.jpg"));
            // Resize gambar
            Image img = icon.getImage().getScaledInstance(280, 280, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            imageLabel.setText("Image not found");
        }

        imageCard.add(imageLabel, BorderLayout.CENTER);
        
        // Efek Rounded sederhana
        imageCard.setBorder(BorderFactory.createLineBorder(Color.WHITE, 10));
        leftPanel.add(imageCard);

        // --- PANEL KANAN (Form Login) ---
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(new GridBagLayout());
        
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(new EmptyBorder(20, 40, 20, 40));

        JLabel lblTitle = new JLabel("Login Admin");
        lblTitle.setFont(AppColor.FONT_HEADER);
        lblTitle.setForeground(AppColor.TEXT_MAIN);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Masuk untuk mengelola kantin");
        lblSub.setFont(AppColor.FONT_SUBTITLE);
        lblSub.setForeground(Color.GRAY);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Field Username
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(AppColor.FONT_BOLD);
        JTextField txtUser = new JTextField();
        styleTextField(txtUser);

        // Field Password
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(AppColor.FONT_BOLD);
        JPasswordField txtPass = new JPasswordField();
        styleTextField(txtPass);

        // Show Password Checkbox
        JCheckBox chkShow = new JCheckBox("Tampilkan Password");
        chkShow.setBackground(Color.WHITE);
        chkShow.setFont(AppColor.FONT_REGULAR);
        chkShow.addActionListener(e -> {
            if (chkShow.isSelected()) {
                txtPass.setEchoChar((char) 0);
            } else {
                txtPass.setEchoChar('•');
            }
        });

        // Tombol Login
        JButton btnLogin = new JButton("LOGIN");
        // GANTI: Tombol Login jadi Ungu
        styleButton(btnLogin, AppColor.PRIMARY_PURPLE, Color.WHITE); 
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Action Login
        btnLogin.addActionListener(e -> {
            String user = txtUser.getText();
            String pass = new String(txtPass.getPassword());

            // TODO: Sambungkan ke Database/Socket Server Developer A nanti
            if (user.equals("admin") && pass.equals("admin123")) {
                new AdminDashboard().setVisible(true);
                this.dispose(); // Tutup window login
            } else {
                JOptionPane.showMessageDialog(this, "Username atau Password salah!", "Login Gagal", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Menyusun Komponen
        formPanel.add(lblTitle);
        formPanel.add(lblSub);
        formPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        formPanel.add(lblUser);
        formPanel.add(txtUser);
        formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(lblPass);
        formPanel.add(txtPass);
        formPanel.add(chkShow);
        formPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        formPanel.add(btnLogin);

        rightPanel.add(formPanel);

        // Add panels to frame
        add(leftPanel);
        add(rightPanel);
    }

    // Helper Styling Text Field
    private void styleTextField(JTextField field) {
        field.setPreferredSize(new Dimension(300, 35));
        field.setMaximumSize(new Dimension(400, 35));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    // Helper Styling Button
    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setPreferredSize(new Dimension(300, 45));
        btn.setMaximumSize(new Dimension(400, 45));
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efek Hover
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginAdmin().setVisible(true));
    }
}