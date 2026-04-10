package com.example.signuploginfirebasee;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StepCounterService extends Service implements SensorEventListener {
    private static final String TAG = "StepCounterService";
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private int totalSteps = 0;
    private int previousSteps = 0;
    private NotificationManager notificationManager;
    private FirebaseAuth auth;
    private DatabaseReference databaseRef;

    // Constants
    private static final String STEP_PREFS = "StepCounterPrefs";
    private static final String CHANNEL_ID = "step_counter_channel";
    private static final int PERSISTENT_NOTIFICATION_ID = 1;
    private static final int MILESTONE_INTERVAL = 100;
    private static final int DAILY_GOAL = 10000;

    // Milestone tracking
    private int lastNotifiedMilestone = 0;
    private long lastNotificationTime = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service starting");

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        // Initialize sensor
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        }

        // Initialize notification
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel();

        // Load saved steps and check day change
        SharedPreferences prefs = getSharedPreferences(STEP_PREFS, MODE_PRIVATE);
        totalSteps = prefs.getInt("totalSteps", 0);
        previousSteps = prefs.getInt("previousSteps", 0);
        lastNotifiedMilestone = (totalSteps / MILESTONE_INTERVAL) * MILESTONE_INTERVAL;
        checkDayChange();

        // Start foreground service
        startForegroundService();

        // Register sensor listener if available
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Log.e(TAG, "No step counter sensor available");
            stopSelf();
        }
    }

    private void startForegroundService() {
        Notification notification = createPersistentNotification();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(PERSISTENT_NOTIFICATION_ID, notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH);
            } else {
                startForeground(PERSISTENT_NOTIFICATION_ID, notification);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error starting foreground service", e);
            startForeground(PERSISTENT_NOTIFICATION_ID, notification);
        }
    }

    private void checkDayChange() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        SharedPreferences prefs = getSharedPreferences(STEP_PREFS, MODE_PRIVATE);
        String lastDate = prefs.getString("lastDate", "");

        if (!today.equals(lastDate)) {
            totalSteps = 0;
            previousSteps = 0;
            lastNotifiedMilestone = 0;
            prefs.edit()
                    .putString("lastDate", today)
                    .putInt("totalSteps", 0)
                    .putInt("previousSteps", 0)
                    .apply();
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Step Counter",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Step counter notifications");
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createPersistentNotification() {
        int progress = Math.min(100, (int) (((float) totalSteps / DAILY_GOAL) * 100));
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Step Counter Active")
                .setContentText(String.format(Locale.getDefault(),
                        "%d steps (%d%% of goal)", totalSteps, progress))
                .setSmallIcon(R.drawable.ic_walking)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            int currentSteps = (int) event.values[0];

            if (previousSteps == 0) {
                previousSteps = currentSteps;
                return;
            }

            int stepsSinceLastUpdate = currentSteps - previousSteps;
            if (stepsSinceLastUpdate > 0) {
                totalSteps += stepsSinceLastUpdate;
                previousSteps = currentSteps;

                // Calculate current milestone
                int currentMilestone = (totalSteps / MILESTONE_INTERVAL) * MILESTONE_INTERVAL;

                // Only notify if we've crossed a new milestone
                if (currentMilestone > lastNotifiedMilestone) {
                    sendMilestoneNotification(currentMilestone);
                    lastNotifiedMilestone = currentMilestone;
                }

                saveCurrentSteps();
                saveStepsToFirebase();
                updateNotification();
            }
        }
    }

    private void sendMilestoneNotification(int milestone) {
        // 1-second debounce to prevent rapid notifications
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastNotificationTime < 1000) return;
        lastNotificationTime = currentTime;

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Step Milestone Reached!")
                .setContentText("You've walked " + milestone + " steps!")
                .setSmallIcon(R.drawable.ic_walking)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(milestone, notification);
    }

    private void updateNotification() {
        notificationManager.notify(PERSISTENT_NOTIFICATION_ID, createPersistentNotification());
    }

    private void saveCurrentSteps() {
        getSharedPreferences(STEP_PREFS, MODE_PRIVATE)
                .edit()
                .putInt("totalSteps", totalSteps)
                .putInt("previousSteps", previousSteps)
                .apply();
    }

    private void saveStepsToFirebase() {
        if (auth.getCurrentUser() != null) {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            databaseRef.child("stepCounts")
                    .child(auth.getCurrentUser().getUid())
                    .child(today)
                    .setValue(totalSteps);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
}