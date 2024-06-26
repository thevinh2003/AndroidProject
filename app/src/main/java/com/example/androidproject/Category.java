package com.example.androidproject;

import com.google.firebase.database.FirebaseDatabase;

public class Category {
    private String id;
    private String name;
    FirebaseDatabase db = FirebaseDatabase.getInstance();
    public Category() {
    }

    public Category(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
