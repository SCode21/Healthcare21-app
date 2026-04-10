package com.example.signuploginfirebasee;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Profile2Activity extends AppCompatActivity {

    private EditText etDoctorName, etDoctorEmail, etDoctorSpecialty, etDoctorContact, etDoctorDescription;
    private Button btnSaveProfile;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String doctorUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile2);

        // Initialize views
        etDoctorName = findViewById(R.id.etDoctorName);
        etDoctorEmail = findViewById(R.id.etDoctorEmail);
        etDoctorSpecialty = findViewById(R.id.etDoctorSpecialty);
        etDoctorContact = findViewById(R.id.etDoctorContact);
        etDoctorDescription = findViewById(R.id.etDoctorDescription);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Get current doctor UID
        if (auth.getCurrentUser() != null) {
            doctorUid = auth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "Doctor not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load doctor's profile data
        loadDoctorProfile();

        // Save profile button click listener
        btnSaveProfile.setOnClickListener(v -> saveDoctorProfile());
    }

    private void loadDoctorProfile() {
        db.collection("doctors").document(doctorUid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etDoctorName.setText(documentSnapshot.getString("name"));
                        etDoctorEmail.setText(documentSnapshot.getString("email"));
                        etDoctorSpecialty.setText(documentSnapshot.getString("specialty"));
                        etDoctorContact.setText(documentSnapshot.getString("contact"));
                        etDoctorDescription.setText(documentSnapshot.getString("description"));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Profile2Activity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveDoctorProfile() {
        String name = etDoctorName.getText().toString().trim();
        String email = etDoctorEmail.getText().toString().trim();
        String specialty = etDoctorSpecialty.getText().toString().trim();
        String contact = etDoctorContact.getText().toString().trim();
        String description = etDoctorDescription.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || specialty.isEmpty() || contact.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a map to update the doctor's profile
        Map<String, Object> doctorData = new HashMap<>();
        doctorData.put("name", name);
        doctorData.put("email", email);
        doctorData.put("specialty", specialty);
        doctorData.put("contact", contact);
        doctorData.put("description", description);

        // Update the Firestore document
        db.collection("doctors").document(doctorUid)
                .set(doctorData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Profile2Activity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Profile2Activity.this, "Error updating profile", Toast.LENGTH_SHORT).show();
                });
    }
}