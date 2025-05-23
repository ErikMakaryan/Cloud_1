package com.example.vohoportunitysconect.models;

import com.google.firebase.database.Exclude;
import java.util.Date;

public class Application {
    private String id;
    private String opportunityId;
    private String opportunityTitle;
    private String organizationName;
    private String volunteerId;
    private String volunteerName;
    private String volunteerEmail;
    private String organizerId;
    private String message;
    private String status;
    private Date appliedDate;

    public Application() {
        // Required empty constructor for Firebase
    }

    public Application(String opportunityId, String opportunityTitle, String organizationName,
                      String volunteerId, String volunteerName, String volunteerEmail,
                      String organizerId, String message) {
        this.opportunityId = opportunityId;
        this.opportunityTitle = opportunityTitle;
        this.organizationName = organizationName;
        this.volunteerId = volunteerId;
        this.volunteerName = volunteerName;
        this.volunteerEmail = volunteerEmail;
        this.organizerId = organizerId;
        this.message = message;
        this.status = "pending";
        this.appliedDate = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getVolunteerId() {
        return volunteerId;
    }

    public void setVolunteerId(String volunteerId) {
        this.volunteerId = volunteerId;
    }

    public String getVolunteerName() {
        return volunteerName;
    }

    public void setVolunteerName(String volunteerName) {
        this.volunteerName = volunteerName;
    }

    public String getVolunteerEmail() {
        return volunteerEmail;
    }

    public void setVolunteerEmail(String volunteerEmail) {
        this.volunteerEmail = volunteerEmail;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getAppliedDate() {
        return appliedDate;
    }

    public void setAppliedDate(Date appliedDate) {
        this.appliedDate = appliedDate;
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