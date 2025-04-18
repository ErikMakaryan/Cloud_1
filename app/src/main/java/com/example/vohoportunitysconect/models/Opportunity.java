package com.example.vohoportunitysconect.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import java.util.Date;
import java.util.List;

@IgnoreExtraProperties
public class Opportunity {
    public enum Difficulty {
        EASY("easy"),
        MEDIUM("medium"),
        HARD("hard");

        private final String value;

        Difficulty(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Difficulty fromValue(String value) {
            for (Difficulty difficulty : Difficulty.values()) {
                if (difficulty.value.equals(value)) {
                    return difficulty;
                }
            }
            return MEDIUM; // Default to MEDIUM if unknown value
        }
    }

    @Exclude
    private String id;
    private String title;
    private String description;
    private String organizationId;
    private String organizationName;
    private String location;
    private String category;
    private String skills;
    private Date deadline;
    private String imageUrl;
    private List<String> categories;
    private boolean isRemote;
    private int requiredHours;
    private boolean featured;
    private boolean urgent;
    private Difficulty difficulty;
    private int maxVolunteers;
    private int currentVolunteers;
    private long createdAt;
    private boolean isActive;
    private List<String> requiredCertifications;
    private String compensationType; // e.g., "certificate", "stipend", "none"
    private double compensationAmount;
    private String organization;
    private Date updatedAt;
    private String date;
    private String startDate;
    private String endDate;
    private int maxParticipants;
    private String requirements;
    private String status;

    // Empty constructor for Firebase
    public Opportunity() {
        this.featured = false;
        this.isActive = true;
        this.currentVolunteers = 0;
        this.createdAt = System.currentTimeMillis();
    }

    public Opportunity(String id, String title, String description, String organizationId, 
                      String organizationName, String location, String category, String skills) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.location = location;
        this.category = category;
        this.skills = skills;
        this.featured = false;
        this.isActive = true;
        this.currentVolunteers = 0;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    public boolean isRemote() {
        return isRemote;
    }

    public void setRemote(boolean remote) {
        isRemote = remote;
    }

    public int getRequiredHours() {
        return requiredHours;
    }

    public void setRequiredHours(int requiredHours) {
        this.requiredHours = requiredHours;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public void setUrgent(boolean urgent) {
        this.urgent = urgent;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public int getMaxVolunteers() {
        return maxVolunteers;
    }

    public void setMaxVolunteers(int maxVolunteers) {
        this.maxVolunteers = maxVolunteers;
    }

    public int getCurrentVolunteers() {
        return currentVolunteers;
    }

    public void setCurrentVolunteers(int currentVolunteers) {
        this.currentVolunteers = currentVolunteers;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public List<String> getRequiredCertifications() {
        return requiredCertifications;
    }

    public void setRequiredCertifications(List<String> requiredCertifications) {
        this.requiredCertifications = requiredCertifications;
    }

    public String getCompensationType() {
        return compensationType;
    }

    public void setCompensationType(String compensationType) {
        this.compensationType = compensationType;
    }

    public double getCompensationAmount() {
        return compensationAmount;
    }

    public void setCompensationAmount(double compensationAmount) {
        this.compensationAmount = compensationAmount;
    }

    @Exclude
    public boolean isFull() {
        return currentVolunteers >= maxVolunteers;
    }

    @Exclude
    public boolean isExpired() {
        return deadline != null && deadline.before(new Date());
    }

    @Exclude
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 