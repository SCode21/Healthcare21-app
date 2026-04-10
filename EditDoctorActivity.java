package com.example.signuploginfirebasee;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
public class EditDoctorActivity extends AppCompatActivity {

    private TextInputEditText etName, etSpecialty, etContact, etDescription;
    private Button btnSaveChanges;
    private FirebaseFirestore db;
    private String doctorId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_doctor);

        // Initialize views
        etName = findViewById(R.id.etDoctorName);
        etSpecialty = findViewById(R.id.etDoctorSpecialty);
        etContact = findViewById(R.id.etDoctorContact);
        etDescription = findViewById(R.id.etDoctorDescription);
        btnSaveChanges = findViewById(R.id.btnSaveChanges);

        db = FirebaseFirestore.getInstance();
        doctorId = getIntent().getStringExtra("doctorId");

        // Check if doctorId is valid
        if (doctorId == null || doctorId.isEmpty()) {
            Toast.makeText(this, "Doctor ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadDoctorDetails();

        btnSaveChanges.setOnClickListener(v -> saveDoctorChanges());
    }

    private void loadDoctorDetails() {
        db.collection("doctors").document(doctorId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etName.setText(documentSnapshot.getString("name"));
                        etSpecialty.setText(documentSnapshot.getString("specialty"));
                        etContact.setText(documentSnapshot.getString("contact"));
                        etDescription.setText(documentSnapshot.getString("description"));
                    } else {
                        Toast.makeText(this, "Doctor not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading doctor: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("EditDoctor", "Error loading doctor", e);
                    finish();
                });
    }

    private void saveDoctorChanges() {
        String name = etName.getText().toString().trim();
        String specialty = etSpecialty.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(specialty)) {
            etSpecialty.setError("Specialty is required");
            etSpecialty.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(contact) || contact.length() < 10) {
            etContact.setError("Enter valid 10-digit contact");
            etContact.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(description)) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return;
        }

        // Create update map
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("specialty", specialty);
        updates.put("contact", contact);
        updates.put("description", description);

        db.collection("doctors").document(doctorId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Doctor updated successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Notify parent activity of success
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("EditDoctor", "Update error", e);
                });
    }
}