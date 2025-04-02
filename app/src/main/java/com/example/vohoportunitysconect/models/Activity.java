package com.example.vohoportunitysconect.models;

import com.google.firebase.firestore.Exclude;

public class Activity {
    @Exclude
    private String id;
    private String userId;
    private String type;
    private String description;
    private long createdAt;

    public Activity() {
        // Required empty constructor for Firestore
    }

    public Activity(String userId, String type, String description) {
        this.userId = userId;
        this.type = type;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
} 