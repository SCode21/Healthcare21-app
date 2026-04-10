package com.example.signuploginfirebasee;

import java.util.List;

public class Patient {
    private String id;
    private String name;
    private String age;
    private String phone;
    private String email; // Add email field
    private List<String> paymentIds;

    public Patient() {}

    public String getId() { return id; }
    public String getName() { return name; }
    public String getAge() { return age; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; } // Add getter for email
    public List<String> getPaymentIds() { return paymentIds; }

    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setAge(String age) { this.age = age; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; } // Add setter for email
    public void setPaymentIds(List<String> paymentIds) { this.paymentIds = paymentIds; }

    // Add these methods to match the AdminDashboardActivity requirements
    public String getFullName() {
        return name; // Assuming 'name' is the full name
    }

    public String getContact() {
        return phone; // Assuming 'phone' is the contact number
    }
}