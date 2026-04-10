package com.example.signuploginfirebasee;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AdminRegisterActivity extends AppCompatActivity {
    private EditText adminRegEmail, adminRegPassword, adminRegName, adminRegKey;
    private Button adminRegisterButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private final String SECRET_ADMIN_KEY = "ADMIN123"; // Change this in production

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        adminRegEmail = findViewById(R.id.adminRegEmail);
        adminRegPassword = findViewById(R.id.adminRegPassword);
        adminRegName = findViewById(R.id.adminRegName);
        adminRegKey = findViewById(R.id.adminRegKey);
        adminRegisterButton = findViewById(R.id.adminRegisterButton);

        adminRegisterButton.setOnClickListener(v -> {
            String email = adminRegEmail.getText().toString().trim();
            String password = adminRegPassword.getText().toString().trim();
            String name = adminRegName.getText().toString().trim();
            String key = adminRegKey.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty() || name.isEmpty() || key.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!key.equals(SECRET_ADMIN_KEY)) {
                adminRegKey.setError("Invalid admin key");
                return;
            }

            registerAdmin(email, password, name);
        });
    }

    private void registerAdmin(String email, String password, String name) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
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

                            String uid = user.getUid();
                            Map<String, Object> admin = new HashMap<>();
                            admin.put("email", email);
                            admin.put("name", name);
                            admin.put("role", "admin");

                            db.collection("admins").document(uid).set(admin)
                                    .addOnSuccessListener(aVoid -> {
                                        // Also store in users collection for role lookup
                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("email", email);
                                        userData.put("role", "admin");
                                        db.collection("users").document(uid).set(userData)
                                                .addOnSuccessListener(aVoid1 -> {
                                                    Toast.makeText(this, "Admin registration successful. Please login.", Toast.LENGTH_SHORT).show();
                                                    auth.signOut(); // Logout the newly registered admin
                                                    startActivity(new Intent(this, LoginActivity.class));
                                                    finish();
                                                });
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to save admin data", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}