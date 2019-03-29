package com.jby.vegeapp.object;

import java.io.Serializable;

public class CustomerObject implements Serializable {
    private String do_id, do_prefix, id, name, nickname, phone, address;
    private String totalDeliverQuantity;

    public CustomerObject(String do_id, String do_prefix, String id, String name, String nickname, String phone, String address, String totalDeliverQuantity) {
        this.do_id = do_id;
        this.do_prefix = do_prefix;
        this.id = id;
        this.name = name;
        this.nickname = nickname;
        this.phone = phone;
        this.address = address;
        this.totalDeliverQuantity = totalDeliverQuantity;
    }

    public String getDo_id() {
        return do_id;
    }

    public String getDo_prefix() {
        return do_prefix;
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

    public String getAddress() {
        return address;
    }

    public String getTotalDeliverQuantity() {
        return totalDeliverQuantity;
    }
}
