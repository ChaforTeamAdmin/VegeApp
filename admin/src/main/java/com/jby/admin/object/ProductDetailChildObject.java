package com.jby.admin.object;

import java.io.Serializable;

public class ProductDetailChildObject implements Serializable {
    private String id, driverID, driverName, farmerID, farmerName;
    private String price, grade, quantity, date, time, status, weight, selfAbsorbWeight, takenQuantity;
    private String deliveryRemarkStatus, pickUpRemarkStatus;
    private String do_id, customer_id, customer;

    public ProductDetailChildObject() {
    }

    public ProductDetailChildObject(String id, String weight) {
        this.id = id;
        this.weight = weight;
    }
    /*
    * for stock control purpose (when the list is refresh or searching)
    * */
    public ProductDetailChildObject(String farmerID, String quantity, String takenQuantity) {
        this.farmerID = farmerID;
        this.quantity = quantity;
        this.takenQuantity = takenQuantity;
    }

    public ProductDetailChildObject(String farmerID, String farmerName, String quantity, String takenQuantity) {
        this.farmerID = farmerID;
        this.farmerName = farmerName;
        this.quantity = quantity;
        this.takenQuantity = takenQuantity;
    }

    public ProductDetailChildObject(String id, String price, String grade, String date, String status, String weight, String selfAbsorbWeight, String do_id, String deliveryRemarkStatus, String pickUpRemarkStatus) {
        this.id = id;
        this.do_id = do_id;
        this.price = price;
        this.grade = grade;
        this.date = date;
        this.status = status;
        this.weight = weight;
        this.selfAbsorbWeight = selfAbsorbWeight;
        this.deliveryRemarkStatus = deliveryRemarkStatus;
        this.pickUpRemarkStatus = pickUpRemarkStatus;
    }

    public ProductDetailChildObject(String id, String weight, String grade, String date, String do_id) {
        this.id = id;
        this.weight = weight;
        this.grade = grade;
        this.date = date;
        this.do_id = do_id;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public String getCustomer() {
        return customer;
    }

    public String getTakenQuantity() {
        return takenQuantity;
    }

    public String getId() {
        return id;
    }

    public String getDriverID() {
        return driverID;
    }

    public String getDriverName() {
        return driverName;
    }

    public String getFarmerID() {
        return farmerID;
    }

    public String getFarmerName() {
        return farmerName;
    }

    public String getPrice() {
        return price;
    }

    public String getGrade() {
        return grade;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    public String getWeight() {
        return weight;
    }

    public String getDo_id() {
        return do_id;
    }

    public String getSelfAbsorbWeight() {
        return selfAbsorbWeight;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setTakenQuantity(String takenQuantity) {
        this.takenQuantity = takenQuantity;
    }

    public String getDeliveryRemarkStatus() {
        return deliveryRemarkStatus;
    }

    public String getPickUpRemarkStatus() {
        return pickUpRemarkStatus;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDriverID(String driverID) {
        this.driverID = driverID;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public void setFarmerID(String farmerID) {
        this.farmerID = farmerID;
    }

    public void setFarmerName(String farmerName) {
        this.farmerName = farmerName;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public void setDeliveryRemarkStatus(String deliveryRemarkStatus) {
        this.deliveryRemarkStatus = deliveryRemarkStatus;
    }

    public void setPickUpRemarkStatus(String pickUpRemarkStatus) {
        this.pickUpRemarkStatus = pickUpRemarkStatus;
    }

    public void setDo_id(String do_id) {
        this.do_id = do_id;
    }

    public void setCustomer_id(String customer_id) {
        this.customer_id = customer_id;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public void joinAllWeight(String weight) {
        this.weight = this.weight + ", " + weight;
    }

    public void totalQuantity(String quantity) {
        this.quantity = String.valueOf(Integer.valueOf(this.quantity) + Integer.valueOf(quantity));
    }
}
