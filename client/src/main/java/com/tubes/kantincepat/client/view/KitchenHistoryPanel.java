package com.tubes.kantincepat.client.view;

import com.tubes.kantincepat.client.KitchenApp;
import com.tubes.kantincepat.client.net.ClientSocket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class KitchenHistoryPanel extends JPanel {

    private final KitchenApp mainApp;
    private JPanel ordersContainer;

    public KitchenHistoryPanel(KitchenApp mainApp) {
        this.mainApp = mainApp;

        System.out.println("[HistoryPanel] constructor instance = " +
                System.identityHashCode(this));

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // HEADER: panah kiri + judul
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel backBtn = new JLabel("←");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 22));
        backBtn.setForeground(Color.decode("#111827"));
        backBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        backBtn.setBorder(new EmptyBorder(0, 0, 0, 10));

        backBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                mainApp.showKitchen();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                backBtn.setForeground(Color.decode("#C084B7"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                backBtn.setForeground(Color.decode("#111827"));
            }
        });

        JLabel title = new JLabel("Riwayat Pesanan");
        title.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 22f));
        title.setForeground(Color.decode("#111827"));

        JPanel leftHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        leftHeader.setOpaque(false);
        leftHeader.add(backBtn);
        leftHeader.add(Box.createHorizontalStrut(8));
        leftHeader.add(title);

        headerPanel.add(leftHeader, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // CONTAINER GRID
        ordersContainer = new JPanel();
        ordersContainer.setLayout(new GridLayout(0, 3, 16, 16));
        ordersContainer.setOpaque(false);
        ordersContainer.setBorder(new EmptyBorder(16, 16, 16, 16));

        JScrollPane scrollPane = new JScrollPane(ordersContainer);
        scrollPane.setBorder(null);

        Color bg = Color.decode("#f6edf3");
        scrollPane.setBackground(bg);
        scrollPane.getViewport().setBackground(bg);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // initial load
        loadHistoryFromServer();
    }

    public void refreshHistory() {
        System.out.println("[HistoryPanel] refreshHistory() instance = " +
                System.identityHashCode(this));
        loadHistoryFromServer();
    }

    private void loadHistoryFromServer() {
        System.out.println("[HistoryPanel] loadHistoryFromServer() instance = " +
                System.identityHashCode(this));

        ordersContainer.removeAll();

        String resp = ClientSocket.getInstance().sendRequest("GET_ORDER_HISTORY");
        System.out.println("[History] resp = " + resp);

        if (resp == null) {
            showCenterMessage("Tidak ada respon dari server.");
            return;
        }

        if (resp.startsWith("NO_ORDERS")) {
            showCenterMessage("Belum ada riwayat pesanan.");
            return;
        }

        if (!resp.startsWith("ORDERS:")) {
            showCenterMessage("Format data tidak dikenali.<br/>Respon: " + resp);
            return;
        }

        String data = resp.substring("ORDERS:".length());
        String[] chunks = data.split(";");
        System.out.println("[History] jumlah chunk = " + chunks.length);

        ordersContainer.setLayout(new GridLayout(0, 3, 16, 16));

        int createdCount = 0;

        for (String chunk : chunks) {
            if (chunk == null) continue;
            chunk = chunk.trim();
            if (chunk.isEmpty()) continue;

            String[] f = chunk.split("\\|", 4);
            if (f.length < 4) {
                System.out.println("[History] skip chunk (kurang field): " + chunk);
                continue;
            }

            try {
                int orderId = Integer.parseInt(f[0]);
                String fullname = f[1];
                String statusEnum = f[2];
                String itemsStr = f[3];

                System.out.println("[History] add card orderId=" + orderId +
                        " name=" + fullname +
                        " status=" + statusEnum +
                        " items=" + itemsStr);

                JPanel card = createHistoryCard(orderId, fullname, statusEnum, itemsStr);
                ordersContainer.add(card);
                createdCount++;

            } catch (NumberFormatException ex) {
                System.out.println("[History] gagal parse orderId untuk chunk: " + chunk);
            }
        }

        System.out.println("[History] total card dibuat = " + createdCount);
        if (createdCount == 0) {
            showCenterMessage("Belum ada riwayat pesanan.");
        } else {
            ordersContainer.revalidate();
            ordersContainer.repaint();
        }
    }

    private void showCenterMessage(String text) {
        ordersContainer.removeAll();
        ordersContainer.setLayout(new BorderLayout());

        JLabel empty = new JLabel(
                "<html><div style='text-align:center;'>" + text + "</div></html>",
                SwingConstants.CENTER
        );
        empty.setFont(GUIUtils.getCustomFont("Lato-Regular.ttf", 14f));
        ordersContainer.add(empty, BorderLayout.CENTER);

        ordersContainer.revalidate();
        ordersContainer.repaint();
    }

    private JPanel createHistoryCard(int orderId, String customerName, String statusEnum, String itemsStr) {
        RoundedPanel card = new RoundedPanel(24, Color.WHITE);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#E5E7EB"), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JLabel lblOrderId = new JLabel("Order#" + orderId);
        lblOrderId.setFont(GUIUtils.getCustomFont("Lato-Regular.ttf", 13f));
        lblOrderId.setForeground(Color.decode("#6B7280"));
        lblOrderId.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblName = new JLabel(customerName);
        lblName.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 16f));
        lblName.setForeground(Color.decode("#111827"));
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblStatus = new JLabel("Status : Selesai");
        lblStatus.setFont(GUIUtils.getCustomFont("Lato-Regular.ttf", 13f));
        lblStatus.setForeground(Color.decode("#6B7280"));
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(lblOrderId);
        header.add(Box.createVerticalStrut(4));
        header.add(lblName);
        header.add(Box.createVerticalStrut(4));
        header.add(lblStatus);

        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(0, 0, 16, 0));

        if (itemsStr != null && !itemsStr.isBlank()) {
            String[] items = itemsStr.split(",");
            for (String item : items) {
                String trimmed = item.trim();
                if (trimmed.isEmpty()) continue;

                JLabel lblItem = new JLabel(trimmed);
                lblItem.setFont(GUIUtils.getCustomFont("Lato-Regular.ttf", 13f));
                lblItem.setForeground(Color.decode("#111827"));
                lblItem.setAlignmentX(Component.LEFT_ALIGNMENT);

                body.add(lblItem);
                body.add(Box.createVerticalStrut(4));
            }
        }

        JButton btn = new JButton("Selesai");
        btn.setPreferredSize(new Dimension(0, 44));
        btn.setFocusPainted(false);
        btn.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 14f));
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setEnabled(false);
        btn.setBackground(Color.decode("#E5E7EB"));
        btn.setForeground(Color.decode("#9CA3AF"));

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(btn, BorderLayout.CENTER);

        card.add(header, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private static class RoundedPanel extends JPanel {
        private final int cornerRadius;
        private final Color bgColor;

        public RoundedPanel(int cornerRadius, Color bgColor) {
            this.cornerRadius = cornerRadius;
            this.bgColor = bgColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
