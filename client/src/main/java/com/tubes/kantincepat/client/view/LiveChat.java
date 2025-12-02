package com.tubes.kantincepat.client.view;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.tubes.kantincepat.client.ClientApp;
import com.tubes.kantincepat.client.net.ChatClient;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class LiveChat extends JPanel {

    private ClientApp mainApp;
    private JPanel messageListPanel; // Panel tempat menampung bubble chat
    
    private JLabel headerNameLabel;
    private JLabel headerAvatarInitialLabel;

    private ChatClient chatClient;
    private int currentUserId;
    
    public LiveChat(ClientApp app) {
        this.mainApp = app;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // JANGAN ambil currentUserId di sini
        // JANGAN inisialisasi ChatClient di sini

        add(createHeader(), BorderLayout.NORTH);

        messageListPanel = new JPanel();
        messageListPanel.setLayout(new BoxLayout(messageListPanel, BoxLayout.Y_AXIS));
        messageListPanel.setBackground(Color.WHITE);
        messageListPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(messageListPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        add(createInputArea(), BorderLayout.SOUTH);
    }

    public void initChatForCurrentUser() {
        // kalau sudah pernah di-init, tidak usah dua kali
        if (this.chatClient != null) return;

        this.currentUserId = mainApp.getCurrentUserId();
        System.out.println("[LiveChat] init dengan userId = " + currentUserId);

        refreshHeaderInfo();

        try {
            chatClient = new ChatClient(currentUserId);
            chatClient.setListener((senderId, message) -> {
                boolean isSender = (senderId == currentUserId);
                SwingUtilities.invokeLater(() -> addMessage(message, isSender));
            });
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Tidak bisa konek ke server chat");
        }
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
                mainApp.showView("HOME");
            }
        });

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        infoPanel.setBackground(Color.WHITE);

        // Avatar bulat
        RoundedPanel avatar = new RoundedPanel(40, mainApp.COLOR_ACCENT);
        avatar.setPreferredSize(new Dimension(40, 40));
        avatar.setLayout(new GridBagLayout());

        headerAvatarInitialLabel = new JLabel("?");          // placeholder dulu
        headerAvatarInitialLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerAvatarInitialLabel.setForeground(Color.WHITE);
        avatar.add(headerAvatarInitialLabel);

        // Nama & status
        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(Color.WHITE);

        headerNameLabel = new JLabel("");                    // diisi nanti
        headerNameLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

        JLabel lblStatus = new JLabel("● Online");
        lblStatus.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblStatus.setForeground(new Color(50, 200, 50));

        textPanel.add(headerNameLabel);
        textPanel.add(lblStatus);

        infoPanel.add(btnBack);
        infoPanel.add(avatar);
        infoPanel.add(textPanel);

        panel.add(infoPanel, BorderLayout.WEST);

        // isi awal (kalau sudah ada user)
        refreshHeaderInfo();

        return panel;
    }

    private void refreshHeaderInfo() {
        if (headerNameLabel == null || headerAvatarInitialLabel == null) return;

        String role = mainApp.getCurrentUserRole();
        String selfName = mainApp.getCurrentUserName();

        // 1. Nama lawan chat
        String friendName;
        if ("ADMIN".equals(role)) {
            // nanti bisa diganti jadi nama customer aktif
            friendName = "Customer";
        } else {
            // default: CUSTOMER atau belum login
            friendName = "Kasir";
        }
        headerNameLabel.setText(friendName);

        // 2. Inisial avatar:
        //    - kalau ADMIN -> "K"
        //    - kalau CUSTOMER -> inisial nama user dari DB
        String initial;
        if ("CUSTOMER".equals(role)) {
            initial = "K";
        } else {
            initial = "C";
        }
        headerAvatarInitialLabel.setText(initial);
    }

    private void addMessage(String text, boolean isSender) {
        // Gunakan BorderLayout untuk baris pesan agar bubble tidak melar
        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setBorder(new EmptyBorder(5, 0, 5, 0)); // Jarak vertikal antar pesan

        // 1. SIAPKAN FONT & UKURAN TEKS
        Font fontChat = new Font("SansSerif", Font.PLAIN, 14);

        Canvas c = new Canvas();
        FontMetrics fm = c.getFontMetrics(fontChat);
        int textWidth = fm.stringWidth(text);

        int maxTextWidth = 140;

        String htmlText;
        if (textWidth > maxTextWidth) {
            htmlText = "<html><body style='width: " + maxTextWidth + "px; padding: 0px; margin: 0px;'>"
                    + text + "</body></html>";
        } else {
            htmlText = "<html><body style='padding: 0px; margin: 0px;'>"
                    + text + "</body></html>";
        }

        JLabel lblText = new JLabel(htmlText);
        lblText.setFont(fontChat);

        if (isSender) {
            // USER (KANAN)
            RoundedPanel bubble = new RoundedPanel(20, new Color(225, 180, 205));
            bubble.setLayout(new GridBagLayout());
            bubble.setBorder(new EmptyBorder(8, 12, 8, 12));
            bubble.add(lblText);

            rowPanel.add(bubble, BorderLayout.EAST);

        } else {
            JPanel leftContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            leftContainer.setBackground(Color.WHITE);

            String role = mainApp.getCurrentUserRole();
            String senderName;

            if (isSender) {
                // bubble kiri tapi dikirim oleh user yang sedang login (jarang kejadian, tapi amanin aja)
                senderName = mainApp.getCurrentUserName() != null
                        ? mainApp.getCurrentUserName()
                        : "Me";
            } else {
                // lawan chat
                if ("ADMIN".equals(role)) {
                    senderName = "Customer";   // nanti tinggal ganti jadi nama customer aktif
                } else {
                    senderName = "Kasir";
                }
            }

            String initial = senderName.substring(0, 1).toUpperCase();

            RoundedPanel avatar = new RoundedPanel(30, mainApp.COLOR_ACCENT);
            avatar.setPreferredSize(new Dimension(30, 30));
            avatar.setLayout(new GridBagLayout());

            JLabel lblInit = new JLabel(initial);
            lblInit.setFont(new Font("SansSerif", Font.BOLD, 14));
            lblInit.setForeground(Color.WHITE);
            avatar.add(lblInit);

            RoundedPanel bubble = new RoundedPanel(20, new Color(240, 240, 240));
            bubble.setLayout(new GridBagLayout());
            bubble.setBorder(new EmptyBorder(8, 12, 8, 12));
            bubble.add(lblText);

            leftContainer.add(avatar);
            leftContainer.add(bubble);

            rowPanel.add(leftContainer, BorderLayout.WEST);
        }


        // >>> INI BAGIAN PENTING BIAR TIDAK FULL HEIGHT <<<
        // Biar BoxLayout Y gak nge-stretch tinggi setiap row
        rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT); // aman untuk semua
        rowPanel.doLayout();                              // hitung ukuran dulu
        Dimension pref = rowPanel.getPreferredSize();
        // width boleh melebar, tinggi dikunci ke tinggi konten
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));

        // Tambah ke panel list
        messageListPanel.add(rowPanel);
        messageListPanel.revalidate();
        messageListPanel.repaint();

        // Auto scroll ke bawah
        SwingUtilities.invokeLater(() -> {
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(
                    JScrollPane.class, messageListPanel);
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
        btnSend.setForeground(mainApp.COLOR_PRIMARY); // Warna Pink

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

        if (!text.isEmpty() && !text.equals(placeholder)) {

            // tampilkan di UI sebagai pengirim
            // addMessage(text, true);

            // kirim ke server
            if (chatClient != null) {
                chatClient.sendChat(text);
            }

            textField.setText("");
        }
    }

}