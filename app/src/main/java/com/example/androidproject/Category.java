package com.example.androidproject;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class Category {
    String id;
    String name;

    FirebaseDatabase db = FirebaseDatabase.getInstance();

    public Category(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Category(){}
    public Category(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CompletableFuture<ArrayList<Category>> getAllCategories() {
        CompletableFuture<ArrayList<Category>> future = new CompletableFuture<>();

        db.getReference("Category").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Category> categories = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Category category = snapshot.getValue(Category.class);
                    if (category != null) {
                        categories.add(category);
                    }
                }
                future.complete(categories);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }

    public CompletableFuture<String> getCategoryNameById(String categoryId) {
        CompletableFuture<String> future = new CompletableFuture<>();

        db.getReference("Category").child(categoryId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Category category = dataSnapshot.getValue(Category.class);
                if (category != null) {
                    future.complete(category.getName());
                } else {
                    future.completeExceptionally(new Exception("Category not found"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                future.completeExceptionally(databaseError.toException());
            }
        });

        return future;
    }
}
