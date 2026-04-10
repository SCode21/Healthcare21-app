package com.example.signuploginfirebasee;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FeedbackActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        EditText feedbackEditText = findViewById(R.id.feedbackEditText);
        Button submitFeedbackButton = findViewById(R.id.submitFeedbackButton);

        submitFeedbackButton.setOnClickListener(v -> {
            String feedback = feedbackEditText.getText().toString().trim();
            FirebaseUser currentUser = mAuth.getCurrentUser();

            if (feedback.isEmpty()) {
                Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show();
                return;
            }

            if (currentUser == null) {
                Toast.makeText(this, "Please login to submit feedback", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> feedbackData = new HashMap<>();
            feedbackData.put("userId", currentUser.getUid());
            feedbackData.put("userEmail", currentUser.getEmail());
            feedbackData.put("feedback", feedback);
            feedbackData.put("timestamp", System.currentTimeMillis());

            db.collection("feedbacks")
                    .add(feedbackData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show();
                        feedbackEditText.setText("");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to submit feedback", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}