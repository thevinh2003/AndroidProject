package com.example.androidproject;

import java.io.Serializable;
import java.util.ArrayList;

public class Cart implements Serializable {
    private String id;
    private String userName;
    int ProductQuantity;
    private double total = 0.0;
    private double tax = 0.0;

    public String getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setIdUser(String username) {
        this.userName = username;
    }

    public int getProductQuantity() {
        return ProductQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        ProductQuantity = productQuantity;
    }

    public Cart() {}

    public Cart(String id, String username) {
        this.id = id;
        this.userName = username;
    }

    public void updateTotalInCart(ArrayList<Product> listProductSelect){
        this.total = 0.0;
        for (Product product: listProductSelect) {
            this.total += product.toMoney(product.getQuantityInCart());
        }
    }

    public double getTotal() {
        return total;
    }
}
