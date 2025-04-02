package com.example.vohoportunitysconect.models;

import com.google.firebase.firestore.Exclude;

public class Certificate {
    @Exclude
    private String id;
    private String userId;
    private String title;
    private String description;
    private String issuer;
    private String certificateUrl;
    private long issueDate;
    private long expiryDate;
    private boolean isVerified;
    private long createdAt;
    private String skills;
    private int hoursCompleted;

    public Certificate() {
        // Required empty constructor for Firestore
    }

    public Certificate(String userId, String title, String description, String issuer,
                      String certificateUrl, long issueDate, long expiryDate, String skills, int hoursCompleted) {
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.issuer = issuer;
        this.certificateUrl = certificateUrl;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.isVerified = false;
        this.createdAt = System.currentTimeMillis();
        this.skills = skills;
        this.hoursCompleted = hoursCompleted;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getCertificateUrl() {
        return certificateUrl;
    }

    public void setCertificateUrl(String certificateUrl) {
        this.certificateUrl = certificateUrl;
    }

    public long getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(long issueDate) {
        this.issueDate = issueDate;
    }

    public long getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(long expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public int getHoursCompleted() {
        return hoursCompleted;
    }

    public void setHoursCompleted(int hoursCompleted) {
        this.hoursCompleted = hoursCompleted;
    }
} 