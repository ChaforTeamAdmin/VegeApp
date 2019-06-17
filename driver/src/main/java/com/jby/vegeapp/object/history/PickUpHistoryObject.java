package com.jby.vegeapp.object.history;

import java.io.Serializable;

public class PickUpHistoryObject implements Serializable {
    private String ro_id, id, farmer_id, farmer, quantity, created_time;

    public PickUpHistoryObject(String ro_id, String id, String farmer_id, String farmer, String quantity, String created_time) {
        this.ro_id = ro_id;
        this.id = id;
        this.farmer_id = farmer_id;
        this.farmer = farmer;
        this.quantity = quantity;
        this.created_time = created_time;
    }

    public String getRo_id() {
        return ro_id;
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
