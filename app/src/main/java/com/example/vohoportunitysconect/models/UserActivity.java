package com.example.vohoportunitysconect.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;
import java.util.Date;

public class UserActivity {
    @Exclude
    private String id;
    private String userId;
    private String opportunityId;
    private String title;
    private String description;
    private Type type;
    private Date timestamp;
    private int hours;

    public enum Type {
        APPLIED,
        ACCEPTED,
        COMPLETED,
        SAVED,
        REVIEWED
    }

    // Empty constructor for Firestore
    public UserActivity() {
    }

    public UserActivity(String userId, String opportunityId, String title, String description, Type type) {
        this.userId = userId;
        this.opportunityId = opportunityId;
        this.title = title;
        this.description = description;
        this.type = type;
        this.timestamp = new Date();
        this.hours = 0;
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }
} 