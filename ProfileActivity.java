package com.example.signuploginfirebasee;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";
    private ImageView imgProfile;
    private EditText edtFullName, edtDOB, edtGender, edtBloodGroup, edtContact, edtEmail, edtAddress, edtRole;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        imgProfile = findViewById(R.id.imgProfile);
        edtFullName = findViewById(R.id.edtFullName);
        edtDOB = findViewById(R.id.edtDOB);
        edtGender = findViewById(R.id.edtGender);
        edtBloodGroup = findViewById(R.id.edtBloodGroup);
        edtContact = findViewById(R.id.edtContact);
        edtEmail = findViewById(R.id.edtEmail);
        edtAddress = findViewById(R.id.edtAddress);
        edtRole = findViewById(R.id.edtRole);

        Button btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        Button btnSaveProfile = findViewById(R.id.btnSaveProfile);

        if (user != null) {
            edtEmail.setText(user.getEmail());
            loadProfileData();
        } else {
            Log.e(TAG, "User is null, not logged in!");
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show();
        }

        btnUploadPhoto.setOnClickListener(v -> openGallery());
        btnSaveProfile.setOnClickListener(v -> saveProfileData());

        // Move cursor to DOB when "Enter" is pressed in Full Name
        edtFullName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                edtDOB.requestFocus();
                return true;
            }
            return false;
        });

        // Open Date Picker when DOB field is clicked
        edtDOB.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                    edtDOB.setText(selectedDate);
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void loadProfileData() {
        if (user == null) return;
        DocumentReference userDoc = db.collection("users").document(user.getUid());
        userDoc.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                edtFullName.setText(documentSnapshot.getString("FullName"));
                edtDOB.setText(documentSnapshot.getString("DOB"));
                edtGender.setText(documentSnapshot.getString("Gender"));
                edtBloodGroup.setText(documentSnapshot.getString("BloodGroup"));
                edtContact.setText(documentSnapshot.getString("Contact"));
                edtAddress.setText(documentSnapshot.getString("Address"));
                String role = documentSnapshot.getString("role");
                if (role != null) {
                    edtRole.setText(role);
                }
                String imageUrl = documentSnapshot.getString("imageUrl");
                if (imageUrl != null) {
                    Glide.with(this).load(imageUrl).into(imgProfile);
                }
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to load profile", e);
            Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
        });
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            imgProfile.setImageURI(imageUri);
            uploadImage(imageUri);
        }
    }

    private void uploadImage(Uri imageUri) {
        if (user == null) return;

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("profile_images/" + user.getUid());
        storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();
                db.collection("users").document(user.getUid())
                        .update("imageUrl", imageUrl)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Image uploaded", Toast.LENGTH_SHORT).show();
                            Glide.with(this).load(imageUrl).into(imgProfile);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        });
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveProfileData() {
        if (user == null) return;

        String fullName = edtFullName.getText().toString().trim();
        String dob = edtDOB.getText().toString().trim();
        String gender = edtGender.getText().toString().trim();
        String bloodGroup = edtBloodGroup.getText().toString().trim();
        String contact = edtContact.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String role = edtRole.getText().toString().trim();

        if (fullName.isEmpty() || dob.isEmpty() || gender.isEmpty() || bloodGroup.isEmpty() || contact.isEmpty() || address.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference userDoc = db.collection("users").document(user.getUid());
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("FullName", fullName);
        userProfile.put("DOB", dob);
        userProfile.put("Gender", gender);
        userProfile.put("BloodGroup", bloodGroup);
        userProfile.put("Contact", contact);
        userProfile.put("Address", address);
        userProfile.put("role", role);

        userDoc.set(userProfile, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
                    navigateToHome();
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
}