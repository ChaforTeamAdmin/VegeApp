package com.jby.vegeapp.object.history;

import java.io.Serializable;

public class DeliveryHistoryObject implements Serializable {
    private String id, do_id, customer_id, customer, quantity, created_time;

    public DeliveryHistoryObject(String id, String do_id, String customer_id, String customer, String quantity, String created_time) {
        this.id = id;
        this.do_id = do_id;
        this.customer_id = customer_id;
        this.customer = customer;
        this.quantity = quantity;
        this.created_time = created_time;
    }

    public String getId() {
        return id;
    }

    public String getDo_id() {
        return do_id;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public String getCustomer() {
        return customer;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getCreated_time() {
        return created_time;
    }
}
