package com.example.signuploginfirebasee;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AppointmentConfirmationActivity extends AppCompatActivity {

    private TextView tvDoctorName, tvPatientName, tvAppointmentDetails, tvContact, tvEmail, tvSpecialty;
    private ImageView ivDoctorImage;
    private Button btnSelectDateTime, btnConfirmAppointment;
    private FirebaseFirestore db;

    private String doctorId, doctorName, patientId, patientName, doctorImageUrl, doctorPhone, doctorEmail, doctorSpecialist;
    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;
    private boolean isDateTimeSelected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_confirmation);

        // Initialize views
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvPatientName = findViewById(R.id.tvPatientName);
        tvAppointmentDetails = findViewById(R.id.tvAppointmentDetails);
        ivDoctorImage = findViewById(R.id.ivDoctorImage);
        btnSelectDateTime = findViewById(R.id.btnSelectDateTime);
        btnConfirmAppointment = findViewById(R.id.btnConfirmAppointment);
        tvContact = findViewById(R.id.tvContact);
        tvEmail = findViewById(R.id.tvEmail);
        tvSpecialty = findViewById(R.id.tvSpecialty);

        // Disable Confirm Appointment button initially
        btnConfirmAppointment.setEnabled(false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get data from intent
        doctorId = getIntent().getStringExtra("doctorId");
        patientId = getIntent().getStringExtra("patientId");

        // Fetch doctor info
        fetchDoctorInfo(doctorId);

        // Fetch patient info
        fetchPatientInfo(patientId);

        // Set click listeners
        btnSelectDateTime.setOnClickListener(v -> openDatePicker());
        btnConfirmAppointment.setOnClickListener(v -> confirmAppointment());
    }

    // Fetch doctor info from Firestore using doctorId
    private void fetchDoctorInfo(String doctorId) {
        if (doctorId == null || doctorId.isEmpty()) {
            Toast.makeText(this, "Doctor ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("doctors").document(doctorId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        doctorName = documentSnapshot.getString("name");
                        doctorImageUrl = documentSnapshot.getString("imageUrl");
                        doctorPhone = documentSnapshot.getString("contact");
                        doctorEmail = documentSnapshot.getString("email");
                        doctorSpecialist = documentSnapshot.getString("specialty");

                        // Update UI with fetched data
                        tvDoctorName.setText("Doctor: " + doctorName);
                        tvContact.setText(doctorPhone);
                        tvEmail.setText(doctorEmail);
                        tvSpecialty.setText("Specialty: " + doctorSpecialist);

                        // Load doctor's image using Glide
                        if (doctorImageUrl != null && !doctorImageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(doctorImageUrl)
                                    .placeholder(R.drawable.doctor_placeholder)
                                    .error(R.drawable.ic_doctor1)
                                    .into(ivDoctorImage);
                        } else {
                            Toast.makeText(this, "Doctor image not available", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Doctor not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching doctor info", Toast.LENGTH_SHORT).show();
                    Log.e("AppointmentConfirmation", "Error fetching doctor info", e);
                });
    }

    // Fetch patient info from Firestore using patientId
    private void fetchPatientInfo(String patientId) {
        if (patientId == null || patientId.isEmpty()) {
            Toast.makeText(this, "Patient ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("patients").document(patientId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        patientName = documentSnapshot.getString("name");
                        tvPatientName.setText("Patient: " + patientName);
                    } else {
                        Toast.makeText(this, "Patient not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching patient info", Toast.LENGTH_SHORT).show();
                    Log.e("AppointmentConfirmation", "Error fetching patient info", e);
                });
    }

    // Open DatePicker with validation
    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year, month, dayOfMonth);

            // Check if the selected date is in the past
            if (selectedDate.before(Calendar.getInstance())) {
                Toast.makeText(this, "You cannot select a past date!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if the selected date is a Sunday
            if (selectedDate.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                Toast.makeText(this, "No appointments available on Sundays!", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedYear = year;
            selectedMonth = month;
            selectedDay = dayOfMonth;
            openTimePicker(); // Open time picker after selecting a valid date
        }, selectedYear, selectedMonth, selectedDay);

        // Disable past dates
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

        datePickerDialog.show();
    }

    // Open TimePicker with validation
    private void openTimePicker() {
        Calendar calendar = Calendar.getInstance();
        selectedHour = calendar.get(Calendar.HOUR_OF_DAY);
        selectedMinute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            // Check if the selected date is today
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(selectedYear, selectedMonth, selectedDay);

            Calendar today = Calendar.getInstance();
            boolean isToday = selectedDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    selectedDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    selectedDate.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH);

            // Check if the selected time is in the past for today's date
            if (isToday) {
                Calendar selectedTime = Calendar.getInstance();
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedTime.set(Calendar.MINUTE, minute);

                if (selectedTime.before(today)) {
                    Toast.makeText(this, "You cannot select a past time for today!", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Check if the selected time is within the allowed range (9:00 AM to 10:00 PM)
            if (hourOfDay < 9 || hourOfDay >= 22) {
                Toast.makeText(this, "Appointments are only available between 9:00 AM and 10:00 PM!", Toast.LENGTH_SHORT).show();
                return;
            }

            selectedHour = hourOfDay;
            selectedMinute = minute;
            updateAppointmentDetails(); // Update the UI with selected date and time
            isDateTimeSelected = true; // Mark date and time as selected
            btnConfirmAppointment.setEnabled(true); // Enable Confirm Appointment button
        }, selectedHour, selectedMinute, false);

        timePickerDialog.show();
    }

    // Update the UI with selected appointment details
    private void updateAppointmentDetails() {
        String appointmentDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
        String appointmentTime = String.format("%02d:%02d", selectedHour, selectedMinute);
        tvAppointmentDetails.setText("Date: " + appointmentDate + "\nTime: " + appointmentTime);
    }

    // Confirm appointment and store in Firestore
    private void confirmAppointment() {
        if (!isDateTimeSelected) {
            Toast.makeText(this, "Please select a date and time!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Construct the appointment date and time
        Calendar calendar = Calendar.getInstance();
        calendar.set(selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute);
        long timestamp = calendar.getTimeInMillis();

        // Query Firestore to check if an appointment already exists for the selected doctor, date, and time
        db.collection("appointments")
                .whereEqualTo("doctorId", doctorId)
                .whereEqualTo("date", timestamp)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            // No conflicting appointment found, proceed to confirm
                            createAppointment(timestamp);
                        } else {
                            // Conflicting appointment found
                            Toast.makeText(this, "This time slot is already booked. Please choose another time.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error checking appointment availability", Toast.LENGTH_SHORT).show();
                        Log.e("AppointmentConfirmation", "Error checking appointment availability", task.getException());
                    }
                });
    }

    // Helper method to create the appointment
    private void createAppointment(long timestamp) {
        // Create a map to store appointment details
        Map<String, Object> appointment = new HashMap<>();
        appointment.put("doctorId", doctorId);
        appointment.put("doctorName", doctorName);
        appointment.put("patientId", patientId);
        appointment.put("patientName", patientName);
        appointment.put("appointmentTime", String.format("%02d:%02d", selectedHour, selectedMinute));
        appointment.put("date", timestamp); // Store as timestamp
        appointment.put("status", "Scheduled"); // Default status

        // Store the appointment in Firestore
        db.collection("appointments").add(appointment)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Appointment confirmed!", Toast.LENGTH_SHORT).show();

                    // Redirect to PaymentActivity with all required data
                    Intent intent = new Intent(this, PaymentActivity.class);
                    intent.putExtra("doctorId", doctorId);
                    intent.putExtra("patientId", patientId);
                    intent.putExtra("doctorName", doctorName);
                    intent.putExtra("patientName", patientName);
                    intent.putExtra("appointmentDate", selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay);
                    intent.putExtra("appointmentTime", String.format("%02d:%02d", selectedHour, selectedMinute));
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error confirming appointment", Toast.LENGTH_SHORT).show();
                    Log.e("AppointmentConfirmation", "Error confirming appointment", e);
                });
    }
}