package com.jby.admin.object;

import java.util.ArrayList;
import java.util.List;

public class ProductDetailParentObject {
    private String id, name, picture, type, price, available_quantity, taken_quantity, quantity, product_code;
    private int priority;
    private String[] unavailableID, unavailableFarmer;
    private ArrayList<ProductDetailChildObject> productDetailChildObjectArrayList = new ArrayList<>();

    /*
     * for unavailable list purpose
     * */
    public ProductDetailParentObject(String id, String[] unavailableID, String[] unavailableFarmer) {
        this.id = id;
        this.unavailableID = unavailableID;
        this.unavailableFarmer = unavailableFarmer;
    }

    /*
     * for stock control list purpose
     * */
    public ProductDetailParentObject(String id, String available_quantity, String taken_quantity) {
        this.id = id;
        this.available_quantity = available_quantity;
        this.taken_quantity = taken_quantity;
    }

    /*
     * stock fragment (expandable list's parent item)
     * */
    public ProductDetailParentObject(String id, String name, String picture, String type, String price, String available_quantity, String taken_quantity, String product_code) {
        this.id = id;
        this.name = name;
        this.picture = picture;
        this.type = type;
        this.price = price;
        this.available_quantity = available_quantity;
        this.taken_quantity = taken_quantity;
        this.product_code = product_code;
    }

    /*
     * delivery order detail list view purpose
     * */
    public ProductDetailParentObject(String id, String name, String picture, String type, String price, String available_quantity, ArrayList<ProductDetailChildObject> productDetailChildObjectArrayList) {
        this.id = id;
        this.name = name;
        this.picture = picture;
        this.type = type;
        this.price = price;
        this.available_quantity = available_quantity;
        this.productDetailChildObjectArrayList = productDetailChildObjectArrayList;
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

    public String[] getUnavailableID() {
        return unavailableID;
    }

    public String[] getUnavailableFarmer() {
        return unavailableFarmer;
    }

    public String getAvailable_quantity() {
        return available_quantity;
    }

    public String getTaken_quantity() {
        return taken_quantity;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getProduct_code() {
        return product_code;
    }

    public int getPriority() {
        return priority;
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

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setProductDetailChildObjectArrayList(ArrayList<ProductDetailChildObject> productDetailChildObjectArrayList) {
        this.productDetailChildObjectArrayList = productDetailChildObjectArrayList;
    }

    public void setAvailable_quantity(String available_quantity) {
        this.available_quantity = available_quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setTaken_quantity(String taken_quantity) {
        this.taken_quantity = taken_quantity;
    }

    public void setUnavailableID(String[] unavailableID) {
        this.unavailableID = unavailableID;
    }

    public void setUnavailableFarmer(String[] unavailableFarmer) {
        this.unavailableFarmer = unavailableFarmer;
    }

    public void setProductDetailChildObjectArrayList(ProductDetailChildObject productDetailChildObject) {
        this.productDetailChildObjectArrayList.add(productDetailChildObject);
    }

    public ArrayList<ProductDetailChildObject> getProductDetailChildObjectArrayList() {
        return productDetailChildObjectArrayList;
    }
}
