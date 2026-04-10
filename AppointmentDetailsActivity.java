package com.example.signuploginfirebasee;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AppointmentDetailsActivity extends AppCompatActivity {

    private TextView tvPatientName, tvAppointmentDate, tvAppointmentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_details);

        // Initialize views
        tvPatientName = findViewById(R.id.tvPatientName);
        tvAppointmentDate = findViewById(R.id.tvAppointmentDate);
        tvAppointmentTime = findViewById(R.id.tvAppointmentTime);

        // Get data from intent
        Intent intent = getIntent();
        String patientName = intent.getStringExtra("patientName");
        String appointmentDate = intent.getStringExtra("appointmentDate");
        String appointmentTime = intent.getStringExtra("appointmentTime");

        // Display appointment details
        tvPatientName.setText("Patient: " + patientName);
        tvAppointmentDate.setText("Date: " + appointmentDate);
        tvAppointmentTime.setText("Time: " + appointmentTime);
    }
}