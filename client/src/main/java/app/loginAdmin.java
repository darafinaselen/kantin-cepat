package app;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.net.URL;
import java.awt.geom.RoundRectangle2D;

public class loginAdmin extends JFrame {

    // Warna tema
    private static final Color COLOR_ORANGE = new Color(0xFFFFFF);
    private static final Color COLOR_BG_LIGHT  = new Color(0xCCF3D1);  // hijau pastel muda
    private static final Color COLOR_TEXT_DARK = new Color(0x263238);
    private static final Color COLOR_PRIMARY   = new Color(0x2E7D32);

    private final JTextField tfUsername     = new JTextField(20);
    private final JPasswordField pfPassword = new JPasswordField(20);
    private final JCheckBox cbShowPass      = new JCheckBox("Show password");
    private final RoundedButton btnLogin    = new RoundedButton("Login");

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> new loginAdmin().setVisible(true));
    }

    public loginAdmin() {
        super("Kantin Cepat — Login Admin");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        setLayout(new BorderLayout());
        add(buildLeftPanel(), BorderLayout.WEST);
        add(buildRightPanel(), BorderLayout.CENTER);

        getRootPane().setDefaultButton(btnLogin);
    }

    // ======================= PANEL KIRI ==========================
    private JPanel buildLeftPanel() {
        JPanel left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(360, getHeight()));
        // margin oranye di sekeliling kartu
        left.setBorder(new EmptyBorder(20, 20, 20, 20));
        left.setBackground(COLOR_ORANGE);

        // Load gambar background
        ImageIcon bgIcon = loadImage("/images/kantin.jpg");

        // Panel gambar rounded yang mengisi penuh panel kiri (minus border)
        RoundedImagePanel card = new RoundedImagePanel(bgIcon);
        card.setLayout(new BorderLayout());
        card.setOpaque(false);

        // Logo di pojok kiri atas gambar (seperti contoh)
        ImageIcon logoIcon = loadImage("/images/logo.png");
        JLabel logoLabel;
        if (logoIcon != null) {
            Image scaledLogo = logoIcon.getImage()
                    .getScaledInstance(70, 70, Image.SCALE_SMOOTH);
            logoLabel = new JLabel(new ImageIcon(scaledLogo));
        } else {
            logoLabel = new JLabel("Kantin Cepat");
        }

        JPanel logoWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        logoWrapper.setOpaque(false);
        logoWrapper.add(logoLabel);

        card.add(logoWrapper, BorderLayout.NORTH);

        // kartu langsung ditaruh di CENTER, jadi dia membesar mengikuti panel kiri
        left.add(card, BorderLayout.CENTER);

        return left;
    }

    private ImageIcon loadImage(String path) {
        try {
            URL url = getClass().getResource(path);
            System.out.println("Load image " + path + " -> " + url);
            if (url != null) {
                return new ImageIcon(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ======================= PANEL KANAN (FORM LOGIN) ==========================
    private JPanel buildRightPanel() {
        JPanel right = new JPanel(new GridBagLayout());
        right.setBackground(COLOR_BG_LIGHT);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230)),
                new EmptyBorder(24, 32, 28, 32)
        ));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;

        JLabel lblTitle = new JLabel("Login Admin");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(COLOR_TEXT_DARK);

        JLabel lblDesc = new JLabel("Masuk untuk mengelola menu dan pengguna.");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setForeground(new Color(120, 120, 120));

        c.gridy = 0; card.add(lblTitle, c);
        c.gridy = 1; card.add(lblDesc, c);

        c.gridy = 2; card.add(new JLabel("Username"), c);
        c.gridy = 3;
        styleTextField(tfUsername);
        tfUsername.setText("admin");
        card.add(tfUsername, c);

        c.gridy = 4; card.add(new JLabel("Password"), c);
        c.gridy = 5;
        styleTextField(pfPassword);
        pfPassword.setText("1234");
        pfPassword.setEchoChar('•');
        card.add(pfPassword, c);

        c.gridy = 6;
        cbShowPass.setOpaque(false);
        card.add(cbShowPass, c);

        // tombol Login
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnLogin.setBackground(COLOR_PRIMARY);
        btnLogin.setForeground(Color.WHITE);
        btnPanel.add(btnLogin);

        c.gridy = 7;
        c.insets = new Insets(20, 6, 6, 6);
        card.add(btnPanel, c);

        // action
        btnLogin.addActionListener(e -> doLogin());
        cbShowPass.addActionListener(e -> togglePassword());

        right.add(card);
        return right;
    }

    private void styleTextField(JTextField tf) {
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(190, 190, 190)),
                new EmptyBorder(6, 8, 6, 8)
        ));
    }

    private void togglePassword() {
        pfPassword.setEchoChar(cbShowPass.isSelected() ? 0 : '•');
    }

    private void doLogin() {
    String user = tfUsername.getText().trim();
    String pass = new String(pfPassword.getPassword());

    if (user.equals("admin") && pass.equals("1234")) {
        // buka frame dashboard
        SwingUtilities.invokeLater(() -> {
            new adminDashboard().setVisible(true);
        });
        dispose(); // tutup jendela login
    } else {
        JOptionPane.showMessageDialog(this,
                "Username atau password salah.",
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}


    // ======================= PANEL GAMBAR ROUNDED ==========================
    private static class RoundedImagePanel extends JPanel {
        private final Image image;

        public RoundedImagePanel(ImageIcon icon) {
            this.image = (icon != null) ? icon.getImage() : null;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (image == null) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            int arc = 40;

            Shape clip = new RoundRectangle2D.Float(
                    0, 0, getWidth(), getHeight(), arc, arc
            );
            g2.setClip(clip);

            int panelW = getWidth();
            int panelH = getHeight();
            int imgW = image.getWidth(null);
            int imgH = image.getHeight(null);

            // scale cover seperti CSS object-fit: cover
            float scale = Math.max((float) panelW / imgW, (float) panelH / imgH);
            int newW = (int) (imgW * scale);
            int newH = (int) (imgH * scale);

            int x = (panelW - newW) / 2;
            int y = (panelH - newH) / 2;

            g2.drawImage(image, x, y, newW, newH, null);
            g2.dispose();
        }
    }

    // ======================= BUTTON MODERN ==========================
    static class RoundedButton extends JButton {
        public RoundedButton(String text) {
            super(text);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
            setMargin(new Insets(8, 20, 8, 20));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color base = getBackground();
            if (getModel().isPressed())  base = base.darker();
            if (getModel().isRollover()) base = base.brighter();

            g2.setColor(base);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(getText())) / 2;
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), x, y);

            g2.dispose();
        }
    }
}
