package com.jby.admin.object;

public class StockObject {
    private String date, totalQuantity, totalWeight;

    public StockObject(String date, String totalWeight, String totalQuantity) {
        this.date = date;
        this.totalWeight = totalWeight;
        this.totalQuantity = totalQuantity;

    }

    public String getDate() {
        return date;
    }

    public String getTotalQuantity() {
        return totalQuantity;
    }

    public String getTotalWeight() {
        return totalWeight;
    }
}
