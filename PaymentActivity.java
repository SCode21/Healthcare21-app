package com.example.signuploginfirebasee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private TextView tvDoctorName, tvPatientName, tvAppointmentDate, tvAppointmentTime, tvAmount;
    private Button btnPayNow;
    private ImageView ivPaymentSuccess;
    private FirebaseFirestore db;
    private CardView cardDoctor, cardPatient, cardAppointment, cardPayment;

    private String doctorName, patientName, appointmentDate, appointmentTime;
    private String doctorId, patientId;
    private double amount = 100.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Initialize views
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvPatientName = findViewById(R.id.tvPatientName);
        tvAppointmentDate = findViewById(R.id.tvAppointmentDate);
        tvAppointmentTime = findViewById(R.id.tvAppointmentTime);
        tvAmount = findViewById(R.id.tvAmount);
        btnPayNow = findViewById(R.id.btnPayNow);
        ivPaymentSuccess = findViewById(R.id.ivPaymentSuccess);
        cardDoctor = findViewById(R.id.cardDoctor);
        cardPatient = findViewById(R.id.cardPatient);
        cardAppointment = findViewById(R.id.cardAppointment);
        cardPayment = findViewById(R.id.cardPayment);

        // Initially hide the success image
        ivPaymentSuccess.setVisibility(View.GONE);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get data from intent
        Intent intent = getIntent();
        doctorName = intent.getStringExtra("doctorName");
        patientName = intent.getStringExtra("patientName");
        appointmentDate = intent.getStringExtra("appointmentDate");
        appointmentTime = intent.getStringExtra("appointmentTime");
        doctorId = intent.getStringExtra("doctorId");
        patientId = intent.getStringExtra("patientId");

        // Display appointment details
        tvDoctorName.setText(doctorName != null ? doctorName : "Not Available");
        tvPatientName.setText(patientName != null ? patientName : "Not Available");
        tvAppointmentDate.setText(appointmentDate != null ? appointmentDate : "Not Available");
        tvAppointmentTime.setText(appointmentTime != null ? appointmentTime : "Not Available");
        tvAmount.setText(String.format("₹%.2f", amount)); // Changed from $ to ₹

        // Handle Pay Now button click
        btnPayNow.setOnClickListener(v -> processPayment());
    }

    private void processPayment() {
        // Disable button during processing
        btnPayNow.setEnabled(false);
        btnPayNow.setText("Processing...");

        String transactionId = "txn_" + System.currentTimeMillis();

        Map<String, Object> payment = new HashMap<>();
        payment.put("doctorName", doctorName);
        payment.put("patientName", patientName);
        payment.put("appointmentDate", appointmentDate);
        payment.put("appointmentTime", appointmentTime);
        payment.put("amount", amount);
        payment.put("transactionId", transactionId);

        db.collection("payments").add(payment)
                .addOnSuccessListener(documentReference -> {
                    // Show success image before transition
                    ivPaymentSuccess.setVisibility(View.VISIBLE);

                    // Delay transition to show the image briefly
                    btnPayNow.postDelayed(() -> {
                        Intent intent = new Intent(PaymentActivity.this, ThankYouActivity.class);
                        intent.putExtra("doctorName", doctorName);
                        intent.putExtra("patientName", patientName);
                        intent.putExtra("appointmentDate", appointmentDate);
                        intent.putExtra("appointmentTime", appointmentTime);
                        intent.putExtra("transactionId", transactionId);
                        startActivity(intent);
                        finish();
                    }, 1000); // 1 second delay
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Payment failed. Please try again.", Toast.LENGTH_SHORT).show();
                    btnPayNow.setEnabled(true);
                    btnPayNow.setText("Pay Now");
                    ivPaymentSuccess.setVisibility(View.GONE);
                });
    }
}