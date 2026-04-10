package com.example.signuploginfirebasee;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Patterns;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
public class LoginActivity extends AppCompatActivity {
    private EditText loginEmail, loginPassword;
    private TextView signupRedirectText, forgotPassword;
    private Button loginButton;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        signupRedirectText = findViewById(R.id.signUpRedirectText);
        forgotPassword = findViewById(R.id.forgot_password);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginEmail.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                loginPassword.requestFocus();
                return true;
            }
            return false;
        });

        loginPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                performLogin();
                return true;
            }
            return false;
        });

        loginButton.setOnClickListener(v -> performLogin());

        signupRedirectText.setOnClickListener(v -> showSignupRoleDialog());

        forgotPassword.setOnClickListener(view -> showForgotPasswordDialog());
    }

    private void showSignupRoleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Your Role");
        String[] roles = {"Patient", "Doctor", "Admin"};
        builder.setItems(roles, (dialog, which) -> {
            switch (which) {
                case 0: // Patient
                    startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                    break;
                case 1: // Doctor
                    startActivity(new Intent(LoginActivity.this, DoctorSignupActivity.class));
                    break;
                case 2: // Admin
                    startActivity(new Intent(LoginActivity.this, AdminRegisterActivity.class));
                    break;
            }
        });
        builder.show();
    }

    private void performLogin() {
        String email = loginEmail.getText().toString();
        String pass = loginPassword.getText().toString();

        if (!email.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (!pass.isEmpty()) {
                if (isStrongPassword(pass)) {
                    auth.signInWithEmailAndPassword(email, pass)
                            .addOnSuccessListener(authResult -> {
                                checkUserRole(authResult.getUser().getUid());
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    loginPassword.setError("Password must be at least 8 characters with uppercase, lowercase, number and special character");
                }
            } else {
                loginPassword.setError("Password cannot be empty");
            }
        } else if (email.isEmpty()) {
            loginEmail.setError("Email cannot be empty");
        } else {
            loginEmail.setError("Please enter valid email");
        }
    }

    private void checkUserRole(String uid) {
        db.collection("users").document(uid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        String role = task.getResult().getString("role");
                        redirectToRoleActivity(role);
                    } else {
                        Toast.makeText(this, "Error fetching user role", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void redirectToRoleActivity(String role) {
        Intent intent;
        switch (role.toLowerCase()) {
            case "admin":
                intent = new Intent(this, AdminDashboardActivity.class);
                break;
            case "doctor":
                intent = new Intent(this, DoctorHomeActivity.class);
                break;
            case "patient":
            default:
                intent = new Intent(this, HomeActivity.class);
                break;
        }
        startActivity(intent);
        finish();
    }

    private boolean isStrongPassword(String password) {
        if (password.length() < 8) return false;
        if (!password.matches(".*[A-Z].*")) return false;
        if (!password.matches(".*[a-z].*")) return false;
        if (!password.matches(".*\\d.*")) return false;
        return password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*");
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot, null);
        EditText emailBox = dialogView.findViewById(R.id.emailBox);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.btnReset).setOnClickListener(view -> {
            String userEmail = emailBox.getText().toString();

            if (TextUtils.isEmpty(userEmail)) {
                emailBox.setError("Email cannot be empty");
                return;
            }

            auth.sendPasswordResetEmail(userEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Check your email", Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        } else {
                            Toast.makeText(this, "Failed to send email", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(view -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        dialog.show();
    }
}