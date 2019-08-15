package com.jby.admin.object.entity;

public class FarmerObject {
    private String id, name, phone, address;

    public FarmerObject(String id, String name, String phone, String address) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
    }

    public FarmerObject(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public FarmerObject() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }
}
