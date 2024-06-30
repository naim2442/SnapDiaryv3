package com.example.snapdiaryv3;

public class DiaryEntry {
    private String description;
    private float moodLevel;
    private String imageUri;
    private String audioUri;
    private long timestamp; // Timestamp in milliseconds
    private Double latitude;
    private Double longitude;
    private String entryId;
    private String audioFilePath; // Added field

    private String location;

    // Default constructor required for calls to DataSnapshot.getValue(DiaryEntry.class)
    public DiaryEntry() {
    }

    // Parameterized constructor
    public DiaryEntry(String description, float moodLevel, String imageUri, String audioUri, long timestamp, String audioFilePath,Double latitude, Double longitude) {
        this.description = description;
        this.moodLevel = moodLevel;
        this.imageUri = imageUri;
        this.audioUri = audioUri;
        this.timestamp = timestamp;
        this.audioFilePath = audioFilePath;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getter for audioFilePath
    public String getAudioFilePath() {
        return audioFilePath;
    }

    // Other getters and setters
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

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public long getTimestamp() {
        return timestamp;
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

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
