package com.example.vohoportunitysconect.models;

import com.google.firebase.firestore.Exclude;

public class Statistics {
    @Exclude
    private String id;
    private String userId;
    private int totalOpportunities;
    private int completedOpportunities;
    private int totalHours;
    private int totalApplications;
    private int acceptedApplications;
    private int rejectedApplications;
    private float averageRating;
    private int totalReviews;
    private long createdAt;

    public Statistics() {
        // Required empty constructor for Firestore
    }

    public Statistics(String userId) {
        this.userId = userId;
        this.totalOpportunities = 0;
        this.completedOpportunities = 0;
        this.totalHours = 0;
        this.totalApplications = 0;
        this.acceptedApplications = 0;
        this.rejectedApplications = 0;
        this.averageRating = 0;
        this.totalReviews = 0;
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

    public int getTotalOpportunities() {
        return totalOpportunities;
    }

    public void setTotalOpportunities(int totalOpportunities) {
        this.totalOpportunities = totalOpportunities;
    }

    public int getCompletedOpportunities() {
        return completedOpportunities;
    }

    public void setCompletedOpportunities(int completedOpportunities) {
        this.completedOpportunities = completedOpportunities;
    }

    public int getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(int totalHours) {
        this.totalHours = totalHours;
    }

    public int getTotalApplications() {
        return totalApplications;
    }

    public void setTotalApplications(int totalApplications) {
        this.totalApplications = totalApplications;
    }

    public int getAcceptedApplications() {
        return acceptedApplications;
    }

    public void setAcceptedApplications(int acceptedApplications) {
        this.acceptedApplications = acceptedApplications;
    }

    public int getRejectedApplications() {
        return rejectedApplications;
    }

    public void setRejectedApplications(int rejectedApplications) {
        this.rejectedApplications = rejectedApplications;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public int getTotalReviews() {
        return totalReviews;
    }

    public void setTotalReviews(int totalReviews) {
        this.totalReviews = totalReviews;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
} 