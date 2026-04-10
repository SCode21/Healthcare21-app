package com.example.signuploginfirebasee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PatientAddActivity extends AppCompatActivity {

    private static final String TAG = "PatientAddActivity";
    private EditText etName, etAge, etPhone, etEmail;
    private Button btnSave;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String doctorId, doctorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_add);
        Log.d(TAG, "Activity created");

        // Initialize views
        initializeViews();

        // Initialize Firebase
        initializeFirebase();

        // Get doctor data from intent
        getDoctorData();

        // Load user's email
        loadUserEmail();

        // Set click listener for save button
        btnSave.setOnClickListener(v -> {
            Log.d(TAG, "Save button clicked");
            savePatient();
        });
    }

    private void initializeViews() {
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        btnSave = findViewById(R.id.btnSave);
        Log.d(TAG, "Views initialized");
    }

    private void initializeFirebase() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        Log.d(TAG, "Firebase initialized");
    }

    private void getDoctorData() {
        doctorId = getIntent().getStringExtra("doctorId");
        doctorName = getIntent().getStringExtra("doctorName");
        Log.d(TAG, "Received doctorId: " + doctorId + ", doctorName: " + doctorName);

        if (doctorId == null) {
            Toast.makeText(this, "Doctor ID is required", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No doctorId provided");
            finish();
            return;
        }

        // If doctorName wasn't passed, fetch it from Firestore
        if (doctorName == null) {
            Log.d(TAG, "Fetching doctor name from Firestore");
            fetchDoctorName();
        }
    }

    private void fetchDoctorName() {
        db.collection("doctors").document(doctorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        doctorName = documentSnapshot.getString("name");
                        Log.d(TAG, "Fetched doctor name: " + doctorName);
                        if (doctorName == null) {
                            doctorName = "Unknown Doctor";
                            Log.w(TAG, "Doctor name was null in document");
                        }
                    } else {
                        doctorName = "Unknown Doctor";
                        Log.w(TAG, "Doctor document doesn't exist");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error fetching doctor name", e);
                    doctorName = "Unknown Doctor";
                });
    }

    private void loadUserEmail() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            etEmail.setText(email);
            etEmail.setEnabled(false);
            Log.d(TAG, "User email loaded: " + email);
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "No authenticated user");
            finish();
        }
    }

    private void savePatient() {
        Log.d(TAG, "Attempting to save patient");

        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        Log.d(TAG, "Form data - Name: " + name + ", Age: " + ageStr +
                ", Phone: " + phone + ", Email: " + email);

        // Validate inputs
        if (name.isEmpty()) {
            etName.setError("Name is required");
            Log.d(TAG, "Validation failed - empty name");
            return;
        }

        if (ageStr.isEmpty()) {
            etAge.setError("Age is required");
            Log.d(TAG, "Validation failed - empty age");
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone is required");
            Log.d(TAG, "Validation failed - empty phone");
            return;
        }

        // Validate age
        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age <= 0 || age > 120) {
                etAge.setError("Age must be 1-120");
                Log.d(TAG, "Validation failed - invalid age range");
                return;
            }
        } catch (NumberFormatException e) {
            etAge.setError("Invalid age");
            Log.d(TAG, "Validation failed - age not a number");
            return;
        }

        // Validate phone
        if (phone.length() < 10) {
            etPhone.setError("Invalid phone number");
            Log.d(TAG, "Validation failed - phone too short");
            return;
        }

        // Create patient data
        Map<String, Object> patient = new HashMap<>();
        patient.put("name", name);
        patient.put("age", ageStr);
        patient.put("phone", phone);
        patient.put("email", email);
        patient.put("doctorId", doctorId);
        patient.put("doctorName", doctorName != null ? doctorName : "Unknown Doctor");
        patient.put("timestamp", System.currentTimeMillis());

        Log.d(TAG, "Attempting to save patient data: " + patient.toString());

        // Save to Firestore
        db.collection("patients").add(patient)
                .addOnSuccessListener(documentReference -> {
                    String patientId = documentReference.getId();
                    Toast.makeText(this, "Patient saved successfully", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Patient saved successfully with ID: " + patientId);

                    // Proceed to appointment confirmation
                    Intent intent = new Intent(this, AppointmentConfirmationActivity.class);
                    intent.putExtra("patientId", patientId);
                    intent.putExtra("doctorId", doctorId);
                    intent.putExtra("doctorName", patient.get("doctorName").toString());
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save patient: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error saving patient", e);
                });
    }
}