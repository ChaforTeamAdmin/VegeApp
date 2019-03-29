package com.jby.admin.object;

import java.util.ArrayList;

public class RemarkParentObject {
    private String date;
    private ArrayList<RemarkChildObject> remarkChildObjectArrayList = new ArrayList<>();

    public RemarkParentObject(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setRemarkChildObjectArrayList(RemarkChildObject remarkChildObject) {
        this.remarkChildObjectArrayList.add(remarkChildObject);
    }

    public ArrayList<RemarkChildObject> getRemarkChildObjectArrayList() {
        return remarkChildObjectArrayList;
    }
}
