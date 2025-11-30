package com.tubes.kantincepat.client.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.tubes.kantincepat.client.ClientApp;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class LiveChat extends JPanel {

    private ClientApp ClientApp;
    private JPanel messageListPanel; // Panel tempat menampung bubble chat

    public LiveChat(ClientApp app) {
        this.ClientApp = app;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 1. Header (Tombol Back, Foto Kasir, Status)
        add(createHeader(), BorderLayout.NORTH);

        // 2. Message Area (Scrollable)
        messageListPanel = new JPanel();
        messageListPanel.setLayout(new BoxLayout(messageListPanel, BoxLayout.Y_AXIS));
        messageListPanel.setBackground(Color.WHITE);
        messageListPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // simulasi percakapan
        addMessage("Hai, ada mie ayam? aku mau banget makan mie ayam nih. aku kelaperan", true);
        addMessage("Ada, mbak", false);
        addMessage("Asik!!", true);
        addMessage("Hai, ada mie ayam?", true);
        addMessage("Ada, mbak", false);
        addMessage("Asik!!", true);
        addMessage("Hai, ada mie ayam?", true);
        
        // Scroll Pane agar bisa di-scroll
        JScrollPane scrollPane = new JScrollPane(messageListPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Input Area (Bawah)
        add(createInputArea(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));
        
        // Tombol Back
        JLabel btnBack = new JLabel("←");
        btnBack.setFont(new Font("SansSerif", Font.BOLD, 24));
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ClientApp.showView("HOME"); 
            }
        });

        // Info Kasir (Tengah/Kiri dikit)
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        infoPanel.setBackground(Color.WHITE);
        
        // Foto Kasir
        RoundedPanel avatar = new RoundedPanel(40, ClientApp.COLOR_ACCENT);
        avatar.setPreferredSize(new Dimension(40, 40));
        avatar.setLayout(new GridBagLayout());
        try {
            ImageIcon originalIcon = GUIUtils.loadImageIcon("profile.png", 35,35); // Pastikan nama file sesuai
            // Image scaledImage = originalIcon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH);
            JLabel lblAvatar = new JLabel(new ImageIcon(originalIcon.getImage()));
            avatar.add(lblAvatar);
        } catch (Exception e) {
            avatar.add(new JLabel("IMG")); // Fallback jika gambar tidak ketemu
        }

        // Nama & Status
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(Color.WHITE);
        JLabel lblName = new JLabel("Cashier");
        lblName.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        JLabel lblStatus = new JLabel("● Online");
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblStatus.setForeground(new Color(50, 200, 50)); // Warna Hijau

        textPanel.add(lblName);
        textPanel.add(lblStatus);

        infoPanel.add(btnBack);
        infoPanel.add(avatar);
        infoPanel.add(textPanel);

        panel.add(infoPanel, BorderLayout.WEST);
        return panel;
    }

    private void addMessage(String text, boolean isSender) {
        // Gunakan BorderLayout untuk baris pesan agar bubble tidak melar
        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setBorder(new EmptyBorder(5, 0, 5, 0)); // Jarak vertikal antar pesan

        // 1. SIAPKAN FONT & UKURAN TEKS
        Font fontChat = new Font("SansSerif", Font.PLAIN, 14);
        
        // Hitung lebar teks
        Canvas c = new Canvas(); 
        FontMetrics fm = c.getFontMetrics(fontChat);
        int textWidth = fm.stringWidth(text);
        
        // Batas lebar sebelum teks turun baris (Wrapping)
        int maxTextWidth = 140; 
        
        // 2. LOGIKA HTML UNTUK TEXT WRAPPING
        String htmlText;
        if (textWidth > maxTextWidth) {
            // Jika teks panjang, paksa lebar fix agar turun baris
            // Tambahkan padding/margin 0 di body agar tidak ada ruang kosong misterius
            htmlText = "<html><body style='width: " + maxTextWidth + "px; padding: 0px; margin: 0px;'>" 
                       + text + "</body></html>";
        } else {
            // Jika teks pendek, biarkan ukurannya alami
            htmlText = "<html><body style='padding: 0px; margin: 0px;'>" 
                       + text + "</body></html>";
        }

        // 3. BUAT LABEL PESAN
        JLabel lblText = new JLabel(htmlText);
        lblText.setFont(fontChat);

        if (isSender) {
            // --- USER (KANAN) ---
            // Gunakan GridBagLayout di dalam bubble agar label pas di tengah padding
            RoundedPanel bubble = new RoundedPanel(20, new Color(225, 180, 205)); // Pink
            bubble.setLayout(new GridBagLayout()); 
            bubble.setBorder(new EmptyBorder(8, 12, 8, 12)); // Padding dalam bubble
            bubble.add(lblText);

            // Masukkan bubble ke sisi KANAN (EAST)
            // BorderLayout.EAST tidak akan menarik lebar komponen (Shrink-wrap)
            rowPanel.add(bubble, BorderLayout.EAST);

        } else {
            // --- KASIR (KIRI) ---
            // Kita butuh container 'FlowLayout' untuk menggabungkan Avatar + Bubble
            // agar mereka menempel rapi di kiri
            JPanel leftContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            leftContainer.setBackground(Color.WHITE);
            
            // Avatar
            RoundedPanel avatar = new RoundedPanel(30, ClientApp.COLOR_ACCENT);
            avatar.setPreferredSize(new Dimension(30, 30));
            avatar.setLayout(new GridBagLayout());
            avatar.add(new JLabel("8")); // Placeholder icon
            
            // Bubble Abu
            RoundedPanel bubble = new RoundedPanel(20, new Color(240, 240, 240)); 
            bubble.setLayout(new GridBagLayout());
            bubble.setBorder(new EmptyBorder(8, 12, 8, 12)); 
            bubble.add(lblText);

            leftContainer.add(avatar);
            leftContainer.add(bubble);

            // Masukkan container gabungan ke sisi KIRI (WEST)
            rowPanel.add(leftContainer, BorderLayout.WEST);
        }

        messageListPanel.add(rowPanel);
        messageListPanel.revalidate();
        messageListPanel.repaint();

        // LOGIKA AUTO SCROLL KE BAWAH
        SwingUtilities.invokeLater(() -> {
            // Mengambil ScrollPane induk dari messageListPanel
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, messageListPanel);
            if (scrollPane != null) {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }

    private JPanel createInputArea() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(10, 20, 20, 20)); 

        // Panel Input yang Rounded
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        
        // Custom Rounded Background untuk Input Field
        JPanel inputBg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 248, 250)); // Abu sangat muda
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
            }
        };
        inputBg.setLayout(new BorderLayout());
        inputBg.setBorder(new EmptyBorder(5, 20, 5, 10)); // Padding dalam textfield
        inputBg.setPreferredSize(new Dimension(100, 50));

        // TextField
        String placeholder = "Write your message";
        JTextField textField = new JTextField(placeholder);
        textField.setBorder(null);
        textField.setOpaque(false); 
        textField.setForeground(Color.GRAY); // Warna awal Abu-abu

        // Tambahkan Listener untuk efek Placeholder
        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                // Saat diklik: Hapus placeholder, warna jadi Hitam
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                // Saat ditinggalkan: Jika kosong, kembalikan placeholder warna Abu
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(Color.GRAY);
                }
            }
        });

        // Icon Mic & Send (Kanan)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        
        JLabel btnSend = new JLabel("➤"); // Ganti icon_send.png
        btnSend.setForeground(ClientApp.COLOR_PRIMARY); // Warna Pink

        btnSend.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                sendMessage(textField, placeholder);
            }
        });
        textField.addActionListener(e -> sendMessage(textField, placeholder));

        actions.add(btnSend);

        inputBg.add(textField, BorderLayout.CENTER);
        inputBg.add(actions, BorderLayout.EAST);

        container.add(inputBg, BorderLayout.CENTER);
        return container;
    }

    private void sendMessage(JTextField textField, String placeholder) {
        String text = textField.getText().trim();

        // Validasi: Jangan kirim jika kosong atau isinya masih placeholder
        if (!text.isEmpty() && !text.equals(placeholder)) {
            
            // 1. Tambahkan pesan ke layar sebagai Sender (Kanan)
            addMessage(text, true);
            
            // 2. Bersihkan TextField
            textField.setText("");
            
            // 3. (Opsional) Simulasi balasan otomatis kasir
            // Timer timer = new Timer(1000, e -> addMessage("Baik kak, ditunggu ya!", false));
            // timer.setRepeats(false);
            // timer.start();
        }
    }
}