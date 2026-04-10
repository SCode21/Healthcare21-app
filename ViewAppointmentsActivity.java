package com.example.signuploginfirebasee;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewAppointmentsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AppointmentAdapter appointmentAdapter;
    private List<Appointment> appointmentsList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_appointments);

        // Initialize UI components
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize data structures
        appointmentsList = new ArrayList<>();
        appointmentAdapter = new AppointmentAdapter(appointmentsList, this);
        recyclerView.setAdapter(appointmentAdapter);

        // Add diagnostic checks
        recyclerView.postDelayed(this::runDiagnostics, 1000);

        // Fetch appointments
        fetchAppointments();
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

    private void fetchAppointments() {
        String doctorId = auth.getCurrentUser().getUid();
        if (doctorId == null || doctorId.isEmpty()) {
            Toast.makeText(this, "Doctor ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference appointmentsRef = db.collection("appointments");
        Query query = appointmentsRef.whereEqualTo("doctorId", doctorId);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                appointmentsList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String appointmentId = document.getId();
                    String status = document.getString("status");
                    Log.d("APPT_STATUS", "Status: " + status);

                    // Create appointment object
                    Appointment appointment = new Appointment(
                            document.getString("patientId"),
                            document.getString("doctorId"),
                            document.getString("patientName"),
                            document.getString("doctorName"),
                            document.getString("appointmentTime"),
                            document.getLong("date"),
                            status
                    );
                    appointment.setAppointmentId(appointmentId);

                    appointmentsList.add(appointment);
                }

                appointmentAdapter.notifyDataSetChanged();
                Log.d("DATA_LOADED", "Loaded " + appointmentsList.size() + " appointments");
            } else {
                Toast.makeText(this, "Failed to fetch appointments", Toast.LENGTH_SHORT).show();
                Log.e("FETCH_ERROR", "Error fetching appointments", task.getException());
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