package com.jby.admin.object;

import java.io.Serializable;

public class RemarkChildObject implements Serializable {
    private String id, product_id, product,farmer, customer, pickUpDriver, deliveryDriver, farmer_weight, customer_weight;
    private String remark_type, remark, remark_status, status;
    private String date;

    public RemarkChildObject(String id, String product_id, String product, String farmer, String customer, String pickUpDriver, String deliveryDriver, String farmer_weight, String customer_weight, String remark_type, String remark, String remark_status, String status, String date) {
        this.id = id;
        this.product_id = product_id;
        this.product = product;
        this.farmer = farmer;
        this.customer = customer;
        this.pickUpDriver = pickUpDriver;
        this.deliveryDriver = deliveryDriver;
        this.farmer_weight = farmer_weight;
        this.customer_weight = customer_weight;
        this.remark_type = remark_type;
        this.remark = remark;
        this.remark_status = remark_status;
        this.status = status;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getProduct_id() {
        return product_id;
    }

    public String getProduct() {
        return product;
    }

    public String getFarmer() {
        return farmer;
    }

    public String getCustomer() {
        return customer;
    }

    public String getPickUpDriver() {
        return pickUpDriver;
    }

    public String getDeliveryDriver() {
        return deliveryDriver;
    }

    public String getFarmer_weight() {
        return farmer_weight;
    }

    public String getCustomer_weight() {
        return customer_weight;
    }

    public String getRemark_type() {
        return remark_type;
    }

    public String getRemark() {
        return remark;
    }

    public String getRemark_status() {
        return remark_status;
    }

    public String getStatus() {
        return status;
    }

    public String getDate() {
        return date;
    }
}
