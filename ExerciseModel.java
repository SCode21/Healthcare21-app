package com.example.signuploginfirebasee;

public class ExerciseModel {
    private String title;
    private String description;
    private int gifResource;

    public ExerciseModel(String title, String description, int gifResource) {
        this.title = title;
        this.description = description;
        this.gifResource = gifResource;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getGifResource() {
        return gifResource;
    }
}