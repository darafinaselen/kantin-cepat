package com.tubes.kantincepat.client.view;

import java.util.ArrayList;
import java.util.List;
// import java.util.UUID;

public class Order {
    public String orderId;
    public String date;
    public String itemsSummary;
    public String totalPrice;
    public String status;
    public String notes;
    public List<MenuItem> savedItems;

    public Order(String id, String date, String itemsSummary, String totalPrice, String status, String notes, List<MenuItem> items) {
        this.orderId = id; 
        this.date = date;
        this.itemsSummary = itemsSummary;
        this.totalPrice = totalPrice;
        this.status = status;
        this.notes = notes;
        
        if (items != null) {
            this.savedItems = new ArrayList<>(items); 
        } else {
            this.savedItems = new ArrayList<>(); 
        }
    }
}
