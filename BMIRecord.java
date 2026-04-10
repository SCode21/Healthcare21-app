package com.example.signuploginfirebasee;

public class BMIRecord {
    private String id;
    private float bmi;
    private String category;
    private String idealWeight;
    private String timestamp;

    // Empty constructor needed for Firestore
    public BMIRecord() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public float getBmi() { return bmi; }
    public void setBmi(float bmi) { this.bmi = bmi; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getIdealWeight() { return idealWeight; }
    public void setIdealWeight(String idealWeight) { this.idealWeight = idealWeight; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}