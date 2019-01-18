package com.jby.admin.object;

public class ProductDetailChildObject {
    private String id, driverID, driverName, farmerID, farmerName;
    private String price, grade, quantity, date, time;

    public ProductDetailChildObject(String id, String driverID, String driverName, String farmerID, String farmerName, String price, String grade, String quantity, String date, String time) {
        this.id = id;
        this.driverID = driverID;
        this.driverName = driverName;
        this.farmerID = farmerID;
        this.farmerName = farmerName;
        this.price = price;
        this.grade = grade;
        this.quantity = quantity;
        this.date = date;
        this.time = time;
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
}
