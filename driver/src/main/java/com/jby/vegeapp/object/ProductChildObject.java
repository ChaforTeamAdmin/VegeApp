package com.jby.vegeapp.object;

public class ProductChildObject {
    private String id, weight, grade, quantity, farmer;
    private String remark;

    public ProductChildObject(String id, String weight, String quantity, String value, String type) {
        this.id = id;
        this.weight = weight;
        this.quantity = quantity;
        if(type.equals("pick_up")) this.grade = value;
        else this.farmer = value;
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

    public String getFarmer() {
        return farmer;
    }
}
