package com.tubes.kantincepat.client.view.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import com.tubes.kantincepat.client.net.ClientSocket;
import java.awt.*;
import java.awt.event.*;

public class OrderPanel extends JPanel {
    private JTable orderTable;
    private DefaultTableModel orderModel;
    private TableRowSorter<DefaultTableModel> orderSorter;
    private JTextField txtSearchOrder;
    private ClientSocket socketService;

    public OrderPanel(ClientSocket socket) {
        this.socketService = socket;
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);

        JLabel lblHeader = new JLabel("Daftar Pesanan");
        lblHeader.setFont(AppColor.FONT_HEADER);
        add(lblHeader, BorderLayout.NORTH);

        StyleUtils.RoundedPanel toolBar = new StyleUtils.RoundedPanel(15, Color.WHITE);
        toolBar.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        toolBar.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel lblSearch = new JLabel("Cari:"); lblSearch.setFont(AppColor.FONT_BOLD);
        txtSearchOrder = StyleUtils.createModernTextField(); txtSearchOrder.setPreferredSize(new Dimension(200, 35));
        
        JLabel lblFilter = new JLabel("Status:"); lblFilter.setFont(AppColor.FONT_BOLD);
        JComboBox<String> cbFilter = new JComboBox<>(new String[]{"Semua", "PENDING", "COOKING", "READY", "COMPLETED", "CANCELLED"});
        
        JButton btnRefresh = StyleUtils.createRoundedButton("Refresh", AppColor.COLOR_PRIMARY, Color.WHITE);
        JButton btnCancel = StyleUtils.createRoundedButton("Batalkan", AppColor.BTN_RED, Color.WHITE);

        toolBar.add(lblSearch); toolBar.add(txtSearchOrder); 
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(lblFilter); 
        toolBar.add(cbFilter); 
        toolBar.add(Box.createHorizontalStrut(20)); 
        toolBar.add(btnCancel);
        toolBar.add(btnRefresh); 

        String[] cols = {"ID", "Pelanggan", "Menu Pesanan", "Catatan", "Total", "Status", "Tanggal"};
        orderModel = new DefaultTableModel(cols, 0) { 
            public boolean isCellEditable(int r, int c) { return false; }
        };

        orderTable = new JTable(orderModel);
        StyleUtils.styleTable(orderTable, 35);
        orderTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        orderTable.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());
        
        orderSorter = new TableRowSorter<>(orderModel);
        orderTable.setRowSorter(orderSorter);

        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        // LOGIC
        txtSearchOrder.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = txtSearchOrder.getText();
                if (text.trim().length() == 0) orderSorter.setRowFilter(null);
                else orderSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });
        cbFilter.addActionListener(e -> {
            String selected = (String) cbFilter.getSelectedItem();
            if(selected.equals("Semua")) orderSorter.setRowFilter(null);
            else orderSorter.setRowFilter(RowFilter.regexFilter(selected, 5));
        });

        btnRefresh.addActionListener(e -> loadData());

        btnCancel.addActionListener(e -> {
            int r = orderTable.getSelectedRow();
            if(r >= 0) {
                int modelRow = orderTable.convertRowIndexToModel(r);
                String currentStatus = (String) orderModel.getValueAt(modelRow, 5);
                String orderId = orderModel.getValueAt(modelRow, 0).toString();

                if ("CANCELLED".equals(currentStatus) || "COMPLETED".equals(currentStatus)) {
                    JOptionPane.showMessageDialog(this, "Pesanan sudah selesai/dibatalkan.");
                    return;
                }

                int confirm = JOptionPane.showConfirmDialog(this, "Yakin batalkan pesanan ID: " + orderId + "?");
                if (confirm == JOptionPane.YES_OPTION) {
                    new Thread(() -> {
                        String resp = socketService.sendRequest("UPDATE_STATUS;" + orderId + ";CANCELLED");
                        SwingUtilities.invokeLater(() -> {
                            if("UPDATE_SUCCESS".equalsIgnoreCase(resp)) {
                                JOptionPane.showMessageDialog(this, "Pesanan Dibatalkan!");
                                loadData(); // Reload data biar status berubah
                            } else {
                                JOptionPane.showMessageDialog(this, "Gagal update status.");
                            }
                        });
                    }).start();
                }
            } else JOptionPane.showMessageDialog(this, "Pilih pesanan dulu!");
        });

        JPanel container = new JPanel(new BorderLayout(0, 15));
        container.setOpaque(false);
        container.add(toolBar, BorderLayout.NORTH);
        container.add(scrollPane, BorderLayout.CENTER);
        add(container, BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            String response = socketService.sendRequest("GET_ALL_ORDERS");
            
            // Format: ALL_ORDERS#id;customer;menu;note;total;status;date#...
            if (response != null && response.startsWith("ALL_ORDERS#")) {
                String rawData = response.substring("ALL_ORDERS#".length());
                
                SwingUtilities.invokeLater(() -> {
                    orderModel.setRowCount(0); // Bersihkan tabel
                    
                    if (rawData.equals("EMPTY") || rawData.isEmpty()) return;

                    String[] rows = rawData.split("#");
                    for (String row : rows) {
                        String[] cols = row.split(";");
                        if (cols.length >= 7) {
                            orderModel.addRow(new Object[]{
                                cols[0], // ID
                                cols[1], // Pelanggan (Nama User)
                                cols[2], // Menu Summary
                                cols[3], // Catatan
                                "Rp. " + cols[4], // Total
                                cols[5], // Status
                                cols[6]  // Tanggal
                            });
                        }
                    }
                });
            }
        }).start();
    }

    private class StatusRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String s = (String) value;
            if (s.equalsIgnoreCase("COMPLETED") || s.equalsIgnoreCase("READY")) c.setForeground(new Color(46, 125, 50)); 
            else if (s.equalsIgnoreCase("CANCELLED")) c.setForeground(Color.RED);
            else if (s.equalsIgnoreCase("COOKING")) c.setForeground(Color.BLUE);
            else c.setForeground(new Color(255, 143, 0));
            c.setFont(c.getFont().deriveFont(Font.BOLD)); return c;
        }
    }
}