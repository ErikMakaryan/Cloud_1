package com.example.vohoportunitysconect.models;

import java.util.Date;

public class Rating {
    private String id;
    private String userId;
    private String targetId; // ID of the opportunity or organization being rated
    private String targetType; // "opportunity" or "organization"
    private float rating; // 1-5 stars
    private String comment;
    private Date timestamp;

    // Required empty constructor for Firestore
    public Rating() {}

    public Rating(String id, String userId, String targetId, String targetType, float rating, String comment, Date timestamp) {
        this.id = id;
        this.userId = userId;
        this.targetId = targetId;
        this.targetType = targetType;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
} 