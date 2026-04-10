package com.example.signuploginfirebasee;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StepCountActivity extends AppCompatActivity implements SensorEventListener {

    // UI Components
    private TextView stepCountText, dailyGoalText;
    private LinearProgressIndicator progressBar;

    // Sensor Components
    private SensorManager sensorManager;
    private Sensor stepCounterSensor;

    // Step Tracking Variables
    private int totalSteps = 0;
    private int previousSteps = 0;
    private static final int DAILY_GOAL = 10000;
    private static final int PERMISSION_REQUEST_CODE = 101;
    private static final String STEP_PREFS = "StepCounterPrefs";

    // Firebase Components
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_count);

        // Initialize UI
        initializeViews();

        // Initialize Firebase
        initializeFirebase();

        // Initialize Step Counter
        initializeStepCounter();
    }

    private void initializeViews() {
        stepCountText = findViewById(R.id.stepCountText);
        dailyGoalText = findViewById(R.id.dailyGoalText);
        progressBar = findViewById(R.id.progressBar);
        progressBar.setMax(100);
    }

    private void initializeFirebase() {
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    private void initializeStepCounter() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            setupSensor();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                    PERMISSION_REQUEST_CODE);
        }

        loadSavedSteps();
    }

    private void setupSensor() {
        if (sensorManager != null) {
            stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

            if (stepCounterSensor == null) {
                Toast.makeText(this, "No step counter sensor available", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ensure service is running
        startStepCounterService();

        // Load the latest steps (service might have updated them)
        loadSavedSteps();

        if (stepCounterSensor != null) {
            sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    private void startStepCounterService() {
        if (!isMyServiceRunning(StepCounterService.class)) {
            Intent serviceIntent = new Intent(this, StepCounterService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        saveCurrentSteps();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            handleStepEvent(event);
        }
    }

    private void handleStepEvent(SensorEvent event) {
        if (previousSteps == 0) {
            previousSteps = (int) event.values[0];
            return;
        }

        int newSteps = (int) event.values[0] - previousSteps;
        if (newSteps > 0) {
            totalSteps += newSteps;
            previousSteps = (int) event.values[0];
            updateUI();
            saveStepsToFirebase();
        }
    }

    private void updateUI() {
        runOnUiThread(() -> {
            stepCountText.setText(String.valueOf(totalSteps));

            int progress = calculateProgress();
            progressBar.setProgress(progress);
            updateDailyGoalText(progress);
        });
    }

    private int calculateProgress() {
        return Math.min(100, (int) (((float) totalSteps / DAILY_GOAL) * 100));
    }

    private void updateDailyGoalText(int progress) {
        dailyGoalText.setText(String.format(Locale.getDefault(),
                "%d%% of daily goal (%d/%d steps)",
                progress, totalSteps, DAILY_GOAL));
    }

    private void saveStepsToFirebase() {
        if (auth.getCurrentUser() != null) {
            String today = getCurrentDate();
            databaseRef.child("stepCounts")
                    .child(auth.getCurrentUser().getUid())
                    .child(today)
                    .setValue(totalSteps);
        }
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private void loadSavedSteps() {
        SharedPreferences prefs = getSharedPreferences(STEP_PREFS, MODE_PRIVATE);
        totalSteps = prefs.getInt("totalSteps", 0);
        previousSteps = prefs.getInt("previousSteps", 0);
        updateUI();
    }

    private void saveCurrentSteps() {
        getSharedPreferences(STEP_PREFS, MODE_PRIVATE)
                .edit()
                .putInt("totalSteps", totalSteps)
                .putInt("previousSteps", previousSteps)
                .apply();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used but required by interface
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            setupSensor();
        } else {
            Toast.makeText(this, "Permission denied - step counting disabled", Toast.LENGTH_LONG).show();
        }
    }
}