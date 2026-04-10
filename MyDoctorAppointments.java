package com.example.signuploginfirebasee;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MyDoctorAppointments extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PatientAppointmentAdapter patientAppointmentAdapter;
    private List<Appointment> appointmentsList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_doctor_appointments);

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize data structures
        appointmentsList = new ArrayList<>();
        patientAppointmentAdapter = new PatientAppointmentAdapter(appointmentsList, this);
        recyclerView.setAdapter(patientAppointmentAdapter);

        // Add diagnostic checks
        recyclerView.postDelayed(this::runDiagnostics, 1000);

        // Fetch appointments
        fetchPatientAppointments();
    }

    private void runDiagnostics() {
        // Check if adapter is set
        if (recyclerView.getAdapter() == null) {
            Log.e("DIAGNOSTICS", "Adapter not attached to RecyclerView");
            return;
        }

        // Check item count
        Log.d("DIAGNOSTICS", "Adapter item count: " + recyclerView.getAdapter().getItemCount());

        // Force refresh
        recyclerView.invalidate();
        recyclerView.requestLayout();
        Log.d("DIAGNOSTICS", "Forced RecyclerView refresh");
    }

    private void fetchPatientAppointments() {
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = auth.getCurrentUser().getEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("patients")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        appointmentsList.clear();
                        for (QueryDocumentSnapshot patientDocument : task.getResult()) {
                            String patientId = patientDocument.getId();
                            fetchAppointments(patientId);
                        }
                    } else {
                        Toast.makeText(this, "No patients found", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchAppointments(String patientId) {
        db.collection("appointments")
                .whereEqualTo("patientId", patientId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Parse appointment data
                            String appointmentId = document.getId();
                            String status = document.getString("status");
                            Log.d("APPT_STATUS", "Status: " + status);

                            // Create appointment object
                            Appointment appointment = new Appointment(
                                    appointmentId,
                                    document.getString("patientId"),
                                    document.getString("doctorId"),
                                    document.getString("patientName"),
                                    document.getString("doctorName"),
                                    document.getString("appointmentTime"),
                                    document.getLong("date"),
                                    status
                            );

                            appointmentsList.add(appointment);
                        }

                        // Sort and update UI
                        Collections.sort(appointmentsList, (a1, a2) -> Long.compare(a1.getDate(), a2.getDate()));
                        patientAppointmentAdapter.notifyDataSetChanged();

                        // Debug output
                        Log.d("DATA_LOADED", "Loaded " + appointmentsList.size() + " appointments");
                    }
                });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            new Handler().postDelayed(() -> {
                recyclerView.invalidate();
                recyclerView.requestLayout();
                Log.d("FOCUS_FIX", "Window focus changed - refreshed UI");
            }, 200);
        }
    }
}