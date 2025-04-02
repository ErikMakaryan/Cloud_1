package com.example.vohoportunitysconect.models;

import com.google.firebase.firestore.Exclude;

public class Review {
    @Exclude
    private String id;
    private String userId;
    private String opportunityId;
    private float rating;
    private String comment;
    private long createdAt;

    public Review() {
        // Required empty constructor for Firestore
    }

    public Review(String userId, String opportunityId, float rating, String comment) {
        this.userId = userId;
        this.opportunityId = opportunityId;
        this.rating = rating;
        this.comment = comment;
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

    public String getOpportunityId() {
        return opportunityId;
    }

    public void setOpportunityId(String opportunityId) {
        this.opportunityId = opportunityId;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
} 