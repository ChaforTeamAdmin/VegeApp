package com.jby.vegeapp.object;

public class ProductObject {
    private String id, name, picture, type, price, quantity;

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
}
