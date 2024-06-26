package com.example.androidproject;

import java.io.Serializable;
import java.util.ArrayList;

public class Cart implements Serializable {
    private String id;
    private String userId;
    int ProductQuantity;
    private double total = 0.0;
    private double tax = 0.0;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getProductQuantity() {
        return ProductQuantity;
    }

    public void setProductQuantity(int productQuantity) {
        ProductQuantity = productQuantity;
    }

    public Cart() {}

    public Cart(String id, String userId) {
        this.id = id;
        this.userId = userId;
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
