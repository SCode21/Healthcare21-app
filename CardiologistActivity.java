package com.example.signuploginfirebasee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class CardiologistActivity extends AppCompatActivity {
    private static final String TAG = "CardiologistActivity";
    private List<Doctor> doctorList;
    private DoctorAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cardiologist);

        ListView doctorListView = findViewById(R.id.doctorListView);
        doctorList = new ArrayList<>();
        adapter = new DoctorAdapter(this, doctorList);
        doctorListView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();

        fetchDoctors();

        doctorListView.setOnItemClickListener((parent, view, position, id) -> {
            Doctor selectedDoctor = doctorList.get(position);
            Intent intent = new Intent(CardiologistActivity.this, DoctorProfileActivity.class);
            intent.putExtra("doctorId", selectedDoctor.getId());
            startActivity(intent);
        });
    }

    private void fetchDoctors() {
        db.collection("doctors")
                .whereEqualTo("specialty", "Cardiologist")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        doctorList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            try {
                                Doctor doctor = new Doctor();
                                doctor.setId(doc.getId());
                                doctor.setName(doc.getString("name"));
                                doctor.setSpecialty(doc.getString("specialty"));
                                doctor.setEmail(doc.getString("email"));
                                doctor.setContact(doc.getString("contact"));
                                doctor.setImageUrl(doc.getString("imageUrl"));

                                // Set fixed rating of 4.5 for all doctors
                                doctor.setRating(4.5);

                                doctorList.add(doctor);
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing doctor data", e);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Error fetching doctors", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Error getting doctors", task.getException());
                    }
                });
    }
}