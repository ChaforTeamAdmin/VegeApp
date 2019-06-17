package com.jby.vegeapp.object.history;

import java.io.Serializable;

public class BasketHistoryObject implements Serializable {
    private String id, quantity, type, farmer_id, customer_id, stock_id, created_time, created_date;
    private String farmer, customer;

    public BasketHistoryObject(String id, String quantity, String type, String farmer_id, String customer_id, String stock_id, String created_time, String created_date, String farmer, String customer) {
        this.id = id;
        this.quantity = quantity;
        this.type = type;
        this.farmer_id = farmer_id;
        this.customer_id = customer_id;
        this.stock_id = stock_id;
        this.created_time = created_time;
        this.created_date = created_date;
        this.farmer = farmer;
        this.customer = customer;
    }

    public String getId() {
        return id;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getType() {
        return type;
    }

    public String getFarmer_id() {
        return farmer_id;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public String getStock_id() {
        return stock_id;
    }

    public String getCreated_time() {
        return created_time;
    }

    public String getCreated_date() {
        return created_date;
    }

    public String getFarmer() {
        return farmer;
    }

    public String getCustomer() {
        return customer;
    }
}
