package com.example.vohoportunitysconect.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Certificate {
    @Exclude
    private String id;
    private String name;
    private String fileUrl;
    private String filePath;
    private long uploadDate;
    private String userId;

    // Default constructor for Firebase
    public Certificate() {
    }

    // Constructor for creating new certificates
    public Certificate(String id, String name, String fileUrl, long uploadDate, String userId) {
        this.id = id;
        this.name = name;
        this.fileUrl = fileUrl;
        this.uploadDate = uploadDate;
        this.userId = userId;
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

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(long uploadDate) {
        this.uploadDate = uploadDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
} 