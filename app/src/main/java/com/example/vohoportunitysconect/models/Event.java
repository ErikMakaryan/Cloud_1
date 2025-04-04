package com.example.vohoportunitysconect.models;

import com.google.firebase.database.Exclude;
import java.util.ArrayList;
import java.util.List;

public class Event {
    @Exclude
    private String id;
    private String title;
    private String description;
    private String location;
    private long startTime;
    private long endTime;
    private String organizerId;
    private List<String> participantIds;
    private int maxParticipants;
    private String category;
    private List<String> tags;
    private String imageUrl;
    private long createdAt;

    public Event() {
        // Required empty constructor for Firestore
        this.participantIds = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    public Event(String title, String description, String location, long startTime, long endTime,
                String organizerId, int maxParticipants, String category) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.startTime = startTime;
        this.endTime = endTime;
        this.organizerId = organizerId;
        this.maxParticipants = maxParticipants;
        this.category = category;
        this.participantIds = new ArrayList<>();
        this.tags = new ArrayList<>();
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public List<String> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<String> participantIds) {
        this.participantIds = participantIds;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
} 