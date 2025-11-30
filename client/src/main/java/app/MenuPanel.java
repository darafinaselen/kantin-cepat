package app;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

public class MenuPanel extends JPanel {
    private JTextField txtNamaMenu, txtHargaMenu, txtDeskripsiMenu;
    private JComboBox<String> cbKategoriMenu, cbStatusMenu;
    private JLabel lblImagePreview;
    private ImageIcon selectedMenuIcon = null;
    private SocketService socketService;

    public MenuPanel(SocketService socket) {
        this.socketService = socket;
        setLayout(new BorderLayout(20, 20));
        setOpaque(false);

        JLabel lblHeader = new JLabel("Daftar Menu");
        lblHeader.setFont(AppColor.FONT_HEADER);
        add(lblHeader, BorderLayout.NORTH);

        JPanel contentGrid = new JPanel(new BorderLayout(20, 20));
        contentGrid.setOpaque(false);

        // FORM
        StyleUtils.RoundedPanel formCard = new StyleUtils.RoundedPanel(20, Color.WHITE);
        formCard.setLayout(new GridBagLayout());
        formCard.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtNamaMenu = StyleUtils.createModernTextField();
        txtDeskripsiMenu = StyleUtils.createModernTextField();
        
        JPanel pnlHarga = new StyleUtils.RoundedPanel(10, Color.WHITE);
        pnlHarga.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        pnlHarga.setLayout(new BorderLayout());
        JLabel lblRp = new JLabel("  Rp. "); lblRp.setForeground(Color.GRAY);
        txtHargaMenu = new JTextField(); txtHargaMenu.setBorder(null); txtHargaMenu.setOpaque(false);
        txtHargaMenu.addKeyListener(new KeyAdapter() { public void keyTyped(KeyEvent e) { if (!Character.isDigit(e.getKeyChar())) e.consume(); }});
        pnlHarga.add(lblRp, BorderLayout.WEST); pnlHarga.add(txtHargaMenu, BorderLayout.CENTER);

        cbKategoriMenu = new JComboBox<>(new String[]{"MEALS", "DRINK", "SNACK"});
        cbStatusMenu = new JComboBox<>(new String[]{"TRUE (Available)", "FALSE (Empty)"});
        
        lblImagePreview = new JLabel("No Image", SwingConstants.CENTER);
        lblImagePreview.setPreferredSize(new Dimension(140, 140));
        lblImagePreview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        JButton btnPilihFoto = StyleUtils.createRoundedButton("Pilih Foto", AppColor.PRIMARY_PURPLE, Color.WHITE);

        addFormRow(formCard, gbc, 0, "Nama Menu:", txtNamaMenu);
        addFormRow(formCard, gbc, 1, "Deskripsi:", txtDeskripsiMenu);
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.1; formCard.add(new JLabel("Harga:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.9; pnlHarga.setPreferredSize(new Dimension(200, 35)); formCard.add(pnlHarga, gbc);
        addFormRow(formCard, gbc, 3, "Kategori:", cbKategoriMenu);
        addFormRow(formCard, gbc, 4, "Ketersediaan:", cbStatusMenu);

        gbc.gridx = 2; gbc.gridy = 0; gbc.gridheight = 4; gbc.fill = GridBagConstraints.NONE; formCard.add(lblImagePreview, gbc);
        gbc.gridx = 2; gbc.gridy = 4; gbc.gridheight = 1; gbc.fill = GridBagConstraints.HORIZONTAL; formCard.add(btnPilihFoto, gbc);

        // --- BUTTONS (FIX: ADA EDIT KUNING) ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        JButton btnAdd = StyleUtils.createRoundedButton("Tambah", AppColor.BTN_SUCCESS, Color.WHITE);
        JButton btnEdit = StyleUtils.createRoundedButton("Edit", AppColor.BTN_YELLOW, Color.BLACK); // Tombol Kuning
        JButton btnDel = StyleUtils.createRoundedButton("Hapus", AppColor.BTN_RED, Color.WHITE);
        JButton btnReset = StyleUtils.createRoundedButton("Reset", AppColor.BTN_MAROON, Color.WHITE);
        
        btnPanel.add(btnAdd); 
        btnPanel.add(btnEdit); 
        btnPanel.add(btnDel); 
        btnPanel.add(btnReset);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 3; formCard.add(btnPanel, gbc);

        // TABLE
        String[] columns = {"Foto", "ID", "Nama", "Deskripsi", "Harga", "Kategori", "Available"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            public Class<?> getColumnClass(int column) { return column == 0 ? ImageIcon.class : Object.class; }
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        StyleUtils.styleTable(table, 70);
        table.getColumnModel().getColumn(0).setCellRenderer(new StyleUtils.ImageRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        // LOGIC
        btnPilihFoto.addActionListener(e -> chooseImage());
        
        btnAdd.addActionListener(e -> {
            String n = txtNamaMenu.getText(); String p = txtHargaMenu.getText();
            String d = txtDeskripsiMenu.getText(); String c = (String) cbKategoriMenu.getSelectedItem();
            String s = (String) cbStatusMenu.getSelectedItem();
            
            if(n.isEmpty() || p.isEmpty()) { JOptionPane.showMessageDialog(this, "Isi data!"); return; }
            
            btnAdd.setEnabled(false); btnAdd.setText("Loading...");
            new Thread(() -> {
                String msg = "ADD_MENU;" + n + ";" + d + ";" + p + ";" + c + ";" + s;
                String resp = socketService.sendRequest(msg);
                SwingUtilities.invokeLater(() -> {
                    if("SUCCESS".equalsIgnoreCase(resp)) {
                        model.addRow(new Object[]{selectedMenuIcon!=null?StyleUtils.resizeImage(selectedMenuIcon,60,60):null, model.getRowCount()+1, n, d, p, c, s.startsWith("TRUE")});
                        clearForm(); JOptionPane.showMessageDialog(this, "Berhasil!");
                    } else { JOptionPane.showMessageDialog(this, "Gagal: " + resp); }
                    btnAdd.setEnabled(true); btnAdd.setText("Tambah");
                });
            }).start();
        });

        // LOGIC EDIT
        btnEdit.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r >= 0) {
                model.setValueAt(txtNamaMenu.getText(), r, 2);
                model.setValueAt(txtDeskripsiMenu.getText(), r, 3);
                model.setValueAt(txtHargaMenu.getText(), r, 4);
                model.setValueAt(cbKategoriMenu.getSelectedItem(), r, 5);
                String status = (String) cbStatusMenu.getSelectedItem();
                model.setValueAt(status.startsWith("TRUE"), r, 6);
                
                if(selectedMenuIcon != null) {
                    model.setValueAt(StyleUtils.resizeImage(selectedMenuIcon, 60, 60), r, 0);
                }
                clearForm();
                JOptionPane.showMessageDialog(this, "Data berhasil diubah (GUI Only)!");
            } else {
                JOptionPane.showMessageDialog(this, "Pilih baris yang ingin diedit!");
            }
        });

        btnDel.addActionListener(e -> { if(table.getSelectedRow()>=0) model.removeRow(table.getSelectedRow()); });
        btnReset.addActionListener(e -> clearForm());
        
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int r = table.getSelectedRow();
                txtNamaMenu.setText(model.getValueAt(r, 2).toString());
                txtDeskripsiMenu.setText(model.getValueAt(r, 3).toString());
                txtHargaMenu.setText(model.getValueAt(r, 4).toString());
                cbKategoriMenu.setSelectedItem(model.getValueAt(r, 5).toString());
                boolean isAvail = (boolean) model.getValueAt(r, 6);
                cbStatusMenu.setSelectedIndex(isAvail ? 0 : 1);
                ImageIcon thumb = (ImageIcon) model.getValueAt(r, 0);
                if(thumb!=null) { selectedMenuIcon = StyleUtils.resizeImage(thumb, 120, 120); lblImagePreview.setIcon(selectedMenuIcon); lblImagePreview.setText(""); }
                else { lblImagePreview.setIcon(null); lblImagePreview.setText("No Image"); }
            }
        });

        contentGrid.add(formCard, BorderLayout.NORTH);
        contentGrid.add(scrollPane, BorderLayout.CENTER);
        add(contentGrid, BorderLayout.CENTER);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent comp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.1;
        JLabel lbl = new JLabel(label); lbl.setFont(AppColor.FONT_BOLD);
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 0.9;
        comp.setPreferredSize(new Dimension(200, 35));
        panel.add(comp, gbc);
    }

    private void clearForm() {
        txtNamaMenu.setText(""); txtHargaMenu.setText(""); txtDeskripsiMenu.setText("");
        cbKategoriMenu.setSelectedIndex(0); cbStatusMenu.setSelectedIndex(0);
        lblImagePreview.setIcon(null); lblImagePreview.setText("No Image"); selectedMenuIcon = null;
    }

    private void chooseImage() {
        JFileChooser fc = new JFileChooser(); fc.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "png"));
        if(fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedMenuIcon = new ImageIcon(fc.getSelectedFile().getAbsolutePath());
            lblImagePreview.setIcon(StyleUtils.resizeImage(selectedMenuIcon, 120, 120)); lblImagePreview.setText("");
        }
    }
}