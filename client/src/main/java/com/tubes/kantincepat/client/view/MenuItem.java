package com.tubes.kantincepat.client.view;

import java.text.NumberFormat;
import java.util.Locale;

public class MenuItem {
    public int id;
    public String name;
    public String description;
    public int rawPrice;        // Saya ganti Price jadi rawPrice biar konsisten
    public String category;
    public String imagePath;
    public boolean isAvailable;

    public MenuItem(int id, String name, String description, int rawPrice, 
                    String category, String imagePath, boolean isAvailable) {
        
        this.id = id;
        this.name = name;
        this.description = description;
        this.rawPrice = rawPrice;
        this.category = category;
        this.imagePath = imagePath;
        this.isAvailable = isAvailable;
    }

    public String getFormattedPrice() {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String result = format.format(rawPrice);
        return result.replace(",00", "");
    }
}