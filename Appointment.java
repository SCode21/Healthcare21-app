package com.example.signuploginfirebasee;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Appointment {
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String patientName; // Field for patient name
    private String doctorName;
    private String appointmentTime;
    private long date;  // Store the date as a timestamp
    private String status;

    // Default constructor required for Firebase
    public Appointment() {}

    // Constructor with all fields (including patientName)
    public Appointment(String patientId, String doctorId, String patientName, String doctorName, String appointmentTime, long date, String status) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentTime = appointmentTime;
        this.date = date;
        this.status = status;
    }

    // Constructor with appointmentId for Firestore document ID (including patientName)
    public Appointment(String appointmentId, String patientId, String doctorId, String patientName, String doctorName, String appointmentTime, long date, String status) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.patientName = patientName;
        this.doctorName = doctorName;
        this.appointmentTime = appointmentTime;
        this.date = date;
        this.status = status;
    }

    // Getters and Setters for all fields
    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public long getDate() {  // Keep this as long, not int
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Method to format the date into a readable string
    public String getFormattedDate() {
        Date appointmentDate = new Date(this.date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(appointmentDate);
    }
}