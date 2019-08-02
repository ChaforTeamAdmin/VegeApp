package com.jby.admin.object;

import java.util.ArrayList;

public class ExpandableParentObject {
    private String date;
    private ArrayList<DeliveryOrderObject> deliveryOrderObjectArrayList = new ArrayList<>();

    public ExpandableParentObject(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setDeliveryOrderObjectArrayList(DeliveryOrderObject deliveryOrderObject) {
        this.deliveryOrderObjectArrayList.add(deliveryOrderObject);
    }

    public ArrayList<DeliveryOrderObject> getDeliveryOrderObjectArrayList() {
        return deliveryOrderObjectArrayList;
    }
}
