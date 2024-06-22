package com.example.androidproject;

public class Bill {
    private String id;
    private String userName;
    private Double total = 0.0;
    private String createDay = null;

    private String address;

    public String getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public void setId(String idBill) {
        this.id = idBill;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Bill() {}

    public Bill(String userName, String createDay, Double total) {
        this.userName = userName;
        this.createDay = createDay;
        this.total = total;
    }
    public Bill(String id, String userName, String createDay, Double total, String address) {
        this.id = id;
        this.userName = userName;
        this.createDay = createDay;
        this.total = total;
        this.address = address;
    }

    public Double getToTal() {
        return total;
    }

    public void setToTal(Double toTal) {
        this.total = toTal;
    }

    public String getCreateDay() {
        return createDay;
    }

    public void setCreateDay(String createDay) {
        this.createDay = createDay;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

}
