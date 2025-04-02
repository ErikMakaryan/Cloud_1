package com.example.vohoportunitysconect.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Date;

@IgnoreExtraProperties
public class Application {
    @Exclude
    private String id;
    private String userId;
    private String opportunityId;
    private String opportunityTitle;
    private String organizationId;
    private String organizationName;
    private String organizationImageUrl;
    private String status;
    private String coverLetter;
    private long createdAt;
    private long updatedAt;

    public enum Status {
        PENDING("Pending"),
        ACCEPTED("Accepted"),
        REJECTED("Rejected"),
        WITHDRAWN("Withdrawn");

        private final String displayName;

        Status(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public static Status fromString(String status) {
            for (Status s : Status.values()) {
                if (s.name().equalsIgnoreCase(status)) {
                    return s;
                }
            }
            return PENDING;
        }
    }

    public Application() {
        // Required empty constructor for Realtime Database
    }

    public Application(String userId, String opportunityId, String coverLetter) {
        this.userId = userId;
        this.opportunityId = opportunityId;
        this.coverLetter = coverLetter;
        this.status = Status.PENDING.name();
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public Application(String id, String userId, String opportunityId, String opportunityTitle,
                      String organizationId, String organizationName, String organizationImageUrl,
                      Date appliedDate, Status status, String coverLetter) {
        this.id = id;
        this.userId = userId;
        this.opportunityId = opportunityId;
        this.opportunityTitle = opportunityTitle;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.organizationImageUrl = organizationImageUrl;
        this.status = status.name();
        this.coverLetter = coverLetter;
        this.createdAt = appliedDate != null ? appliedDate.getTime() : System.currentTimeMillis();
        this.updatedAt = this.createdAt;
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

    public String getOpportunityTitle() {
        return opportunityTitle;
    }

    public void setOpportunityTitle(String opportunityTitle) {
        this.opportunityTitle = opportunityTitle;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationImageUrl() {
        return organizationImageUrl;
    }

    public void setOrganizationImageUrl(String organizationImageUrl) {
        this.organizationImageUrl = organizationImageUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    @Exclude
    public Status getStatusEnum() {
        return Status.fromString(status);
    }

    @Exclude
    public void setStatusEnum(Status status) {
        this.status = status.name();
        this.updatedAt = System.currentTimeMillis();
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Exclude
    public Date getAppliedDate() {
        return new Date(createdAt);
    }
} 