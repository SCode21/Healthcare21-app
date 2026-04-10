package com.example.signuploginfirebasee;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DoctorSignupActivity extends AppCompatActivity {
    private EditText doctorName, doctorEmail, doctorPassword,
            doctorSpecialty, doctorContact, doctorDescription;
    private Button doctorSignUpButton;
    private TextView doctorLoginRedirect;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctorsignup);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering...");
        progressDialog.setCancelable(false);

        doctorName = findViewById(R.id.doctor_name);
        doctorEmail = findViewById(R.id.doctor_email);
        doctorPassword = findViewById(R.id.doctor_password);
        doctorSpecialty = findViewById(R.id.doctor_specialty);
        doctorContact = findViewById(R.id.doctor_contact);
        doctorDescription = findViewById(R.id.doctor_description);
        doctorSignUpButton = findViewById(R.id.doctor_signup_button);
        doctorLoginRedirect = findViewById(R.id.doctor_login_redirect);

        doctorSignUpButton.setOnClickListener(v -> {
            String name = doctorName.getText().toString().trim();
            String email = doctorEmail.getText().toString().trim();
            String password = doctorPassword.getText().toString().trim();
            String specialty = doctorSpecialty.getText().toString().trim();
            String contact = doctorContact.getText().toString().trim();
            String description = doctorDescription.getText().toString().trim();

            if (validateInputs(name, email, password, specialty, contact, description)) {
                progressDialog.show();
                registerDoctor(name, email, password, specialty, contact, description);
            }
        });

        doctorLoginRedirect.setOnClickListener(v -> {
            startActivity(new Intent(DoctorSignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    private boolean validateInputs(String name, String email, String password,
                                   String specialty, String contact, String description) {
        if (name.isEmpty()) {
            doctorName.setError("Name is required");
            return false;
        }
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            doctorEmail.setError("Valid email is required");
            return false;
        }
        if (password.isEmpty() || password.length() < 8) {
            doctorPassword.setError("Password must be at least 8 characters");
            return false;
        }
        if (specialty.isEmpty()) {
            doctorSpecialty.setError("Specialty is required");
            return false;
        }
        if (contact.isEmpty() || !contact.matches("\\d{10}")) {
            doctorContact.setError("Valid 10-digit number required");
            return false;
        }
        if (description.isEmpty()) {
            doctorDescription.setError("Description is required");
            return false;
        }
        return true;
    }

    private void registerDoctor(String name, String email, String password,
                                String specialty, String contact, String description) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss();
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Send email verification
                            user.sendEmailVerification()
                                    .addOnCompleteListener(emailTask -> {
                                        if (emailTask.isSuccessful()) {
                                            Toast.makeText(this, "Verification email sent", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            saveDoctorData(user.getUid(), name, email, specialty, contact, description);
                        }
                    } else {
                        Toast.makeText(this,
                                "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveDoctorData(String userId, String name, String email,
                                String specialty, String contact, String description) {
        Map<String, Object> doctor = new HashMap<>();
        doctor.put("name", name);
        doctor.put("email", email);
        doctor.put("specialty", specialty);
        doctor.put("contact", contact);
        doctor.put("description", description);
        doctor.put("role", "doctor");
        doctor.put("approved", false); // Add approval status

        db.collection("doctors").document(userId).set(doctor)
                .addOnSuccessListener(aVoid -> {
                    // Also store in users collection for role lookup
                    Map<String, Object> user = new HashMap<>();
                    user.put("email", email);
                    user.put("role", "doctor");
                    user.put("approved", false);

                    db.collection("users").document(userId).set(user)
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(this, "Registration successful. Please wait for admin approval.", Toast.LENGTH_SHORT).show();
                                auth.signOut(); // Logout the newly registered user
                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save doctor data", Toast.LENGTH_SHORT).show();
                });
    }
}