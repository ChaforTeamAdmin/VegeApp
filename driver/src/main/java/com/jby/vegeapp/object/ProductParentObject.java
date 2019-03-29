package com.jby.vegeapp.object;

import java.util.ArrayList;

public class ProductParentObject {
    private String id, picture, name, quantity, price, type;
    private ArrayList<ProductChildObject> productChildObjectArrayList = new ArrayList<>();

    public ProductParentObject(String id, String picture, String name, String quantity, String price, String type) {
        this.id = id;
        this.picture = picture;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.type = type;
    }

    public ProductParentObject(String id, String picture, String name, String quantity, String type,  ArrayList<ProductChildObject> productChildObjectArrayList) {
        this.id = id;
        this.picture = picture;
        this.name = name;
        this.quantity = quantity;
        this.type = type;
        this.productChildObjectArrayList = productChildObjectArrayList;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getPicture() {
        return picture;
    }

    public String getName() {
        return name;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getPrice() {
        return price;
    }

    public void setAddedProductChildObjectArrayList(ProductChildObject addedProductChildObject) {
        this.productChildObjectArrayList.add(addedProductChildObject);
    }

    public ArrayList<ProductChildObject> getProductChildObjectArrayList() {
        return productChildObjectArrayList;
    }
}
