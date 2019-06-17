package com.jby.vegeapp.object.product;

import java.io.Serializable;
import java.util.ArrayList;

public class ProductParentObject implements Serializable {
    private String id, picture, name, quantity, price, type, weight;
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

    public ProductParentObject(String id, String picture, String name, String quantity, String price, String type, ArrayList<ProductChildObject> productChildObjectArrayList) {
        this.id = id;
        this.picture = picture;
        this.name = name;
        this.quantity = quantity;
        this.price = price;
        this.type = type;
        this.productChildObjectArrayList = productChildObjectArrayList;
    }
    /*
    * printing purpose
    * */
    public ProductParentObject(String name, String quantity, String weight) {
        this.name = name;
        this.quantity = quantity;
        this.weight = weight;
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

    public String getWeight() {
        return weight;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void totalQuantity(String quantity){
        this.quantity = String.valueOf(Integer.valueOf(this.quantity) + Integer.valueOf(quantity));
    }

    public void setAddedProductChildObjectArrayList(ProductChildObject addedProductChildObject) {
        this.productChildObjectArrayList.add(addedProductChildObject);
    }

    public ArrayList<ProductChildObject> getProductChildObjectArrayList() {
        return productChildObjectArrayList;
    }
}
