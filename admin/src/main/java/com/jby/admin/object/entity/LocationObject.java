package com.jby.admin.object.entity;

public class LocationObject {
    private String poId, locationId, location;

    public LocationObject() {
    }

    /*
     * for deduction purpose (stock fragment)
     * */
    public LocationObject(String poId, String locationId, String location) {
        this.poId = poId;
        this.location = location;
    }

    /*
     * for add product dialog (DO PO)
     * */
    public LocationObject(String locationId, String location) {
        this.locationId = locationId;
        this.location = location;
    }

    public String getPoId() {
        return poId;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return location;
    }
}
