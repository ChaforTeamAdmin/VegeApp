package com.jby.vegeapp.object.product;

import java.io.Serializable;

public class ProductChildObject implements Serializable {
    private String id, weight, grade, quantity, farmer, farmer_id, status;
    private String remark;

    public ProductChildObject(String id, String weight, String quantity, String grade) {
        this.id = id;
        this.weight = weight;
        this.quantity = quantity;
        this.grade = grade;
    }

    public ProductChildObject(String id, String weight, String quantity, String farmer, String farmer_id) {
        this.id = id;
        this.weight = weight;
        this.quantity = quantity;
        this.farmer = farmer;
        this.farmer_id = farmer_id;
    }

    public ProductChildObject() {
    }

    public String getId() {
        return id;
    }

    public String getWeight() {
        return weight;
    }

    public String getGrade() {
        return grade;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getRemark() {
        return remark;
    }

    public String getFarmer() {
        return farmer;
    }

    public String getStatus() {
        return status;
    }

    public String getFarmer_id() {
        return farmer_id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setFarmer(String farmer) {
        this.farmer = farmer;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setFarmer_id(String farmer_id) {
        this.farmer_id = farmer_id;
    }

    public void joinAllWeight(String weight) {
        this.weight = this.weight + ", " + weight;
    }

    public void totalQuantity(String quantity){
        this.quantity = String.valueOf(Integer.valueOf(this.quantity) + Integer.valueOf(quantity));
    }
}
