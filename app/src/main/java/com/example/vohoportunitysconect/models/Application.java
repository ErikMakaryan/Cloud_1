package com.example.vohoportunitysconect.models;

import com.google.firebase.database.Exclude;
import java.util.Date;

public class Application {
    private String id;
    private String userId;
    private String opportunityId;
    private String title;
    private String organization;
    private String status;
    private long appliedAt;
    private String organizerId;

    public Application() {
        // Required empty constructor for Firebase
    }

    public Application(String id, String userId, String opportunityId, String title, 
                      String organization, String status, long appliedAt, String organizerId) {
        this.id = id;
        this.userId = userId;
        this.opportunityId = opportunityId;
        this.title = title;
        this.organization = organization;
        this.status = status;
        this.appliedAt = appliedAt;
        this.organizerId = organizerId;
    }

    public String getId() {
        return id;
    }

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

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(long appliedAt) {
        this.appliedAt = appliedAt;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    @Exclude
    public Status getStatusEnum() {
        return Status.fromString(status);
    }

    public enum Status {
        PENDING,
        ACCEPTED,
        REJECTED;

        public static Status fromString(String status) {
            try {
                return Status.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                return PENDING;
            }
        }
    }
} 