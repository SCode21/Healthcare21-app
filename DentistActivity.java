
package com.example.signuploginfirebasee;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DentistActivity extends AppCompatActivity {

    private ListView doctorListView;
    private List<Doctor> doctorList;
    private DoctorAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dentist);

        doctorListView = findViewById(R.id.doctorListView);
        doctorList = new ArrayList<>();
        adapter = new DoctorAdapter(this, doctorList);
        doctorListView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();

        fetchDoctors(); // Fetch dentists from Firebase

        doctorListView.setOnItemClickListener((parent, view, position, id) -> {
            Doctor selectedDoctor = doctorList.get(position);
            Intent intent = new Intent(DentistActivity.this, DoctorProfileActivity.class);
            intent.putExtra("doctorId", selectedDoctor.getId());
            startActivity(intent);
        });
    }

    private void fetchDoctors() {
        db.collection("doctors").whereEqualTo("specialty", "Dentist").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        doctorList.clear();
                        if (task.getResult().isEmpty()) {
                            Toast.makeText(this, "No Dentists found", Toast.LENGTH_SHORT).show();
                        }
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            Doctor doctor = doc.toObject(Doctor.class);
                            doctor.setId(doc.getId());
                            doctorList.add(doctor);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Error fetching dentists", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
