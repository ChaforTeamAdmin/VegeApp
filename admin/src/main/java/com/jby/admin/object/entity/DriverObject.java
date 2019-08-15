package com.jby.admin.object.entity;

public class DriverObject {
    private String id, name, nickname, phone;

    public DriverObject(String id, String name, String nickname, String phone) {
        this.id = id;
        this.name = name;
        this.nickname = nickname;
        this.phone = phone;
    }

    public DriverObject() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNickname() {
        return nickname;
    }

    public String getPhone() {
        return phone;
    }
}
