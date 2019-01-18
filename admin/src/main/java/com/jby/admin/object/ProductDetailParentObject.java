package com.jby.admin.object;

import java.util.ArrayList;

public class ProductDetailParentObject {
    private String id, name, picture, type, price, available_quantity;
    private ArrayList<ProductDetailChildObject> productDetailChildObjectArrayList = new ArrayList<>();

    public ProductDetailParentObject(String id, String name, String picture, String type, String price, String available_quantity) {
        this.id = id;
        this.name = name;
        this.picture = picture;
        this.type = type;
        this.price = price;
        this.available_quantity = available_quantity;
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

    public String getAvailable_quantity() {
        return available_quantity;
    }

    public void setProductDetailChildObjectArrayList(ProductDetailChildObject productDetailChildObject) {
        this.productDetailChildObjectArrayList.add(productDetailChildObject);
    }

    public ArrayList<ProductDetailChildObject> getProductDetailChildObjectArrayList() {
        return productDetailChildObjectArrayList;
    }
}