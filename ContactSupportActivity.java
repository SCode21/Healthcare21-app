package com.example.signuploginfirebasee;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ContactSupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_support);

        // Email Support Button
        Button emailSupportButton = findViewById(R.id.emailSupportButton);
        emailSupportButton.setOnClickListener(v -> {
            // Predefined email address
            String email = "rapartisneha@gmail.com";
            String subject = "Support Request";
            String body = "Hello, I need help with...";

            // Create an intent to send an email
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:")); // Only email apps should handle this
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);

            // Verify that the intent will resolve to an activity
            if (emailIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(emailIntent);
            } else {
                // If no email app is available, try a fallback intent
                Intent fallbackIntent = new Intent(Intent.ACTION_SEND);
                fallbackIntent.setType("message/rfc822");
                fallbackIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                fallbackIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                fallbackIntent.putExtra(Intent.EXTRA_TEXT, body);

                if (fallbackIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(fallbackIntent);
                } else {
                    // If no app can handle the request, show a toast
                    Toast.makeText(this, "No email app found. Please install an email app or contact support at rapartisneha@gmail.com", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}