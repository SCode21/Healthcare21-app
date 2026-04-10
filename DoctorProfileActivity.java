package com.example.signuploginfirebasee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class DoctorProfileActivity extends AppCompatActivity {

    private TextView tvDoctorName, tvSpecialty, tvEmail, tvContact;
    private EditText etDescription;
    private Button btnAddPatientInfo, btnBookAppointment;
    private ImageView ivDoctorImage; // Add ImageView for doctor's image
    private FirebaseFirestore db;
    private String doctorId;
    private boolean isPatientInfoAdded = false;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private boolean isDoctor = false; // Flag to check if the user is a doctor

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_profile);

        // Initialize views
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvSpecialty = findViewById(R.id.tvSpecialty);
        tvEmail = findViewById(R.id.tvEmail);
        tvContact = findViewById(R.id.tvContact);
        etDescription = findViewById(R.id.etDescription);
        btnAddPatientInfo = findViewById(R.id.btnAddPatientInfo);
        btnBookAppointment = findViewById(R.id.btnBookAppointment);
        ivDoctorImage = findViewById(R.id.ivDoctorImage); // Initialize ImageView for doctor's image

        // Initialize Firestore and Firebase Auth
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Get doctor ID from intent
        doctorId = getIntent().getStringExtra("doctorId");
        if (doctorId == null) {
            Toast.makeText(this, "Doctor ID missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Check if the current user is a doctor
        checkUserRole();

        // Fetch and display doctor details
        fetchDoctorDetails();

        // Handle "Add Patient Info" button click
        btnAddPatientInfo.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorProfileActivity.this, PatientAddActivity.class);
            intent.putExtra("doctorId", doctorId);
            startActivityForResult(intent, 100); // Start PatientAddActivity for result
        });

        // Handle "Book Appointment" button click
        btnBookAppointment.setOnClickListener(v -> {
            if (isPatientInfoAdded) {
                // Redirect to AppointmentConfirmationActivity
                Intent intent = new Intent(DoctorProfileActivity.this, AppointmentConfirmationActivity.class);
                intent.putExtra("doctorId", doctorId);
                intent.putExtra("doctorName", tvDoctorName.getText().toString()); // Pass the doctor's name
                startActivity(intent);
            } else {
                Toast.makeText(this, "Please add patient information first!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Check if the current user is a doctor or a patient
    private void checkUserRole() {
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");
                            if (role != null && role.equals("doctor")) {
                                isDoctor = true;
                                enableEditing(); // Allow editing for doctors
                            } else {
                                disableEditing(); // Disable editing for patients
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error checking user role: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("DoctorProfileActivity", "Error checking user role", e);
                    });
        }
    }

    // Enable editing for doctors
    private void enableEditing() {
        etDescription.setEnabled(true); // Allow doctors to edit their description
    }

    // Disable editing for patients
    private void disableEditing() {
        etDescription.setEnabled(false); // Disable editing for patients
        btnAddPatientInfo.setVisibility(View.VISIBLE); // Show "Add Patient Info" button for patients
        btnBookAppointment.setVisibility(View.VISIBLE); // Show "Book Appointment" button for patients
    }

    // Fetch doctor details from Firestore
    private void fetchDoctorDetails() {
        db.collection("doctors").document(doctorId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        // Set doctor details in the UI
                        String name = doc.getString("name");
                        String specialty = doc.getString("specialty");
                        String email = doc.getString("email");
                        String contact = doc.getString("contact");
                        String description = doc.getString("description");
                        String imageUrl = doc.getString("imageUrl"); // Fetch image URL

                        if (name != null) {
                            tvDoctorName.setText(name);
                        } else {
                            tvDoctorName.setText("Doctor Name Not Available");
                        }

                        if (specialty != null) {
                            tvSpecialty.setText(specialty);
                        } else {
                            tvSpecialty.setText("Specialty Not Available");
                        }

                        if (email != null) {
                            tvEmail.setText(email);
                        } else {
                            tvEmail.setText("Email Not Available");
                        }

                        if (contact != null) {
                            tvContact.setText(contact);
                        } else {
                            tvContact.setText("Contact Not Available");
                        }

                        if (description != null) {
                            etDescription.setText(description);
                        } else {
                            etDescription.setText("Description Not Available");
                        }

                        // Load doctor's image using Glide
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_doctor1) // Placeholder image
                                    .error(R.drawable.ic_doctor1) // Error image
                                    .into(ivDoctorImage);
                        } else {
                            ivDoctorImage.setImageResource(R.drawable.ic_doctor1); // Default image
                        }
                    } else {
                        Toast.makeText(this, "Doctor data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching doctor details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("DoctorProfileActivity", "Error fetching doctor details", e);
                });
    }

    // Handle result from PatientAddActivity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            isPatientInfoAdded = true; // Set flag to true when patient info is added
            Toast.makeText(this, "Patient Info Added Successfully!", Toast.LENGTH_SHORT).show();
        }
    }
}