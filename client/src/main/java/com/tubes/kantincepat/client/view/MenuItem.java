package com.tubes.kantincepat.client.view;

import java.text.NumberFormat;
import java.util.Locale;

public class MenuItem {
    public int id;
    public String name;
    public String description;
    public int price;       
    public String category;
    public String imagePath;
    public boolean isAvailable;

    public MenuItem(int id, String name, String description, int price, String category, String imagePath, boolean isAvailable) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.imagePath = imagePath;
        this.isAvailable = isAvailable;
    }

    public String getFormattedPrice() {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String result = format.format(price);
        return result.replace(",00", "");
    }
}