package com.example.signuploginfirebasee;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class EmergencyContactActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);

        Button emergencyButton = findViewById(R.id.emergencyButton);
        emergencyButton.setOnClickListener(v -> {
            Intent emergencyIntent = new Intent(Intent.ACTION_DIAL);
            emergencyIntent.setData(Uri.parse("tel:911"));
            startActivity(emergencyIntent);
        });
    }
}