package com.example.signuploginfirebasee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class BMI extends AppCompatActivity {

    // UI Components
    private TextInputEditText heightInput, weightInput;
    private MaterialTextView resultText, idealWeightText, healthTipsText;
    private MaterialCardView resultCard;
    private boolean isMetric = true;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI
        initializeViews();
        setupButtonListeners();
    }

    private void initializeViews() {
        heightInput = findViewById(R.id.heightInput);
        weightInput = findViewById(R.id.weightInput);
        resultText = findViewById(R.id.resultText);
        idealWeightText = findViewById(R.id.idealWeightText);
        healthTipsText = findViewById(R.id.healthTipsText);
        resultCard = findViewById(R.id.resultCard);
    }

    private void setupButtonListeners() {
        MaterialButtonToggleGroup unitGroup = findViewById(R.id.unitGroup);
        MaterialButton calculateButton = findViewById(R.id.calculateButton);
        MaterialButton saveButton = findViewById(R.id.saveButton);
        MaterialButton historyButton = findViewById(R.id.historyButton);

        unitGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (checkedId == R.id.metricUnit && isChecked) {
                isMetric = true;
                heightInput.setHint("Height (cm)");
                weightInput.setHint("Weight (kg)");
            } else if (checkedId == R.id.imperialUnit && isChecked) {
                isMetric = false;
                heightInput.setHint("Height (ft)");
                weightInput.setHint("Weight (lbs)");
            }
        });

        calculateButton.setOnClickListener(v -> calculateBMI());
        saveButton.setOnClickListener(v -> saveBMI());
        historyButton.setOnClickListener(v -> viewHistory());
    }

    private void calculateBMI() {
        String heightStr = heightInput.getText().toString().trim();
        String weightStr = weightInput.getText().toString().trim();

        if (heightStr.isEmpty() || weightStr.isEmpty()) {
            showToast("Please enter both height and weight");
            return;
        }

        try {
            float height = Float.parseFloat(heightStr);
            float weight = Float.parseFloat(weightStr);

            // Convert to metric if imperial
            if (!isMetric) {
                height *= 30.48f;    // ft to cm
                weight *= 0.453592f;  // lbs to kg
            }

            float heightInMeters = height / 100;
            float bmi = weight / (heightInMeters * heightInMeters);

            displayResults(bmi, heightInMeters);
            resultCard.setVisibility(View.VISIBLE);
            scrollToResults();

        } catch (NumberFormatException e) {
            showToast("Please enter valid numbers");
        }
    }

    private void displayResults(float bmi, float heightInMeters) {
        String bmiCategory;
        int color;

        if (bmi < 18.5) {
            bmiCategory = "Underweight";
            color = getResources().getColor(R.color.bmi_underweight);
        } else if (bmi < 25) {
            bmiCategory = "Healthy";
            color = getResources().getColor(R.color.bmi_healthy);
        } else if (bmi < 30) {
            bmiCategory = "Overweight";
            color = getResources().getColor(R.color.bmi_overweight);
        } else {
            bmiCategory = "Obese";
            color = getResources().getColor(R.color.bmi_obese);
        }

        resultText.setText(String.format(Locale.getDefault(), "%.1f (%s)", bmi, bmiCategory));
        resultText.setTextColor(color);

        // Calculate ideal weight range
        float minWeight = 18.5f * (heightInMeters * heightInMeters);
        float maxWeight = 24.9f * (heightInMeters * heightInMeters);
        idealWeightText.setText(String.format(Locale.getDefault(),
                "Healthy weight range: %.1fkg - %.1fkg", minWeight, maxWeight));

        // Set health tips based on BMI category
        String tips = getHealthTips(bmiCategory);
        healthTipsText.setText(tips);
    }

    private String getHealthTips(String category) {
        switch (category) {
            case "Underweight":
                return getString(R.string.underweight_tips);
            case "Healthy":
                return getString(R.string.healthy_tips);
            default:
                return getString(R.string.overweight_tips);
        }
    }

    private void saveBMI() {
        if (resultCard.getVisibility() != View.VISIBLE) {
            showToast("Calculate BMI first");
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            showToast("Please sign in to save results");
            return;
        }

        try {
            String result = resultText.getText().toString();
            String[] parts = result.split("\\s+");

            float bmiValue = Float.parseFloat(parts[0]);
            String category = parts[1].replaceAll("[()]", "");
            String idealWeight = idealWeightText.getText().toString();
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    .format(new Date());

            Map<String, Object> record = new HashMap<>();
            record.put("bmi", bmiValue);
            record.put("category", category);
            record.put("idealWeight", idealWeight);
            record.put("timestamp", timestamp);

            saveRecordToFirestore(record);

        } catch (Exception e) {
            showToast("Error saving data");
            Log.e("BMI_SAVE", "Error saving BMI", e);
        }
    }

    private void saveRecordToFirestore(Map<String, Object> record) {
        db.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .collection("bmiRecords")
                .add(record)
                .addOnSuccessListener(documentReference -> {
                    showToast("BMI saved successfully");
                    Log.d("FIREBASE", "Document ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    showToast("Failed to save BMI");
                    Log.e("FIREBASE", "Error saving document", e);
                });
    }

    private void viewHistory() {
        startActivity(new Intent(this, BMIHistoryActivity.class));
    }

    private void scrollToResults() {
        findViewById(R.id.scrollView).post(() ->
                ((ScrollView) findViewById(R.id.scrollView)).fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}