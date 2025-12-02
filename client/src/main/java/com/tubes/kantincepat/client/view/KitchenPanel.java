package com.tubes.kantincepat.client.view;

import com.tubes.kantincepat.client.KitchenApp;
import com.tubes.kantincepat.client.net.ClientSocket;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class KitchenPanel extends JPanel {

    private final KitchenApp mainApp;
    private JPanel ordersContainer;

    public KitchenPanel(KitchenApp mainApp) {
        this.mainApp = mainApp;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // HEADER: judul + tombol riwayat
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel title = new JLabel("Daftar Pesanan");
        title.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 22f));
        title.setForeground(Color.decode("#111827"));

        JButton historyBtn = new JButton("Riwayat Pesanan");
        historyBtn.setFocusPainted(false);
        historyBtn.setFont(GUIUtils.getCustomFont("Lato-Regular.ttf", 13f));
        historyBtn.setBackground(Color.WHITE);
        historyBtn.setForeground(Color.decode("#C084B7"));
        historyBtn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        historyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        historyBtn.addActionListener(e -> mainApp.showKitchenHistory());

        headerPanel.add(title, BorderLayout.WEST);
        headerPanel.add(historyBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Grid 3 kolom
        ordersContainer = new JPanel();
        ordersContainer.setLayout(new GridLayout(0, 3, 16, 16));
        ordersContainer.setOpaque(false);
        ordersContainer.setBorder(new EmptyBorder(16, 0, 0, 0));

        JScrollPane scrollPane = new JScrollPane(ordersContainer);
        scrollPane.setBorder(null);

        // Ini yang bikin background tetap putih
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBackground(Color.WHITE);

        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);


        loadOrdersFromServer();
    }

    public void refreshOrders() {
        loadOrdersFromServer();
    }


    private void loadOrdersFromServer() {
        ordersContainer.removeAll();

        String resp = ClientSocket.getInstance().sendRequest("GET_KITCHEN_ORDERS");
        if (resp == null || resp.startsWith("NO_ORDERS")) {
            ordersContainer.setLayout(new BorderLayout());
            JLabel empty = new JLabel("Belum ada pesanan.", SwingConstants.CENTER);
            empty.setFont(GUIUtils.getCustomFont("Lato-Regular.ttf", 14f));
            ordersContainer.add(empty, BorderLayout.CENTER);
            revalidate();
            repaint();
            return;
        }

        String[] main = resp.split(":", 2);
        if (main.length < 2 || !"ORDERS".equals(main[0])) {
            ordersContainer.setLayout(new BorderLayout());
            JLabel empty = new JLabel("Format data tidak dikenali.", SwingConstants.CENTER);
            empty.setFont(GUIUtils.getCustomFont("Lato-Regular.ttf", 14f));
            ordersContainer.add(empty, BorderLayout.CENTER);
            revalidate();
            repaint();
            return;
        }

        ordersContainer.setLayout(new GridLayout(0, 3, 16, 16));

        String[] chunks = main[1].split(";");
        for (String chunk : chunks) {
            if (chunk.isBlank()) continue;
            String[] f = chunk.split("\\|", 4);
            if (f.length < 4) continue;

            int orderId = Integer.parseInt(f[0]);
            String fullname = f[1];
            String statusEnum = f[2];   // PENDING / COOKING / READY
            String itemsStr = f[3];

            JPanel card = createOrderCard(orderId, fullname, statusEnum, itemsStr);
            ordersContainer.add(card);
        }

        revalidate();
        repaint();
    }

    private JPanel createOrderCard(int orderId, String customerName, String statusEnum, String itemsStr) {
        RoundedPanel card = new RoundedPanel(30, Color.decode("#F6EDF3"));
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.decode("#E5E7EB"), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        // HEADER
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

        JLabel lblStatus = new JLabel("Status : " + getStatusText(statusEnum));
        lblStatus.setFont(GUIUtils.getCustomFont("Lato-Regular.ttf", 13f));
        lblStatus.setForeground(Color.decode("#6B7280"));
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);

        header.add(lblOrderId);
        header.add(Box.createVerticalStrut(4));
        header.add(lblName);
        header.add(Box.createVerticalStrut(4));
        header.add(lblStatus);

        // BODY
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

        JButton btnAction = createActionButton(orderId, statusEnum);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(btnAction, BorderLayout.CENTER);

        card.add(header, BorderLayout.NORTH);
        card.add(body, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    private JButton createActionButton(int orderId, String statusEnum) {
        String label = getButtonLabel(statusEnum);

        JButton btn = new JButton(label);
        btn.setPreferredSize(new Dimension(0, 44));
        btn.setFocusPainted(false);
        btn.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 14f));
        btn.setBorder(BorderFactory.createEmptyBorder());
        btn.setBackground(getStatusColor(statusEnum)); // warna dinamis
        btn.setForeground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            String nextStatus = getNextStatusEnum(statusEnum);
            if (nextStatus == null) return;

            if (!confirmStatusChange(statusEnum, nextStatus)) {
                return; 
            }

            String resp = ClientSocket.getInstance()
                    .sendRequest("UPDATE_ORDER_STATUS:" + orderId + ":" + nextStatus);

            if (resp != null && resp.startsWith("UPDATE_SUCCESS")) {
                loadOrdersFromServer();
            } else {
                JOptionPane.showMessageDialog(this, "Gagal mengubah status pesanan.");
            }
        });


        return btn;
    }

    private String getStatusText(String statusEnum) {
        return switch (statusEnum) {
            case "PENDING"   -> "Menunggu";
            case "COOKING"   -> "Sedang dimasak";
            case "READY"     -> "Siap diambil";
            case "COMPLETED" -> "Selesai";
            default          -> statusEnum;
        };
    }

    private String getButtonLabel(String statusEnum) {
        return switch (statusEnum) {
            case "PENDING"   -> "Mulai Masak";
            case "COOKING"   -> "Tandai Siap";
            case "READY"     -> "Selesaikan";
            default          -> "Aksi";
        };
    }

    private Color getStatusColor(String statusEnum) {
        return switch (statusEnum) {
            case "PENDING"   -> Color.decode("#C084B7");
            case "COOKING"   -> Color.decode("#dc5151");
            case "READY"     -> Color.decode("#84c086");
            case "COMPLETED" -> Color.decode("#84aac0");
            default          -> Color.GRAY;
        };
    }


    private String getNextStatusEnum(String statusEnum) {
        return switch (statusEnum) {
            case "PENDING" -> "COOKING";
            case "COOKING" -> "READY";
            case "READY"   -> "COMPLETED";
            default        -> null;
        };
    }

    private boolean confirmStatusChange(String currentStatus, String nextStatus) {
        String message;

        switch (nextStatus) {
            case "COOKING":
                message = "Apakah Anda ingin mulai memasak pesanan ini?";
                break;
            case "READY":
                message = "Apakah Anda yakin ingin menandai pesanan ini sebagai siap diambil?";
                break;
            case "COMPLETED":
                message = "Selesaikan pesanan ini?";
                break;
            default:
                message = "Lanjutkan perubahan status?";
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                message,
                "Konfirmasi Perubahan Status",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        return result == JOptionPane.YES_OPTION;
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
