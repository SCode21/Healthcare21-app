package com.example.signuploginfirebasee;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        // FAQ Button
        Button faqButton = findViewById(R.id.faqButton);
        faqButton.setOnClickListener(v -> {
            Intent faqIntent = new Intent(HelpActivity.this, FaqActivity.class);
            startActivity(faqIntent);
        });

        // Contact Support Button
        Button contactSupportButton = findViewById(R.id.contactSupportButton);
        if (contactSupportButton == null) {
            Log.e("HelpActivity", "contactSupportButton is null");
        } else {
            Log.d("HelpActivity", "contactSupportButton found");
        }
        contactSupportButton.setOnClickListener(v -> {
            Intent contactSupportIntent = new Intent(HelpActivity.this, ContactSupportActivity.class);
            startActivity(contactSupportIntent);
        });



        // Emergency Contact Button
        Button emergencyButton = findViewById(R.id.emergencyButton);
        emergencyButton.setOnClickListener(v -> {
            Intent emergencyIntent = new Intent(HelpActivity.this, EmergencyContactActivity.class);
            startActivity(emergencyIntent);
        });

        // Feedback Button
        Button feedbackButton = findViewById(R.id.feedbackButton);
        feedbackButton.setOnClickListener(v -> {
            Intent feedbackIntent = new Intent(HelpActivity.this, FeedbackActivity.class);
            startActivity(feedbackIntent);
        });
    }
}