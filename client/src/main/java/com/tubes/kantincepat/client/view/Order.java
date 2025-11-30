package com.tubes.kantincepat.client.view;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Order {
    String orderId;
    String date;
    String itemsSummary;
    String totalPrice;
    String status;
    String notes;
    List<MenuItem> savedItems; 

    public Order(String date, String itemsSummary, String totalPrice, String status, String notes, List<MenuItem> items) {
        this.orderId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();        this.date = date;
        this.date = date;
        this.itemsSummary = itemsSummary;
        this.totalPrice = totalPrice;
        this.status = status;
        this.notes = notes;
        
        if (items != null) {
            this.savedItems = new ArrayList<>(items); // Copy data
        } else {
            this.savedItems = new ArrayList<>(); // List kosong (jaga-jaga)
        }
    }
}
