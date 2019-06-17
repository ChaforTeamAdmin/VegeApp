package com.jby.admin.object;

public class DeliveryOrderObject {
    private String id, prefix, status, print_status, created_time, driver, customer;

    public DeliveryOrderObject(String id, String prefix, String status, String print_status, String created_time, String driver, String customer) {
        this.id = id;
        this.prefix = prefix;
        this.status = status;
        this.print_status = print_status;
        this.created_time = created_time;
        this.driver = driver;
        this.customer = customer;
    }

    public String getId() {
        return id;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getStatus() {
        return status;
    }

    public String getPrint_status() {
        return print_status;
    }

    public String getCreated_time() {
        return created_time;
    }

    public String getDriver() {
        return driver;
    }

    public String getCustomer() {
        return customer;
    }
}
