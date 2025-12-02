package com.tubes.kantincepat.client.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import com.tubes.kantincepat.client.KitchenApp;
import com.tubes.kantincepat.client.net.ChatClient;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class LiveChat extends JPanel {

    private KitchenApp mainApp;
    private JPanel messageListPanel;
    
    private JLabel headerNameLabel;
    private JLabel headerAvatarInitialLabel;

    private ChatClient chatClient;
    private int currentUserId;
    
    // Map untuk tracking tick labels per message
    private java.util.Map<String, JLabel> messageTickMap = new java.util.HashMap<>();
    private int messageCounter = 0;
    
    public LiveChat(KitchenApp app) {
        this.mainApp = app;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

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
        if (this.chatClient != null) return;

        this.currentUserId = mainApp.getCurrentUserId();
        System.out.println("[LiveChat] init dengan userId = " + currentUserId);

        refreshHeaderInfo();

        try {
            chatClient = new ChatClient(currentUserId);
            chatClient.setListener((senderId, message, isRead) -> {
                boolean isSender = (senderId == currentUserId);

                if (message.isEmpty() && isRead && isSender) {
                    // Ini adalah READ_STATUS untuk pesan saya sendiri
                    System.out.println("[LiveChat] Received READ_STATUS, updating ticks to blue");
                    SwingUtilities.invokeLater(this::updateAllTicksToRead);

                } else {
                    // Ini adalah CHAT_MESSAGE biasa
                    SwingUtilities.invokeLater(() -> {
                        addMessage(message, isSender, isRead);

                        // Jika saya lagi di layar chat dan menerima pesan dari lawan,
                        // langsung tandai semua pesan sebagai sudah dibaca
                        if (!isSender && chatClient != null) {
                            System.out.println("[LiveChat] Auto MARK_ALL_READ karena pesan baru dari lawan");
                            chatClient.markAllAsRead();
                        }
                    });
                }
            });

            // optional: kalau mau mark read untuk pesan lama ketika pertama kali buka chat
            chatClient.markAllAsRead();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Tidak bisa konek ke server chat");
        }
    }


    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

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

        RoundedPanel avatar = new RoundedPanel(40, mainApp.COLOR_ACCENT);
        avatar.setPreferredSize(new Dimension(40, 40));
        avatar.setLayout(new GridBagLayout());

        headerAvatarInitialLabel = new JLabel("?");
        headerAvatarInitialLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        headerAvatarInitialLabel.setForeground(Color.WHITE);
        avatar.add(headerAvatarInitialLabel);

        JPanel textPanel = new JPanel(new GridLayout(2, 1));
        textPanel.setBackground(Color.WHITE);

        headerNameLabel = new JLabel("");
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

        refreshHeaderInfo();

        return panel;
    }

    private void refreshHeaderInfo() {
        if (headerNameLabel == null || headerAvatarInitialLabel == null) return;

        String role = mainApp.getCurrentUserRole();

        String friendName;
        if ("ADMIN".equals(role)) {
            friendName = "Customer";
        } else {
            friendName = "Kasir";
        }
        headerNameLabel.setText(friendName);

        String initial;
        if ("CUSTOMER".equals(role)) {
            initial = "K";
        } else {
            initial = "C";
        }
        headerAvatarInitialLabel.setText(initial);
    }

    private void addMessage(String text, boolean isSender, boolean isRead) {
        JPanel rowPanel = new JPanel(new BorderLayout());
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setBorder(new EmptyBorder(5, 0, 5, 0));

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
            // USER (KANAN) dengan status read
            JPanel rightContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            rightContainer.setBackground(Color.WHITE);

            // Panel untuk bubble + tick
            JPanel bubbleWithStatus = new JPanel();
            bubbleWithStatus.setLayout(new BoxLayout(bubbleWithStatus, BoxLayout.Y_AXIS));
            bubbleWithStatus.setBackground(Color.WHITE);

            RoundedPanel bubble = new RoundedPanel(20, new Color(225, 180, 205));
            bubble.setLayout(new GridBagLayout());
            bubble.setBorder(new EmptyBorder(8, 12, 8, 12));
            bubble.add(lblText);

            // Panel untuk status tick di bawah bubble
            JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 2));
            statusPanel.setBackground(Color.WHITE);

            // LOGIKA ICON CENTANG
            String symbol = isRead ? "✓✓" : "✓";
            Color tickColor = isRead ? new Color(33, 150, 243) : Color.GRAY;
            
            JLabel lblTick = new JLabel(symbol);
            lblTick.setFont(new Font("SansSerif", Font.BOLD, 11));
            lblTick.setForeground(tickColor);
            
            // Simpan referensi untuk update nanti
            String msgKey = "msg_" + messageCounter++;
            messageTickMap.put(msgKey, lblTick);
            
            statusPanel.add(lblTick);

            bubbleWithStatus.add(bubble);
            bubbleWithStatus.add(statusPanel);

            rightContainer.add(bubbleWithStatus);
            rowPanel.add(rightContainer, BorderLayout.EAST);

        } else {
            // LAWAN CHAT (KIRI)
            JPanel leftContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
            leftContainer.setBackground(Color.WHITE);

            String role = mainApp.getCurrentUserRole();
            String senderName;

            if (isSender) {
                senderName = mainApp.getCurrentUserName() != null
                        ? mainApp.getCurrentUserName()
                        : "Me";
            } else {
                if ("ADMIN".equals(role)) {
                    senderName = "Customer";
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

        rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        rowPanel.doLayout();
        Dimension pref = rowPanel.getPreferredSize();
        rowPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));

        messageListPanel.add(rowPanel);
        messageListPanel.revalidate();
        messageListPanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollPane scrollPane = (JScrollPane) SwingUtilities.getAncestorOfClass(
                    JScrollPane.class, messageListPanel);
            if (scrollPane != null) {
                JScrollBar vertical = scrollPane.getVerticalScrollBar();
                vertical.setValue(vertical.getMaximum());
            }
        });
    }
    
    private void updateAllTicksToRead() {
        // Update semua tick menjadi biru (read)
        for (JLabel tickLabel : messageTickMap.values()) {
            tickLabel.setText("✓✓");
            tickLabel.setForeground(new Color(33, 150, 243));
        }
        messageListPanel.revalidate();
        messageListPanel.repaint();
    }

    private JPanel createInputArea() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(new EmptyBorder(10, 20, 20, 20)); 

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        
        JPanel inputBg = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(248, 248, 250));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
            }
        };
        
        inputBg.setLayout(new BorderLayout());
        inputBg.setBorder(new EmptyBorder(5, 20, 5, 10));
        inputBg.setPreferredSize(new Dimension(100, 50));

        String placeholder = "Write your message";
        JTextField textField = new JTextField(placeholder);
        textField.setBorder(null);
        textField.setOpaque(false); 
        textField.setForeground(Color.GRAY);

        textField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textField.getText().equals(placeholder)) {
                    textField.setText("");
                    textField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (textField.getText().isEmpty()) {
                    textField.setText(placeholder);
                    textField.setForeground(Color.GRAY);
                }
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        
        JLabel btnSend = new JLabel("➤");
        btnSend.setForeground(mainApp.COLOR_PRIMARY);

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
            if (chatClient != null) {
                chatClient.sendChat(text);
            }

            textField.setText("");
        }
    }
}