package com.tubes.kantincepat.client.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.tubes.kantincepat.client.ClientApp;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;

public class LiveChat extends JPanel {

    private ClientApp mainApp;
    private JPanel messageListPanel; 
    private JScrollPane scrollPane;

    public LiveChat(ClientApp app) {
        this.mainApp = app;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 1. Header (Tombol Back, Foto Kasir, Status)
        add(createHeader(), BorderLayout.NORTH);

        // 2. Message Area (Scrollable)
        messageListPanel = new JPanel();
        messageListPanel.setLayout(new BoxLayout(messageListPanel, BoxLayout.Y_AXIS));
        messageListPanel.setBackground(Color.WHITE);
        messageListPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        scrollPane = new JScrollPane(messageListPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Input Area (Bawah)
        add(createInputArea(), BorderLayout.SOUTH);
    }

    public void loadChatData() {
        messageListPanel.removeAll();
        
        if (mainApp.getCurrentUser() != null) {
            int myId = mainApp.getCurrentUser().getId();
            List<ChatMessage> chats = ChatServices.getChatHistory(myId);
            
            if (chats.isEmpty()) {
                // Tampilkan pesan selamat datang jika kosong
                addMessage("Halo! Ada yang bisa kami bantu?", false);
            } else {
                for (ChatMessage chat : chats) {
                    // Cek apakah pengirim adalah SAYA sendiri
                    boolean isSender = (chat.senderId == myId);
                    addMessage(chat.message, isSender);
                }
            }
        }
        
        messageListPanel.revalidate();
        messageListPanel.repaint();
        scrollToBottom();
    }
    private void addMessage(String text, boolean isSender) {
        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setBorder(new EmptyBorder(5, 0, 5, 0));

        Font fontChat = new Font("SansSerif", Font.PLAIN, 14);
        
        // Simple HTML wrapping
        String htmlText = "<html><body style='width: 180px; padding: 0px; margin: 0px;'>" 
                        + text + "</body></html>";

        JLabel lblText = new JLabel(htmlText);
        lblText.setFont(fontChat);

        if (isSender) {
            // --- USER (KANAN) ---
            RoundedPanel bubble = new RoundedPanel(20, new Color(225, 180, 205)); 
            bubble.setLayout(new GridBagLayout()); 
            bubble.setBorder(new EmptyBorder(8, 12, 8, 12)); 
            bubble.add(lblText);
            rowPanel.add(bubble, BorderLayout.EAST);
        } else {
            // --- KASIR (KIRI) ---
            JPanel leftContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            leftContainer.setBackground(Color.WHITE);
            
            RoundedPanel avatar = new RoundedPanel(30, GUIUtils.COLOR_ACCENT);
            avatar.setPreferredSize(new Dimension(30, 30));
            avatar.setLayout(new GridBagLayout());
            
            // Coba load gambar admin/default
            ImageIcon icon = GUIUtils.loadImageIcon("profile.png", 25, 25);
            if(icon!=null) avatar.add(new JLabel(icon));
            else avatar.add(new JLabel("A")); 
            
            RoundedPanel bubble = new RoundedPanel(20, new Color(240, 240, 240)); 
            bubble.setLayout(new GridBagLayout());
            bubble.setBorder(new EmptyBorder(8, 12, 8, 12)); 
            bubble.add(lblText);

            leftContainer.add(avatar);
            leftContainer.add(bubble);
            rowPanel.add(leftContainer, BorderLayout.WEST);
        }

        messageListPanel.add(rowPanel);
    }

    private void sendMessage(JTextField textField, String placeholder) {
        String text = textField.getText().trim();

        if (!text.isEmpty() && !text.equals(placeholder)) {
            if (mainApp.getCurrentUser() != null) {
                int myId = mainApp.getCurrentUser().getId();
                
                // 1. Kirim ke Server
                boolean success = ChatServices.sendMessage(myId, text);
                
                if (success) {
                    // 2. Jika sukses, tampilkan di UI
                    addMessage(text, true);
                    textField.setText("");
                    
                    messageListPanel.revalidate();
                    messageListPanel.repaint();
                    scrollToBottom();
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal mengirim pesan (Jaringan Error)");
                }
            }
        }
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            if (scrollPane != null) {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
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

        // Info Kasir (Tengah/Kiri dikit)
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        infoPanel.setBackground(Color.WHITE);
        
        // Foto Kasir
        RoundedPanel avatar = new RoundedPanel(40, GUIUtils.COLOR_ACCENT);
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
        btnSend.setForeground(GUIUtils.COLOR_PRIMARY); // Warna Pink

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
}