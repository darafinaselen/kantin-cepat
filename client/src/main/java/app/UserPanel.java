package app;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class UserPanel extends JPanel {
    private JTextField txtUserApp, txtPassApp, txtEmailApp, txtFullNameApp, txtPhoneApp;
    private JComboBox<String> cbRoleApp;
    private SocketService socketService;

    public UserPanel(SocketService socket) {
        this.socketService = socket;
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);

        JLabel lblHeader = new JLabel("Kelola Pengguna (Table: users)");
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

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        JButton btnAdd = StyleUtils.createRoundedButton("Tambah", AppColor.BTN_SUCCESS, Color.WHITE);
        JButton btnDel = StyleUtils.createRoundedButton("Hapus", AppColor.BTN_RED, Color.WHITE);
        JButton btnReset = StyleUtils.createRoundedButton("Reset", AppColor.BTN_MAROON, Color.WHITE);
        btnPanel.add(btnAdd); btnPanel.add(btnDel); btnPanel.add(btnReset);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; formCard.add(btnPanel, gbc);

        String[] cols = {"ID", "Username", "Email", "Nama Lengkap", "No HP", "Role"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        model.addRow(new Object[]{1, "admin", "admin@kantin.com", "Admin Kantin", "081234567890", "ADMIN"});
        model.addRow(new Object[]{2, "dapur", "dapur@kantin.com", "Staff Dapur", "081234567891", "KITCHEN"});

        JTable table = new JTable(model);
        StyleUtils.styleTable(table, 35);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        // LOGIC
        btnAdd.addActionListener(e -> { 
            btnAdd.setEnabled(false);
            new Thread(() -> {
                String msg = "ADD_USER;" + txtUserApp.getText() + ";" + txtPassApp.getText() + ";" + txtEmailApp.getText() + ";" + txtFullNameApp.getText() + ";" + txtPhoneApp.getText() + ";" + cbRoleApp.getSelectedItem();
                String resp = socketService.sendRequest(msg);
                SwingUtilities.invokeLater(() -> {
                    if("SUCCESS".equalsIgnoreCase(resp)){
                        model.addRow(new Object[]{model.getRowCount()+1, txtUserApp.getText(), txtEmailApp.getText(), txtFullNameApp.getText(), txtPhoneApp.getText(), cbRoleApp.getSelectedItem()}); 
                        clearForm(); JOptionPane.showMessageDialog(this, "Berhasil!");
                    } else { JOptionPane.showMessageDialog(this, "Gagal: " + resp); }
                    btnAdd.setEnabled(true);
                });
            }).start();
        });
        btnDel.addActionListener(e -> { if(table.getSelectedRow()>=0) model.removeRow(table.getSelectedRow()); });
        btnReset.addActionListener(e -> clearForm());

        contentGrid.add(formCard, BorderLayout.NORTH);
        contentGrid.add(scroll, BorderLayout.CENTER);
        add(contentGrid, BorderLayout.CENTER);
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