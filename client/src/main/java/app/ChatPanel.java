package app;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ChatPanel extends JPanel {
    private JPanel pnlChatMessages;
    private JTextField txtChatInput;
    private JList<String> listContacts;
    private DefaultListModel<String> contactDisplayModel;
    private ArrayList<String> allContactsList;
    private JLabel lblChatTarget;
    private JTextField txtSearchContact;
    private int customerCounter = 0;

    public ChatPanel() {
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);

        JLabel lblHeader = new JLabel("Live Chat");
        lblHeader.setFont(AppColor.FONT_HEADER);
        add(lblHeader, BorderLayout.NORTH);

        JPanel chatContainer = new JPanel(new BorderLayout(15, 0));
        chatContainer.setOpaque(false);

        // --- LEFT SIDEBAR ---
        StyleUtils.RoundedPanel contactPanel = new StyleUtils.RoundedPanel(15, Color.WHITE);
        contactPanel.setLayout(new BorderLayout());
        contactPanel.setPreferredSize(new Dimension(280, 0));
        
        JPanel pnlSearch = new JPanel(new BorderLayout());
        pnlSearch.setBackground(Color.WHITE);
        pnlSearch.setBorder(new EmptyBorder(10, 10, 10, 10));
        txtSearchContact = StyleUtils.createModernTextField();
        pnlSearch.add(new JLabel("Cari: "), BorderLayout.WEST);
        pnlSearch.add(txtSearchContact, BorderLayout.CENTER);

        contactDisplayModel = new DefaultListModel<>();
        allContactsList = new ArrayList<>();
        addNewChatSession("Wahyunii"); 
        addNewChatSession("Budi");
        
        listContacts = new JList<>(contactDisplayModel);
        listContacts.setFont(new Font("SansSerif", Font.PLAIN, 14));
        listContacts.setFixedCellHeight(50);
        listContacts.setSelectionBackground(AppColor.SIDEBAR_HOVER);
        listContacts.setSelectionForeground(Color.BLACK);
        listContacts.setBorder(new EmptyBorder(5, 5, 5, 5));

        contactPanel.add(pnlSearch, BorderLayout.NORTH);
        contactPanel.add(new JScrollPane(listContacts), BorderLayout.CENTER);

        txtSearchContact.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { filterContacts(txtSearchContact.getText()); }
        });

        // --- RIGHT CHAT AREA ---
        StyleUtils.RoundedPanel chatAreaPanel = new StyleUtils.RoundedPanel(15, new Color(245, 245, 245));
        chatAreaPanel.setLayout(new BorderLayout());
        chatAreaPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        lblChatTarget = new JLabel(" Pilih kontak...", SwingConstants.LEFT);
        lblChatTarget.setFont(AppColor.FONT_BOLD);
        lblChatTarget.setPreferredSize(new Dimension(0, 50));
        lblChatTarget.setBorder(new EmptyBorder(0, 15, 0, 0));
        
        pnlChatMessages = new JPanel();
        pnlChatMessages.setLayout(new BoxLayout(pnlChatMessages, BoxLayout.Y_AXIS));
        pnlChatMessages.setBackground(new Color(245, 245, 245));
        
        JScrollPane scrollChat = new JScrollPane(pnlChatMessages);
        scrollChat.setBorder(null);
        scrollChat.getVerticalScrollBar().setUnitIncrement(16);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setOpaque(false);
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        txtChatInput = StyleUtils.createModernTextField(); 
        txtChatInput.setPreferredSize(new Dimension(0, 45));
        
        JButton btnSend = StyleUtils.createRoundedButton("Kirim", AppColor.PRIMARY_PURPLE, Color.WHITE);
        btnSend.setPreferredSize(new Dimension(80, 45));
        inputPanel.add(txtChatInput, BorderLayout.CENTER); 
        inputPanel.add(btnSend, BorderLayout.EAST);

        chatAreaPanel.add(lblChatTarget, BorderLayout.NORTH);
        chatAreaPanel.add(scrollChat, BorderLayout.CENTER);
        chatAreaPanel.add(inputPanel, BorderLayout.SOUTH);

        // --- LOGIC ---
        listContacts.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String sel = listContacts.getSelectedValue();
                if(sel != null) {
                    lblChatTarget.setText(sel);
                    pnlChatMessages.removeAll();
                    // Simulasi Chat History
                    addMessageToChat("Halo min", "10:30", false, true); // Pesan Masuk (Read)
                    addMessageToChat("Halo kak, ada yang bisa dibantu?", "10:31", true, true); // Balasan Admin (Read)
                    pnlChatMessages.revalidate(); pnlChatMessages.repaint();
                }
            }
        });

        ActionListener sendAction = e -> {
            String msg = txtChatInput.getText().trim();
            if (listContacts.getSelectedValue() == null) { JOptionPane.showMessageDialog(this, "Pilih kontak!"); return; }
            if(!msg.isEmpty()) {
                String time = new SimpleDateFormat("HH:mm").format(new Date());
                // Kirim Pesan Admin (isMe=true, isRead=false -> Baru terkirim)
                addMessageToChat(msg, time, true, false);
                txtChatInput.setText("");
                SwingUtilities.invokeLater(() -> scrollChat.getVerticalScrollBar().setValue(scrollChat.getVerticalScrollBar().getMaximum()));
            }
        };
        btnSend.addActionListener(sendAction); 
        txtChatInput.addActionListener(sendAction);

        chatContainer.add(contactPanel, BorderLayout.WEST);
        chatContainer.add(chatAreaPanel, BorderLayout.CENTER);
        add(chatContainer, BorderLayout.CENTER);
    }

    // Update Method: Tambah parameter isRead
    private void addMessageToChat(String message, String time, boolean isMe, boolean isRead) {
        JPanel wrapper = new JPanel(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrapper.setOpaque(false);
        ChatBubble bubble = new ChatBubble(message, time, isMe, isRead);
        wrapper.add(bubble);
        pnlChatMessages.add(wrapper);
        pnlChatMessages.add(Box.createRigidArea(new Dimension(0, 5)));
        pnlChatMessages.revalidate(); 
        pnlChatMessages.repaint();
    }

    private void addNewChatSession(String name) {
        customerCounter++;
        allContactsList.add("Pelanggan " + customerCounter + " - " + name);
        filterContacts("");
    }

    private void filterContacts(String query) {
        contactDisplayModel.clear();
        for (String contact : allContactsList) {
            if (contact.toLowerCase().contains(query.toLowerCase())) contactDisplayModel.addElement(contact);
        }
    }

    // --- CUSTOM BUBBLE CHAT WITH STATUS ---
    private class ChatBubble extends JPanel {
        public ChatBubble(String msg, String time, boolean isMe, boolean isRead) {
            setLayout(new BorderLayout()); 
            setOpaque(false);
            setBorder(new EmptyBorder(10, 15, 10, 15));
            setBackground(isMe ? new Color(225, 245, 254) : Color.WHITE);

            JLabel lblMsg = new JLabel("<html><p style='width: 200px'>"+msg+"</p></html>");
            lblMsg.setFont(new Font("SansSerif", Font.PLAIN, 14));
            
            // Status Panel (Jam + Centang)
            JPanel pnlStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            pnlStatus.setOpaque(false);
            
            JLabel lblTime = new JLabel(time);
            lblTime.setFont(new Font("SansSerif", Font.PLAIN, 10)); 
            lblTime.setForeground(Color.GRAY);
            
            pnlStatus.add(lblTime);

            // Icon Status (Hanya untuk pesan Admin)
            if(isMe) {
                JLabel lblTick = new JLabel(isRead ? "✓✓" : "✓");
                lblTick.setFont(new Font("SansSerif", Font.BOLD, 10));
                lblTick.setForeground(isRead ? new Color(33, 150, 243) : Color.GRAY); // Biru jika Read, Abu jika Sent
                pnlStatus.add(lblTick);
            }

            add(lblMsg, BorderLayout.CENTER); 
            add(pnlStatus, BorderLayout.SOUTH);
        }
        
        @Override 
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g; 
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground()); 
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            super.paintComponent(g);
        }
    }
}