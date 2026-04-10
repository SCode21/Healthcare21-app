package com.example.signuploginfirebasee;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class ThankYouActivity extends AppCompatActivity {

    private TextView tvThanksMessage, tvBookingDetails;
    private Button btnReturnHome;
    private ImageView ivThankYou;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thank_you);

        // Initialize views
        tvThanksMessage = findViewById(R.id.tvThanksMessage);
        tvBookingDetails = findViewById(R.id.tvBookingDetails);
        btnReturnHome = findViewById(R.id.btnReturnHome);
        ivThankYou = findViewById(R.id.ivThankYou);

        // Add animation to the thank you image
        Animation bounceAnimation = AnimationUtils.loadAnimation(this, R.anim.bounce);
        ivThankYou.startAnimation(bounceAnimation);

        // Retrieve extras from PaymentActivity
        String docName = getIntent().getStringExtra("doctorName");
        String patName = getIntent().getStringExtra("patientName");
        String transactionId = getIntent().getStringExtra("transactionId");

        // Fallback in case values are null
        if (docName == null) docName = "the Doctor";
        if (patName == null) patName = "Valued Patient";
        if (transactionId == null) transactionId = "N/A";

        // Create formatted messages
        String thankYouMessage = "Thank You, " + patName + "!";
        String bookingDetails = "Your appointment with " + docName + " has been confirmed.\n\n" +
                "Transaction ID: " + transactionId + "\n\n" ;

        // Set the text views
        tvThanksMessage.setText(thankYouMessage);
        tvBookingDetails.setText(bookingDetails);

        // Customize the return home button
        btnReturnHome.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        btnReturnHome.setTextColor(Color.WHITE);
        btnReturnHome.setAllCaps(false);

        // Return to home screen with animation
        btnReturnHome.setOnClickListener(v -> {
            startActivity(new Intent(ThankYouActivity.this, HomeActivity.class));
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}