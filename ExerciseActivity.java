package com.example.signuploginfirebasee;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ExerciseActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ExerciseAdapter adapter;
    private List<ExerciseModel> exerciseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        // Initialize exercise list
        initializeExerciseData();

        // Setup adapter
        adapter = new ExerciseAdapter(this, exerciseList);
        recyclerView.setAdapter(adapter);
    }

    private void initializeExerciseData() {
        exerciseList = new ArrayList<>();

        // Make sure these drawable resources are GIFs in your res/drawable folder
        exerciseList.add(new ExerciseModel("Neck", "Exercises for neck strength", R.drawable.exersice_6));
        exerciseList.add(new ExerciseModel("Back", "Exercises for back posture", R.drawable.exersice_2));
        exerciseList.add(new ExerciseModel("Shoulder", "Shoulder mobility exercises", R.drawable.exersice_3));
        exerciseList.add(new ExerciseModel("Elbow", "Elbow flexibility exercises", R.drawable.exersice_4));
        exerciseList.add(new ExerciseModel("Leg & Thigh", "Leg strength exercises", R.drawable.exersice_5));
        exerciseList.add(new ExerciseModel("Knee", "Knee strengthening exercises", R.drawable.exersice_1));
        exerciseList.add(new ExerciseModel("Hand", "Hand exercises", R.drawable.exersice_15));

    }
}