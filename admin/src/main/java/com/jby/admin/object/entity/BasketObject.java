package com.jby.admin.object.entity;

public class BasketObject {
    private String id, name, quantity;

    public BasketObject(String id, String name, String quantity) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getQuantity() {
        return quantity;
    }
}
