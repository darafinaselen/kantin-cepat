package com.tubes.kantincepat.client.view.admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.tubes.kantincepat.client.net.ClientSocket;

import java.awt.*;
import java.awt.event.*;

public class UserPanel extends JPanel {
    private JTextField txtUserApp, txtPassApp, txtEmailApp, txtFullNameApp, txtPhoneApp;
    private JComboBox<String> cbRoleApp;
    private ClientSocket socketService;
    private DefaultTableModel tableModel;

    public UserPanel(ClientSocket socket) {
        this.socketService = socket;
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);

        JLabel lblHeader = new JLabel("Kelola Pengguna");
        lblHeader.setFont(AppColor.FONT_HEADER);
        add(lblHeader, BorderLayout.NORTH);

        JPanel contentGrid = new JPanel(new BorderLayout(20, 20));
        contentGrid.setOpaque(false);

        StyleUtils.RoundedPanel formCard = new StyleUtils.RoundedPanel(20, Color.WHITE);
        formCard.setLayout(new GridBagLayout());
        formCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtUserApp = StyleUtils.createModernTextField();
        txtPassApp = StyleUtils.createModernTextField();
        txtEmailApp = StyleUtils.createModernTextField();
        txtFullNameApp = StyleUtils.createModernTextField();
        txtPhoneApp = StyleUtils.createModernTextField();
        cbRoleApp = new JComboBox<>(new String[]{"CUSTOMER", "ADMIN", "KITCHEN"});

        addFormRow(formCard, gbc, 0, "Username:", txtUserApp);
        addFormRow(formCard, gbc, 1, "Password:", txtPassApp);
        addFormRow(formCard, gbc, 2, "Email:", txtEmailApp);
        addFormRow(formCard, gbc, 3, "Nama Lengkap:", txtFullNameApp);
        addFormRow(formCard, gbc, 4, "No HP:", txtPhoneApp);
        addFormRow(formCard, gbc, 5, "Role:", cbRoleApp);

        // --- BUTTONS (FIX: ADA EDIT) ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        JButton btnAdd = StyleUtils.createRoundedButton("Tambah", AppColor.BTN_SUCCESS, Color.WHITE);
        JButton btnEdit = StyleUtils.createRoundedButton("Edit", AppColor.BTN_YELLOW, Color.BLACK); // <-- ADA TOMBOL UBAH
        JButton btnDel = StyleUtils.createRoundedButton("Hapus", AppColor.BTN_RED, Color.WHITE);
        JButton btnReset = StyleUtils.createRoundedButton("Reset", AppColor.BTN_MAROON, Color.WHITE);
        
        btnPanel.add(btnAdd); 
        btnPanel.add(btnEdit); 
        btnPanel.add(btnDel); 
        btnPanel.add(btnReset);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; formCard.add(btnPanel, gbc);

        String[] cols = {"ID", "Username", "Email", "Nama Lengkap", "No HP", "Role"};

        tableModel = new DefaultTableModel(cols, 0);
        JTable table = new JTable(tableModel);
        StyleUtils.styleTable(table, 35);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        // LOGIC
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int r = table.getSelectedRow();
                if (r >= 0) {
                    txtUserApp.setText(tableModel.getValueAt(r, 1).toString());
                    txtEmailApp.setText(tableModel.getValueAt(r, 2).toString());
                    txtFullNameApp.setText(tableModel.getValueAt(r, 3).toString());
                    txtPhoneApp.setText(tableModel.getValueAt(r, 4).toString());
                    cbRoleApp.setSelectedItem(tableModel.getValueAt(r, 5).toString());
                    txtPassApp.setText(""); 
                }
            }
        });
        
        btnAdd.addActionListener(e -> { 
            btnAdd.setEnabled(false);
            new Thread(() -> {
                String msg = "ADD_USER;" + txtUserApp.getText() + ";" + txtPassApp.getText() + ";" + txtEmailApp.getText() + ";" + txtFullNameApp.getText() + ";" + txtPhoneApp.getText() + ";" + cbRoleApp.getSelectedItem();
                String resp = socketService.sendRequest(msg);
                SwingUtilities.invokeLater(() -> {
                    if("SUCCESS".equalsIgnoreCase(resp)){
                        JOptionPane.showMessageDialog(this, "Berhasil Menambah User!");
                        clearForm();
                        loadData();
                    } else { JOptionPane.showMessageDialog(this, "Gagal: " + resp); }
                    btnAdd.setEnabled(true);
                });
            }).start();
        });

        // LOGIC EDIT (GUI ONLY)
        btnEdit.addActionListener(e -> {
            int r = table.getSelectedRow();
            if(r >= 0) {
                tableModel.setValueAt(txtEmailApp.getText(), r, 2);
                tableModel.setValueAt(txtFullNameApp.getText(), r, 3);
                tableModel.setValueAt(txtPhoneApp.getText(), r, 4);
                tableModel.setValueAt(cbRoleApp.getSelectedItem(), r, 5);
                clearForm();
                JOptionPane.showMessageDialog(this, "Data User Diubah!");
            } else {
                JOptionPane.showMessageDialog(this, "Pilih user dulu!");
            }
        });

        btnDel.addActionListener(e -> { 
            if(table.getSelectedRow() >= 0) {
                tableModel.removeRow(table.getSelectedRow()); 
            } else {
                JOptionPane.showMessageDialog(this, "Pilih user yang mau dihapus!");
            }
        });

        btnReset.addActionListener(e -> clearForm());

        contentGrid.add(formCard, BorderLayout.NORTH);
        contentGrid.add(scroll, BorderLayout.CENTER);
        add(contentGrid, BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            // Request ke server
            String response = socketService.sendRequest("GET_USERS");
            
            // Format: LIST_USERS#1;admin;...#2;dapur;...
            if (response != null && response.startsWith("LIST_USERS#")) {
                String rawData = response.substring("LIST_USERS#".length());
                
                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0); // Kosongkan tabel lama
                    
                    if (rawData.equals("EMPTY") || rawData.isEmpty()) return;

                    String[] rows = rawData.split("#"); // Pisah antar user
                    for (String row : rows) {
                        String[] cols = row.split(";"); // Pisah antar kolom
                        if (cols.length >= 6) {
                            tableModel.addRow(new Object[]{
                                cols[0], // ID
                                cols[1], // Username
                                cols[2], // Email
                                cols[3], // Fullname
                                cols[4], // Phone
                                cols[5]  // Role
                            });
                        }
                    }
                });
            }
        }).start();
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.1;
        JLabel lbl = new JLabel(label); lbl.setFont(AppColor.FONT_BOLD);
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.9; comp.setPreferredSize(new Dimension(200, 35)); panel.add(comp, gbc);
    }

    private void clearForm() {
        txtUserApp.setText(""); txtPassApp.setText(""); txtEmailApp.setText("");
        txtFullNameApp.setText(""); txtPhoneApp.setText(""); cbRoleApp.setSelectedIndex(0);
    }
}