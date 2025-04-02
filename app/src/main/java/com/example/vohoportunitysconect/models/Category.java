package com.example.vohoportunitysconect.models;

import com.google.firebase.firestore.Exclude;

public class Category {
    @Exclude
    private String id;
    private String name;
    private String description;
    private String iconUrl;
    private long createdAt;

    public Category() {
        // Required empty constructor for Firestore
    }

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.createdAt = System.currentTimeMillis();
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
} 