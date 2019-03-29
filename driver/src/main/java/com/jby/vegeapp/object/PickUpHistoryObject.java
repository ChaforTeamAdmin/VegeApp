package com.jby.vegeapp.object;

import java.io.Serializable;

public class PickUpHistoryObject implements Serializable {
    private String id, farmer_id, farmer, quantity, created_time;

    public PickUpHistoryObject(String id, String farmer_id, String farmer, String quantity, String created_time) {
        this.id = id;
        this.farmer_id = farmer_id;
        this.farmer = farmer;
        this.quantity = quantity;
        this.created_time = created_time;
    }

    public String getId() {
        return id;
    }

    public String getFarmer_id() {
        return farmer_id;
    }

    public String getFarmer() {
        return farmer;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getCreated_time() {
        return created_time;
    }
}
