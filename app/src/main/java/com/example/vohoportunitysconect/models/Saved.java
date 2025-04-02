package com.example.vohoportunitysconect.models;

import com.google.firebase.firestore.Exclude;

public class Saved {
    @Exclude
    private String id;
    private String opportunityId;
    private String userId;
    private long createdAt;

    public Saved() {
        // Required empty constructor for Firestore
    }

    public Saved(String opportunityId, String userId) {
        this.opportunityId = opportunityId;
        this.userId = userId;
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

    public String getOpportunityId() {
        return opportunityId;
    }

    public void setOpportunityId(String opportunityId) {
        this.opportunityId = opportunityId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
} 