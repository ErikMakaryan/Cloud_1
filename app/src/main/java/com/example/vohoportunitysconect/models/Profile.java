package com.example.vohoportunitysconect.models;

import com.google.firebase.firestore.Exclude;
import java.util.ArrayList;
import java.util.List;

public class Profile {
    @Exclude
    private String id;
    private String name;
    private String email;
    private String phone;
    private String bio;
    private String profileImageUrl;
    private String userType;
    private float rating;
    private int ratingCount;
    private int completedOpportunitiesCount;
    private List<String> interests;
    private List<String> skills;
    private long createdAt;

    public Profile() {
        // Required empty constructor for Firestore
        this.interests = new ArrayList<>();
        this.skills = new ArrayList<>();
        this.rating = 0;
        this.ratingCount = 0;
        this.completedOpportunitiesCount = 0;
        this.createdAt = System.currentTimeMillis();
    }

    public Profile(String name, String email, String userType) {
        this.name = name;
        this.email = email;
        this.userType = userType;
        this.interests = new ArrayList<>();
        this.skills = new ArrayList<>();
        this.rating = 0;
        this.ratingCount = 0;
        this.completedOpportunitiesCount = 0;
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

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
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

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests;
    }

    public List<String> getSkills() {
        return skills;
    }

    public void setSkills(List<String> skills) {
        this.skills = skills;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
} 