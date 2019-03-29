package com.jby.vegeapp.object;

import java.util.ArrayList;

public class HistoryParentObject {
    private String date;
    private ArrayList<BasketHistoryObject> basketHistoryObjectArrayList = new ArrayList<>();
    private ArrayList<PickUpHistoryObject> pickUpHistoryObjectArrayList = new ArrayList<>();
    private ArrayList<DeliveryHistoryObject> deliveryHistoryObjectArrayList = new ArrayList<>();
    public HistoryParentObject(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public ArrayList<BasketHistoryObject> getBasketHistoryObjectArrayList() {
        return basketHistoryObjectArrayList;
    }

    public void setBasketHistoryObjectArrayList(BasketHistoryObject basketHistoryObject) {
        this.basketHistoryObjectArrayList.add(basketHistoryObject);
    }

    public ArrayList<PickUpHistoryObject> getPickUpHistoryObjectArrayList() {
        return pickUpHistoryObjectArrayList;
    }

    public void setPickUpHistoryObjectArrayList(PickUpHistoryObject pickUpHistoryObject) {
        this.pickUpHistoryObjectArrayList.add(pickUpHistoryObject);
    }

    public ArrayList<DeliveryHistoryObject> getDeliveryHistoryObjectArrayList() {
        return deliveryHistoryObjectArrayList;
    }

    public void setDeliveryHistoryObjectArrayList(DeliveryHistoryObject deliveryHistoryObject) {
        this.deliveryHistoryObjectArrayList.add(deliveryHistoryObject);
    }

}
