package com.jby.vegeapp.database;

public interface ResultCallBack {
    void createResult(String status);
    void readResult(String result);
    void updateResult(String status);
    void deleteResult(String status);
}
