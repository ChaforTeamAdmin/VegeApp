package com.jby.admin.object;

import java.util.ArrayList;

public class ProductDetailParentObject {
    private String id, name, picture, type, price, available_quantity, quantity, product_code;
    private String[] unavailableID;
    private ArrayList<StockObject> stockObjectArrayList = new ArrayList<>();

    /*
     * stock fragment (expandable list's parent item)
     * */
    public ProductDetailParentObject(String id, String name, String picture, String type, String price, String product_code, String available_quantity) {
        this.id = id;
        this.name = name;
        this.picture = picture;
        this.type = type;
        this.price = price;
        this.product_code = product_code;
        this.available_quantity = available_quantity;
    }

    /*
     * delivery order detail list view purpose
     * */
    public ProductDetailParentObject(String id, String name, String picture, String type, String price, String available_quantity, ArrayList<StockObject> stockObjectArrayList) {
        this.id = id;
        this.name = name;
        this.picture = picture;
        this.type = type;
        this.price = price;
        this.available_quantity = available_quantity;
        this.stockObjectArrayList = stockObjectArrayList;
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

    public String[] getUnavailableID() {
        return unavailableID;
    }

    public String getAvailable_quantity() {
        return available_quantity;
    }

    public String getQuantity() {
        return quantity;
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

    public void setAvailable_quantity(String available_quantity) {
        this.available_quantity = available_quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public void setUnavailableID(String[] unavailableID) {
        this.unavailableID = unavailableID;
    }

    public void setStockObjectArrayList(StockObject farmerObject) {
        this.stockObjectArrayList.add(farmerObject);
    }

    public ArrayList<StockObject> getStockObjectArrayList() {
        return stockObjectArrayList;
    }
}
