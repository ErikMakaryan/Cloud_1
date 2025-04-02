package com.example.vohoportunitysconect.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

public class User {
    @Exclude
    private String id;
    private String name;
    private String email;
    private String phone;
    private String bio;
    private String profileImageUrl;
    private String phoneNumber;
    private List<String> interests;
    private List<String> skills;
    private List<String> completedOpportunities;
    private List<String> savedOpportunities;
    private int volunteerHours;
    private String location;
    private boolean isVerified;
    private UserType userType;
    private float rating;
    private int ratingCount;
    private int completedOpportunitiesCount;
    private long createdAt;
    private String organizationName;
    private String organizationDescription;
    private String organizationWebsite;

    // Required empty constructor for Firestore
    public User() {
        this.interests = new ArrayList<>();
        this.skills = new ArrayList<>();
        this.completedOpportunities = new ArrayList<>();
        this.savedOpportunities = new ArrayList<>();
        this.volunteerHours = 0;
        this.isVerified = false;
        this.rating = 0;
        this.ratingCount = 0;
        this.completedOpportunitiesCount = 0;
        this.createdAt = System.currentTimeMillis();
    }

    public User(String name, String email, String userType) {
        this.name = name;
        this.email = email;
        this.userType = UserType.valueOf(userType);
        this.createdAt = System.currentTimeMillis();
        this.interests = new ArrayList<>();
        this.skills = new ArrayList<>();
        this.completedOpportunities = new ArrayList<>();
        this.savedOpportunities = new ArrayList<>();
        this.volunteerHours = 0;
        this.isVerified = false;
        this.completedOpportunitiesCount = 0;
    }

    // Getters and setters
    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public void addInterest(String interest) {
        if (!interests.contains(interest)) {
            interests.add(interest);
        }
    }

    public void removeInterest(String interest) {
        interests.remove(interest);
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public void addSkill(String skill) {
        if (!skills.contains(skill)) {
            skills.add(skill);
        }
    }

    public void removeSkill(String skill) {
        skills.remove(skill);
    }

    public List<String> getCompletedOpportunities() {
        return completedOpportunities;
    }

    public void setCompletedOpportunities(List<String> completedOpportunities) {
        this.completedOpportunities = completedOpportunities;
    }

    public void addCompletedOpportunity(String opportunityId) {
        if (!completedOpportunities.contains(opportunityId)) {
            completedOpportunities.add(opportunityId);
        }
    }

    public List<String> getSavedOpportunities() {
        return savedOpportunities;
    }

    public void setSavedOpportunities(List<String> savedOpportunities) {
        this.savedOpportunities = savedOpportunities;
    }

    public void addSavedOpportunity(String opportunityId) {
        if (!savedOpportunities.contains(opportunityId)) {
            savedOpportunities.add(opportunityId);
        }
    }

    public void removeSavedOpportunity(String opportunityId) {
        savedOpportunities.remove(opportunityId);
    }

    public int getVolunteerHours() {
        return volunteerHours;
    }

    public void setVolunteerHours(int volunteerHours) {
        this.volunteerHours = volunteerHours;
    }

    public void addVolunteerHours(int hours) {
        this.volunteerHours += hours;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public int getCompletedOpportunitiesCount() {
        return completedOpportunitiesCount;
    }

    public void setCompletedOpportunitiesCount(int completedOpportunitiesCount) {
        this.completedOpportunitiesCount = completedOpportunitiesCount;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getOrganizationDescription() {
        return organizationDescription;
    }

    public void setOrganizationDescription(String organizationDescription) {
        this.organizationDescription = organizationDescription;
    }

    public String getOrganizationWebsite() {
        return organizationWebsite;
    }

    public void setOrganizationWebsite(String organizationWebsite) {
        this.organizationWebsite = organizationWebsite;
    }

    @Exclude
    public boolean isOrganization() {
        return userType == UserType.ORGANIZATION;
    }

    @Exclude
    public boolean isVolunteer() {
        return userType == UserType.VOLUNTEER;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null ? id.equals(user.id) : user.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
} 