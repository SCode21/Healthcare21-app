package com.example.signuploginfirebasee;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DoctorListActivity extends AppCompatActivity {

    private ListView doctorListView;
    private DoctorAdapter doctorAdapter;
    private List<Doctor> doctorList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_list);

        doctorListView = findViewById(R.id.doctorListView);
        doctorList = new ArrayList<>();
        doctorAdapter = new DoctorAdapter(this, doctorList);
        doctorListView.setAdapter(doctorAdapter);

        db = FirebaseFirestore.getInstance();
        fetchDoctors();
    }

    private void fetchDoctors() {
        db.collection("doctors").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    doctorList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Doctor doctor = document.toObject(Doctor.class);
                        doctor.setId(document.getId());
                        doctorList.add(doctor);
                    }
                    doctorAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching doctors", Toast.LENGTH_SHORT).show();
                });
    }
}