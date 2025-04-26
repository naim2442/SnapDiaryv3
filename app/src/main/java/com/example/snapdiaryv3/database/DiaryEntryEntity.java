package com.example.snapdiaryv3.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

/**
 * Entity class for Room database to store diary entries locally
 */
@Entity(tableName = "diary_entries")
public class DiaryEntryEntity {
    
    @PrimaryKey
    @NonNull
    private String entryId;
    
    private String description;
    private float moodLevel;
    private String imageUri;
    private String audioUri;
    private String audioFilePath;
    private long timestamp;
    private Double latitude;
    private Double longitude;
    private String location;
    private boolean isSynced; // Flag to track if entry is synced with Firebase

    public DiaryEntryEntity(@NonNull String entryId, String description, float moodLevel, 
                           String imageUri, String audioUri, String audioFilePath, 
                           long timestamp, Double latitude, Double longitude, String location) {
        this.entryId = entryId;
        this.description = description;
        this.moodLevel = moodLevel;
        this.imageUri = imageUri;
        this.audioUri = audioUri;
        this.audioFilePath = audioFilePath;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
        this.isSynced = false;
    }

    @NonNull
    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(@NonNull String entryId) {
        this.entryId = entryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getMoodLevel() {
        return moodLevel;
    }

    public void setMoodLevel(float moodLevel) {
        this.moodLevel = moodLevel;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getAudioUri() {
        return audioUri;
    }

    public void setAudioUri(String audioUri) {
        this.audioUri = audioUri;
    }

    public String getAudioFilePath() {
        return audioFilePath;
    }

    public void setAudioFilePath(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }
}
