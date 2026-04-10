package com.example.signuploginfirebasee;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class BMIHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BMIHistoryAdapter adapter;
    private List<BMIRecord> records = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmihistory);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Setup RecyclerView
        setupRecyclerView();

        // Load data
        loadBMIHistory();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BMIHistoryAdapter(records);
        recyclerView.setAdapter(adapter);
    }

    private void loadBMIHistory() {
        String userId = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("bmiRecords")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        records.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            BMIRecord record = document.toObject(BMIRecord.class);
                            record.setId(document.getId());
                            records.add(record);
                        }
                        adapter.notifyDataSetChanged();

                        if (records.isEmpty()) {
                            Toast.makeText(this, "No BMI records found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error loading history", Toast.LENGTH_SHORT).show();
                        Log.e("FIREBASE", "Error loading documents", task.getException());
                    }
                });
    }
}