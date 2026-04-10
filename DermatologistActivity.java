package com.example.signuploginfirebasee;



import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.signuploginfirebasee.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DermatologistActivity extends AppCompatActivity {

    private ListView doctorListView;
    private List<Doctor> doctorList;
    private DoctorAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dermatologist);

        doctorListView = findViewById(R.id.doctorListView);
        doctorList = new ArrayList<>();
        adapter = new DoctorAdapter(this, doctorList);
        doctorListView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();

        fetchDoctors();

        doctorListView.setOnItemClickListener((parent, view, position, id) -> {
            Doctor selectedDoctor = doctorList.get(position);
            Intent intent = new Intent(DermatologistActivity.this, DoctorProfileActivity.class);
            intent.putExtra("doctorId", selectedDoctor.getId());
            startActivity(intent);
        });
    }

    private void fetchDoctors() {
        db.collection("doctors").whereEqualTo("specialty", "Dermatologist").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        doctorList.clear();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Doctor doctor = doc.toObject(Doctor.class);
                            doctor.setId(doc.getId());
                            doctorList.add(doctor);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Error fetching doctors", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
