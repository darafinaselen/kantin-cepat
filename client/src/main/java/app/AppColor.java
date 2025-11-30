package app;

import java.awt.Color;
import java.awt.Font;

public class AppColor {

    // --- 1. DEFINISI WARNA BARU (SOFT PINK THEME) ---
    public static final Color COLOR_BG = new Color(248, 248, 252);      // Background Putih Kebiruan
    public static final Color COLOR_ACCENT = new Color(246, 237, 243);  // Pink Muda (Sidebar)
    public static final Color COLOR_PRIMARY = new Color(212, 163, 196); // Pink Utama (Header)

    // --- 2. MAPPING KE VARIABLE LAMA (Agar File Lain Tidak Error) ---
    
    // Header & Warna Utama (Menggantikan Ungu)
    public static final Color PRIMARY_PURPLE = COLOR_PRIMARY; 
    // Membuat warna sedikit lebih gelap otomatis untuk efek gradient di header
    public static final Color DARK_PURPLE = new Color(180, 130, 165); 

    // Background Aplikasi
    public static final Color BACKGROUND = COLOR_BG;
    public static final Color TEXT_MAIN = new Color(80, 50, 70); // Teks agak ungu gelap biar cocok

    // Sidebar
    public static final Color SIDEBAR_BG = COLOR_ACCENT; 
    public static final Color SIDEBAR_HOVER = new Color(235, 215, 230); // Sedikit lebih gelap saat di-hover

    // --- 3. WARNA TOMBOL AKSI (Tetap Standar agar User Friendly) ---
    public static final Color BTN_SUCCESS = new Color(46, 125, 50);     // Hijau (Tetap)
    public static final Color BTN_MAROON = new Color(128, 0, 0);        // Merah Tua (Tetap)
    public static final Color BTN_RED = new Color(211, 47, 47);         // Merah (Tetap)
    public static final Color BTN_YELLOW = new Color(255, 193, 7);      // Kuning (Tetap)
    
    // --- 4. FONT CONFIGURATION ---
    public static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 18); // FONT_TITLE
    public static final Font FONT_BOLD = new Font("SansSerif", Font.BOLD, 14);
    public static final Font FONT_REGULAR = new Font("SansSerif", Font.PLAIN, 12); // FONT_PLAIN
    
    // Tambahan untuk kompatibilitas file lama
    public static final Font FONT_SUBTITLE = new Font("SansSerif", Font.PLAIN, 14); 
}