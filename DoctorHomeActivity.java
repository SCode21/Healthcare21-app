package com.example.signuploginfirebasee;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

public class DoctorHomeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView doctorNameText, doctorSpecialization, tvContact, doctorEmail, doctorDescription;
    private ImageView notificationIcon, doctorImage;
    private Button btnEditProfile, btnLogout;
    private int pendingAppointmentsCount = 0;

    // For Firestore real-time listener
    private ListenerRegistration appointmentListener;
    private static final String CHANNEL_ID = "doctor_appointments_channel";
    private static final int NOTIFICATION_ID = 100;

    private String doctorUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_home);

        // Check notification permissions (Android 13+)
        checkNotificationPermission();

        // Create notification channel (required for Android 8.0+)
        createNotificationChannel();

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

        // Initialize views
        doctorNameText = findViewById(R.id.doctor_name_text);
        notificationIcon = findViewById(R.id.notification_icon);
        doctorImage = findViewById(R.id.doctor_image);
        doctorSpecialization = findViewById(R.id.doctor_specialization);
        tvContact = findViewById(R.id.tvContact);
        doctorEmail = findViewById(R.id.doctor_email);
        doctorDescription = findViewById(R.id.doctor_description);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnLogout = findViewById(R.id.btnLogout);

        // Set click listeners
        notificationIcon.setOnClickListener(v -> navigateTo(ViewAppointmentsActivity.class));
        btnEditProfile.setOnClickListener(v -> openProfileEditor());
        btnLogout.setOnClickListener(v -> logoutDoctor());

        // Load the doctor's details including image
        loadDoctorDetails();

        // Start listening for new appointments
        listenForNewAppointments();

        // Setup Bottom Navigation
        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(this::onNavigationItemSelected);
    }

    private void openProfileEditor() {
        Intent intent = new Intent(DoctorHomeActivity.this, Profile2Activity.class);
        intent.putExtra("doctorId", doctorUid);
        startActivity(intent);
    }

    private void logoutDoctor() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    auth.signOut();
                    Intent intent = new Intent(DoctorHomeActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void loadDoctorDetails() {
        db.collection("doctors").document(doctorUid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("name");
                        String specialty = documentSnapshot.getString("specialty");
                        String contact = documentSnapshot.getString("contact");
                        String email = documentSnapshot.getString("email");
                        String description = documentSnapshot.getString("description");
                        String imageUrl = documentSnapshot.getString("imageUrl");

                        doctorNameText.setText(fullName != null ? fullName : "Doctor");
                        doctorSpecialization.setText(specialty != null ? specialty : "Specialty not specified");
                        tvContact.setText(contact != null ? contact : "Contact not available");
                        doctorEmail.setText(email != null ? email : "Email not available");
                        doctorDescription.setText(description != null ? description : "No description available");

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.ic_doctor)
                                    .error(R.drawable.ic_doctor)
                                    .listener(new RequestListener<Drawable>() {
                                        @Override
                                        public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                                    Target<Drawable> target, boolean isFirstResource) {
                                            Log.e("GLIDE_ERROR", "Image load failed: " + e);
                                            return false;
                                        }

                                        @Override
                                        public boolean onResourceReady(Drawable resource, Object model,
                                                                       Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                            Log.d("GLIDE_SUCCESS", "Image loaded successfully");
                                            return false;
                                        }
                                    })
                                    .circleCrop()
                                    .into(doctorImage);
                        } else {
                            doctorImage.setImageResource(R.drawable.ic_doctor);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading doctor details", Toast.LENGTH_SHORT).show();
                    Log.e("FIRESTORE_ERROR", "Error: " + e.getMessage());
                });
    }

    private void listenForNewAppointments() {
        appointmentListener = db.collection("appointments")
                .whereEqualTo("doctorId", doctorUid)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("DoctorHomeActivity", "Listen failed", e);
                        return;
                    }
                    if (snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        if (dc.getType() == DocumentChange.Type.ADDED) {
                            pendingAppointmentsCount++;
                            updateNotificationIcon();
                            showNewAppointmentNotification(
                                    dc.getDocument().getString("patientName"),
                                    dc.getDocument().getString("appointmentDate"),
                                    dc.getDocument().getString("appointmentTime")
                            );
                        }
                    }
                });
    }

    private void updateNotificationIcon() {
        if (pendingAppointmentsCount > 0) {
            notificationIcon.setVisibility(View.VISIBLE);
            notificationIcon.setImageResource(R.drawable.ic_notification_red);
        } else {
            notificationIcon.setVisibility(View.GONE);
        }
    }

    private void showNewAppointmentNotification(String patientName, String appointmentDate, String appointmentTime) {
        Intent intent = new Intent(this, ViewAppointmentsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("New Appointment Requested")
                .setContentText("Patient: " + patientName + "\nTime: " + appointmentTime)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Doctor Appointments";
            String channelDesc = "Notifications for new appointments";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            channel.setDescription(channelDesc);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            return true;
        } else if (id == R.id.nav_appointments) {
            navigateTo(ViewAppointmentsActivity.class);
            return true;
        } else if (id == R.id.nav_profile) {
            openProfileEditor();
            return true;
        }
        return false;
    }

    private void navigateTo(Class<?> activityClass) {
        Intent intent = new Intent(DoctorHomeActivity.this, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (appointmentListener != null) {
            appointmentListener.remove();
        }
    }
}