package com.jby.admin.object;

public class SelectedItemObject {
    private String id, available, taken;

    public SelectedItemObject() {
    }

    public SelectedItemObject(String id, String available, String taken) {
        this.id = id;
        this.available = available;
        this.taken = taken;
    }

    public String getId() {
        return id;
    }

    public String getAvailable() {
        return available;
    }

    public String getTaken() {
        return taken;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setAvailable(String available) {
        this.available = String.valueOf(Integer.valueOf(available) + 1);
    }

    public void setTaken(String taken) {
        this.taken = String.valueOf(Integer.valueOf(taken) + 1);
    }
}
