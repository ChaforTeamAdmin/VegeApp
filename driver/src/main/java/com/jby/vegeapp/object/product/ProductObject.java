package com.jby.vegeapp.object.product;

public class ProductObject {
    private String id;
    private String name;
    private String picture;
    private String type;
    private String price;
    private String quantity;
    private String product_code;

    public ProductObject() {
    }

    public ProductObject(String id, String name, String picture, String type, String price) {
        this.id = id;
        this.name = name;
        this.picture = picture;
        this.type = type;
        this.price = price;
    }

    public ProductObject(String id, String name, String picture, String type, String price, String quantity) {
        this.id = id;
        this.name = name;
        this.picture = picture;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPicture() {
        return picture;
    }

    public String getType() {
        return type;
    }

    public String getPrice() {
        return price;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getProduct_code() {
        return product_code;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setProduct_code(String product_code) {
        this.product_code = product_code;
    }
}
