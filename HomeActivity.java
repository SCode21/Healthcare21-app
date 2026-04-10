package com.example.signuploginfirebasee;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private EditText etSearch;
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private TextView tvWelcome;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    // List to store doctor specialties
    private List<String> doctorSpecialties = new ArrayList<>();
    private List<LinearLayout> doctorLayouts = new ArrayList<>();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firestore and Auth
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize views
        tvWelcome = findViewById(R.id.tvWelcome);
        etSearch = findViewById(R.id.etSearch);

        // Fetch and display the user's name
        fetchUserName();

        // Handle search input focus
        etSearch.setOnFocusChangeListener((v, hasFocus) -> etSearch.setHint(hasFocus ? "Type to search..." : "Search"));

        // Initialize ImageViews for navigation
        ImageView imgbmi= findViewById(R.id.imgbmi);
        ImageView imgHospitals = findViewById(R.id.imgHospitals);
        ImageView imgfoot = findViewById(R.id.imgfoot);

        ImageView imgCardiologist = findViewById(R.id.imgCardiologist);
        ImageView imgKidney = findViewById(R.id.imgKidney);
        ImageView imgDermatologist = findViewById(R.id.imgDermatologist);
        ImageView imgOrthopedic = findViewById(R.id.imgOrthopedic);
        ImageView imgNeurologist = findViewById(R.id.imgNeurologist);
        ImageView imgDentist = findViewById(R.id.imgDentist);

        // Set click listeners for image buttons
        imgCardiologist.setOnClickListener(v -> navigateTo(CardiologistActivity.class));
        imgKidney.setOnClickListener(v -> navigateTo(KidneyActivity.class));
        imgDermatologist.setOnClickListener(v -> navigateTo(DermatologistActivity.class));
        imgOrthopedic.setOnClickListener(v -> navigateTo(OrthopedicActivity.class));
        imgNeurologist.setOnClickListener(v -> navigateTo(NeurologistActivity.class));
        imgDentist.setOnClickListener(v -> navigateTo(DentistActivity.class));
        imgbmi.setOnClickListener(v -> navigateTo(BMI.class));
        imgfoot.setOnClickListener(v -> navigateTo(StepCountActivity.class));
        imgHospitals.setOnClickListener(v -> navigateTo(NearbyPlacesActivity.class));

        // Initialize doctor specialties and layouts
        initializeDoctorSpecialties();

        // Setup Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Show profile completion dialog only if profile is incomplete
        SharedPreferences prefs = getSharedPreferences("UserProfile", MODE_PRIVATE);
        boolean isProfileComplete = prefs.getBoolean("isProfileComplete", false);

        if (!isProfileComplete) {
            showDialog();
        }

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(this::onBottomNavigationItemSelected);

        // Set Home as selected by default
        bottomNavigationView.setSelectedItemId(R.id.nav_home);


    // Implement search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterDoctorSpecialties(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    // Fetch the user's name from Firestore
    private void fetchUserName() {
        String userId = auth.getCurrentUser().getUid();
        firestore.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String FullName = document.getString("FullName");
                            if (FullName != null && !FullName.isEmpty()) {
                                tvWelcome.setText("Hello, " + FullName + "!");
                            }
                        }
                    } else {
                        tvWelcome.setText("Hello, User!");
                    }
                });
    }

    // Handle bottom navigation item selection

    private boolean onBottomNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // If already on Home, just return true without navigation
            return true;
        } else if (id == R.id.nav_exercise) {
            startActivity(new Intent(this, ExerciseActivity.class));
            return true;
        } else if (id == R.id.nav_appointments) {
            startActivity(new Intent(this, MyDoctorAppointments.class));
            return true;
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reset selection to Home when returning to this activity
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
    }

    // Show AlertDialog for profile completion
    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("Welcome!")
                .setMessage("Complete the Profile.")
                .setPositiveButton("OK", (dialog, id) -> {
                    // Redirect to ProfileActivity
                    Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Handle navigation item selection
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_profile) navigateTo(ProfileActivity.class);
        else if (id == R.id.nav_settings) navigateTo(SettingsActivity.class);
        else if (id == R.id.nav_help) navigateTo(HelpActivity.class);

        else if (id == R.id.nav_logout) finish();

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Handle back press for drawer
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    // Navigate without creating duplicate activities
    private void navigateTo(Class<?> activityClass) {
        Intent intent = new Intent(HomeActivity.this, activityClass);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    // Initialize doctor specialties and layouts
    private void initializeDoctorSpecialties() {
        // Add doctor specialties to the list
        doctorSpecialties.add("Heart");
        doctorSpecialties.add("Kidney");
        doctorSpecialties.add("Dermatologist");
        doctorSpecialties.add("Bones");
        doctorSpecialties.add("Tooth");
        doctorSpecialties.add("Brain");

        // Add doctor layouts to the list
        doctorLayouts.add((LinearLayout) findViewById(R.id.cardiologist_layout));
        doctorLayouts.add((LinearLayout) findViewById(R.id.kidney_layout));
        doctorLayouts.add((LinearLayout) findViewById(R.id.dermatologist_layout));
        doctorLayouts.add((LinearLayout) findViewById(R.id.orthopedic_layout));
        doctorLayouts.add((LinearLayout) findViewById(R.id.dentist_layout));
        doctorLayouts.add((LinearLayout) findViewById(R.id.neurologist_layout));
    }

    // Filter doctor specialties based on search text
    private void filterDoctorSpecialties(String searchText) {
        for (int i = 0; i < doctorSpecialties.size(); i++) {
            String specialty = doctorSpecialties.get(i);
            LinearLayout layout = doctorLayouts.get(i);

            if (specialty.toLowerCase().contains(searchText.toLowerCase())) {
                layout.setVisibility(View.VISIBLE); // Show the layout if it matches the search text
            } else {
                layout.setVisibility(View.GONE); // Hide the layout if it doesn't match the search text
            }
        }
    }
}