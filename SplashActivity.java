package com.example.signuploginfirebasee;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final long SPLASH_DELAY = 3000; // 3 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views
        ImageView splashImage = findViewById(R.id.splash_image);
        Button letsGoButton = findViewById(R.id.lets_go_button);

        // Verify and set splash image
        try {
            splashImage.setImageResource(R.drawable.splash_img);
        } catch (Exception e) {
            Log.e(TAG, "Error loading splash image: " + e.getMessage());
            splashImage.setImageResource(R.drawable.ic_doctors);
        }

        // Set click listener for the button
        letsGoButton.setOnClickListener(v -> navigateToLogin());

        // Auto-navigate after delay
        new Handler().postDelayed(this::navigateToLogin, SPLASH_DELAY);
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}