package com.jby.admin.object;

public class ProductDetailChildObject {
    private String id, driverID, driverName, farmerID, farmerName;
    private String price, grade, quantity, date, time, status, weight, takenQuantity;
    private String do_id;
    private String deliveryRemarkStatus, pickUpRemarkStatus;


    public ProductDetailChildObject(String farmerID, String farmerName, String quantity, String takenQuantity) {
        this.farmerID = farmerID;
        this.farmerName = farmerName;
        this.quantity = quantity;
        this.takenQuantity = takenQuantity;
    }

    public ProductDetailChildObject(String id, String price, String grade, String date, String status, String weight, String do_id, String deliveryRemarkStatus, String pickUpRemarkStatus) {
        this.id = id;
        this.do_id = do_id;
        this.price = price;
        this.grade = grade;
        this.date = date;
        this.status = status;
        this.weight = weight;
        this.deliveryRemarkStatus = deliveryRemarkStatus;
        this.pickUpRemarkStatus = pickUpRemarkStatus;
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
}
