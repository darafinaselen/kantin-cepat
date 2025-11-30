package app;

import java.awt.Color;
import java.awt.Font;

public class AppColor {
    // --- TEMA UTAMA (UNGU) ---
    public static final Color PRIMARY_PURPLE = new Color(106, 27, 154); // #6A1B9A (Ungu Utama)
    public static final Color DARK_PURPLE = new Color(74, 20, 140);     // #4A148C (Ungu Gelap/Gradient)
    
    // --- BACKGROUND ---
    public static final Color BACKGROUND = new Color(245, 245, 245);    // #F5F5F5
    public static final Color TEXT_MAIN = new Color(38, 50, 56);        // #263238
    
    // --- WARNA SIDEBAR (Nuansa Ungu Muda) ---
    public static final Color SIDEBAR_BG = new Color(243, 229, 245);    // #F3E5F5
    public static final Color SIDEBAR_HOVER = new Color(225, 190, 231); // #E1BEE7
    
    // --- WARNA TOMBOL AKSI ---
    public static final Color BTN_SUCCESS = new Color(46, 125, 50);     // #2E7D32 (HIJAU - Khusus Tombol Tambah)
    public static final Color BTN_MAROON = new Color(128, 0, 0);        // Reset
    public static final Color BTN_RED = new Color(211, 47, 47);         // Hapus/Logout
    public static final Color BTN_YELLOW = new Color(255, 193, 7);      // Ubah (Kuning lebih cerah dikit)
    
    // --- FONT STANDAR ---
    public static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 24);
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("SansSerif", Font.PLAIN, 12);
}