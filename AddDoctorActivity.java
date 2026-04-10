package com.example.signuploginfirebasee;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class AddDoctorActivity extends AppCompatActivity {

    private TextInputEditText etName, etSpecialty, etEmail, etPassword, etContact, etDescription;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_doctor);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        etName = findViewById(R.id.etName);
        etSpecialty = findViewById(R.id.etSpecialty);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etContact = findViewById(R.id.etContact);
        etDescription = findViewById(R.id.etDescription);
        Button btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> createDoctorAccount());
    }

    private void createDoctorAccount() {
        String name = Objects.requireNonNull(etName.getText()).toString().trim();
        String specialty = Objects.requireNonNull(etSpecialty.getText()).toString().trim();
        String email = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(etPassword.getText()).toString().trim();
        String contact = Objects.requireNonNull(etContact.getText()).toString().trim();
        String description = Objects.requireNonNull(etDescription.getText()).toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(specialty)) {
            etSpecialty.setError("Specialty is required");
            return;
        }

        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email is required");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be ≥6 characters");
            return;
        }

        if (TextUtils.isEmpty(contact) || contact.length() < 10) {
            etContact.setError("Valid contact number is required");
            return;
        }

        if (TextUtils.isEmpty(description)) {
            etDescription.setError("Description is required");
            return;
        }

        // Create Firebase user
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String uid = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                        // 1. Create basic user document in 'users' collection
                        Map<String, Object> user = new HashMap<>();
                        user.put("email", email);
                        user.put("role", "doctor");
                        user.put("uid", uid);

                        db.collection("users").document(uid)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    // 2. Create full doctor document in 'doctors' collection
                                    Map<String, Object> doctor = new HashMap<>();
                                    doctor.put("name", name);
                                    doctor.put("specialty", specialty);
                                    doctor.put("email", email);
                                    doctor.put("contact", contact);
                                    doctor.put("description", description);
                                    doctor.put("approved", false);
                                    doctor.put("uid", uid);
                                    doctor.put("imageUrl", "");

                                    db.collection("doctors").document(uid)
                                            .set(doctor)
                                            .addOnSuccessListener(aVoid1 -> {
                                                // Update auth profile with display name
                                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                                        .setDisplayName("Dr. " + name)
                                                        .build();

                                                mAuth.getCurrentUser().updateProfile(profileUpdates)
                                                        .addOnCompleteListener(profileTask -> {
                                                            Toast.makeText(AddDoctorActivity.this,
                                                                    "Doctor registered successfully!",
                                                                    Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        });
                                            })
                                            .addOnFailureListener(e -> {
                                                // Rollback: Delete user document if doctor creation fails
                                                db.collection("users").document(uid).delete();
                                                mAuth.getCurrentUser().delete();
                                                Toast.makeText(AddDoctorActivity.this,
                                                        "Error saving doctor details: " + e.getMessage(),
                                                        Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    // Rollback: Delete auth user if user document creation fails
                                    mAuth.getCurrentUser().delete();
                                    Toast.makeText(AddDoctorActivity.this,
                                            "Error creating user record: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(AddDoctorActivity.this,
                                "Registration failed: " + Objects.requireNonNull(task.getException()).getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}