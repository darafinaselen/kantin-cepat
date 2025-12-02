package com.tubes.kantincepat.client.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.net.URL;
import java.io.InputStream;

public class GUIUtils {
    // Warna Global
    public static final Color COLOR_BG = Color.decode("#FFFFFF");
    public static final Color COLOR_BTN = Color.decode("#D4A3C4");
    public static final Color COLOR_TEXT = Color.decode("#000000");

    public static ImageIcon loadImageIcon(String fileName, int width, int height) {
        try {
            URL imgURL = GUIUtils.class.getResource("/assets/" + fileName);
            if (imgURL != null) {
                ImageIcon originalIcon = new ImageIcon(imgURL);
                Image image = originalIcon.getImage();
                Image newimg = image.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
                return new ImageIcon(newimg);
            } else {
                System.err.println("Gambar tidak ditemukan: /assets/" + fileName);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Font getCustomFont(String fontFileName, float size) {
        try {
            // Load dari folder resources/fonts/
            InputStream is = GUIUtils.class.getResourceAsStream("/fonts/" + fontFileName);
            if (is == null) {
                System.err.println("Font tidak ditemukan: " + fontFileName);
                return new Font("Arial", Font.BOLD, (int) size); // Fallback kalau gagal
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            return font.deriveFont(size);
        } catch (Exception e) {
            e.printStackTrace();
            return new Font("Arial", Font.BOLD, (int) size);
        }
    }

    // Method tambah Logo
    public static void addLogo(JPanel panel) {
        try {
            URL imgURL = GUIUtils.class.getResource("/images/logo_kantin.png");
            if (imgURL != null) {
                ImageIcon originalIcon = new ImageIcon(imgURL);
                Image image = originalIcon.getImage();
                Image newimg = image.getScaledInstance(150, 120, java.awt.Image.SCALE_SMOOTH);
                JLabel labelGambar = new JLabel(new ImageIcon(newimg));
                labelGambar.setAlignmentX(Component.CENTER_ALIGNMENT);
                panel.add(labelGambar);
                panel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        } catch (Exception e) {
            System.err.println("Gagal load gambar logo.");
        }
    }

    private static class RoundedFieldStyle extends JTextField {
        private int arcWidth = 20; 
        private Color fillColor = Color.decode("#D9D9D9"); // Warna Fill
        private Color shadowColor = new Color(0, 0, 0, 64); // Hitam Transparan (25%)

        public RoundedFieldStyle(String placeholder) {
            setOpaque(false); 
            setBorder(new EmptyBorder(10, 15, 10, 15)); // Padding Text (Atas, Kiri, Bawah, Kanan)
            setFont(GUIUtils.getCustomFont("Lato-Regular.ttf", 12f));
            setToolTipText(placeholder); 
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int shadowSize = 4; // Ukuran bayangan (Y=4)

            // 1. Gambar SHADOW dulu (di bawah)
            // Digeser ke bawah (+shadowSize)
            g2.setColor(shadowColor);
            g2.fillRoundRect(1, shadowSize, width - 2, height - shadowSize - 1, arcWidth, arcWidth);

            // 2. Gambar BACKGROUND UTAMA (di atas shadow)
            // Dikurangi tinggi shadow biar ga ketutup
            g2.setColor(fillColor); 
            g2.fillRoundRect(0, 0, width - 1, height - shadowSize - 1, arcWidth, arcWidth);

            g2.dispose();
            
            // 3. Gambar Teks (super)
            super.paintComponent(g);
        }
    }
    
    private static class RoundedPassStyle extends JPasswordField {
        private int arcWidth = 20;
        private Color fillColor = Color.decode("#D9D9D9");
        private Color shadowColor = new Color(0, 0, 0, 64);

        public RoundedPassStyle() {
            setOpaque(false);
            setBorder(new EmptyBorder(10, 15, 10, 15));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int shadowSize = 4;

            g2.setColor(shadowColor);
            g2.fillRoundRect(1, shadowSize, width - 2, height - shadowSize - 1, arcWidth, arcWidth);

            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, width - 1, height - shadowSize - 1, arcWidth, arcWidth);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Class khusus untuk Tombol Bulat + Shadow
    private static class RoundedButtonStyle extends JButton {
        private Color shadowColor = Color.decode("#9E768F");
        private Color hoverColor;
        private Color normalColor;
        private Color pressedColor;

        public RoundedButtonStyle(String text, Color baseColor) {
            super(text);
            this.normalColor = baseColor;
            this.hoverColor = baseColor.brighter(); 
            this.pressedColor = baseColor.darker(); 
            
            setContentAreaFilled(false); 
            setFocusPainted(false);      
            setBorderPainted(false);     
            setOpaque(false);
            setBorder(new EmptyBorder(10, 15, 10, 15));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int shadowSize = 4; 
            
            int arcHeight = height - shadowSize; 
            int arcWidth = arcHeight;

            if (getModel().isArmed()) {
                g2.setColor(pressedColor);
                g2.fillRoundRect(0, shadowSize / 2, width, height - shadowSize, arcWidth, arcHeight);
            } else {
                g2.setColor(shadowColor);
                g2.fillRoundRect(2, shadowSize, width - 4, height - shadowSize, arcWidth, arcHeight);

                if (getModel().isRollover()) {
                    g2.setColor(hoverColor);
                } else {
                    g2.setColor(normalColor);
                }
                g2.fillRoundRect(0, 0, width, height - shadowSize, arcWidth, arcHeight);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    public static JTextField createRoundedTextField(String tooltip) {
        JTextField field = new RoundedFieldStyle(tooltip);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45)); 
        return field;
    }

    public static JTextField createRoundedTextFieldForKitchen(String tooltip) {
        JTextField field = new RoundedFieldStyle(tooltip);
        field.setMaximumSize(new Dimension(300, 45)); 
        return field;
    }

    public static JPasswordField createRoundedPasswordField() {
        JPasswordField field = new RoundedPassStyle();
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        return field;
    }

    public static JPasswordField createRoundedPasswordFieldForKitchen() {
        JPasswordField field = new RoundedPassStyle();
        field.setMaximumSize(new Dimension(300, 45));
        return field;
    }

    // Method bikin Tombol Cantik
    public static JButton createStyledButton(String text) {
        // Pakai class RoundedButtonStyle yang baru
        JButton btn = new RoundedButtonStyle(text, COLOR_BTN);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        // Ukuran tombol
        btn.setPreferredSize(new Dimension(300, 45)); 
        btn.setMaximumSize(new Dimension(300, 45));
        // Styling Teks
        btn.setForeground(Color.BLACK); // Sesuai Figma (Teks Hitam)
        btn.setFont(getCustomFont("Lato-Bold.ttf", 16f)); // Font agak besar
        // Ubah cursor jadi tangan saat di-hover
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }


    // Helper nambah label + field biar rapi
    public static void addLabelAndField(JPanel p, String labelText, JComponent field) {
        JPanel labelWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        labelWrapper.setOpaque(false);
        
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 14f));
        labelWrapper.add(lbl);

        labelWrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, lbl.getPreferredSize().height));

        p.add(labelWrapper);
        p.add(field);
        p.add(Box.createRigidArea(new Dimension(0, 10)));
    }

    public static void addLabelAndFieldForKitchen(JPanel p, String labelText, JComponent field) {
        // Wrapper vertikal untuk label + field
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);
        group.setAlignmentX(Component.CENTER_ALIGNMENT); // seluruh group di tengah

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(GUIUtils.getCustomFont("Lato-Bold.ttf", 14f));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);    // teks label di tengah

        // pastikan field juga di tengah
        field.setAlignmentX(Component.CENTER_ALIGNMENT);

        group.add(lbl);
        group.add(Box.createRigidArea(new Dimension(0, 5)));
        group.add(field);

        p.add(group);
        p.add(Box.createRigidArea(new Dimension(0, 10)));
    }

}