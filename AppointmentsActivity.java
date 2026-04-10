package com.example.signuploginfirebasee;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AppointmentsActivity extends AppCompatActivity {

    private RecyclerView recyclerViewAppointments;
    private AppointmentAdapter appointmentAdapter;
    private List<Appointment> appointmentList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointments);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView and Appointment List
        recyclerViewAppointments = findViewById(R.id.recyclerViewAppointments);
        recyclerViewAppointments.setLayoutManager(new LinearLayoutManager(this));
        appointmentList = new ArrayList<>();

        // Fetch appointments for the current user
        fetchAppointments();
    }

    private void fetchAppointments() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();

            // Fetch appointments for the current user
            db.collection("appointments")
                    .whereEqualTo("patientId", userId) // Filter appointments based on the user's ID
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot querySnapshot = task.getResult();
                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                // Clear previous data and add the fetched appointments
                                appointmentList.clear();
                                for (DocumentSnapshot document : querySnapshot) {
                                    Appointment appointment = document.toObject(Appointment.class);

                                    // Manually set the appointmentId from the Firestore document ID
                                    appointment.setAppointmentId(document.getId());

                                    appointmentList.add(appointment);
                                }
                                // Update the RecyclerView with the appointments
                                appointmentAdapter = new AppointmentAdapter(appointmentList, AppointmentsActivity.this);
                                recyclerViewAppointments.setAdapter(appointmentAdapter);
                            } else {
                                // Show a message if no appointments are found
                                findViewById(R.id.tvNoAppointments).setVisibility(View.VISIBLE);
                            }
                        } else {
                            // Handle error in fetching appointments
                            Toast.makeText(AppointmentsActivity.this, "Failed to load appointments", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}
