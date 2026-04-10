package com.example.signuploginfirebasee;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.firebase.firestore.PropertyName;

@IgnoreExtraProperties
public class Doctor {
    @DocumentId
    private String id;
    private String name;
    private String specialty;
    private String email;
    private String contact;
    private String imageUrl;
    private Object rating; // Changed to Object to handle both String and Double

    public Doctor() {
        // Default constructor required for Firestore
    }

    public Doctor(String id, String name, String specialty, String email,
                  String contact, String imageUrl, double rating) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.email = email;
        this.contact = contact;
        this.imageUrl = imageUrl;
        this.rating = rating;
    }

    // Getters
    @Exclude
    public String getId() { return id; }
    public String getName() { return name; }
    public String getSpecialty() { return specialty; }
    public String getEmail() { return email; }
    public String getContact() { return contact; }
    public String getImageUrl() { return imageUrl; }

    // Special getter for rating that handles all cases
    @Exclude
    public double getRating() {
        if (rating instanceof String) {
            try {
                return Double.parseDouble((String) rating);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        } else if (rating instanceof Double) {
            return (Double) rating;
        } else if (rating instanceof Long) {
            return ((Long) rating).doubleValue();
        }
        return 0.0;
    }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public void setEmail(String email) { this.email = email; }
    public void setContact(String contact) { this.contact = contact; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // Special setter for Firestore
    @PropertyName("rating")
    public void setRating(Object rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", specialty='" + specialty + '\'' +
                ", rating=" + getRating() + // Use getRating() to ensure double
                '}';
    }
}