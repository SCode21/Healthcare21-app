package com.example.signuploginfirebasee;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AdminDashboardActivity extends AppCompatActivity {

    private LinearLayout patientsLayout, doctorsLayout, appointmentsLayout;
    private FirebaseFirestore db;
    private Button btnAddDoctor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        patientsLayout = findViewById(R.id.patientsLayout);
        doctorsLayout = findViewById(R.id.doctorsLayout);
        appointmentsLayout = findViewById(R.id.appointmentsLayout);
        btnAddDoctor = findViewById(R.id.btnAddDoctor);

        db = FirebaseFirestore.getInstance();

        btnAddDoctor.setOnClickListener(v -> startActivity(new Intent(this, AddDoctorActivity.class)));

        loadPatientsForAdmin();
        loadDoctorsForAdmin();
        loadAppointmentsForAdmin();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAllLists();
    }

    private void refreshAllLists() {
        patientsLayout.removeAllViews();
        doctorsLayout.removeAllViews();
        appointmentsLayout.removeAllViews();
        loadPatientsForAdmin();
        loadDoctorsForAdmin();
        loadAppointmentsForAdmin();
    }

    private void loadPatientsForAdmin() {
        db.collection("patients").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Patient patient = document.toObject(Patient.class);
                    patient.setId(document.getId());
                    addPatientViewForAdmin(patient);
                }
            } else {
                Toast.makeText(this, "Error loading patients", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addPatientViewForAdmin(Patient patient) {
        View patientView = getLayoutInflater().inflate(R.layout.item_patient_simple, patientsLayout, false);

        TextView tvPatientName = patientView.findViewById(R.id.tvPatientName);
        TextView tvPatientEmail = patientView.findViewById(R.id.tvPatientEmail);
        TextView tvPatientContact = patientView.findViewById(R.id.tvPatientContact);
        Button btnDeletePatient = patientView.findViewById(R.id.btnDeletePatient);

        tvPatientName.setText(patient.getFullName());
        tvPatientEmail.setText(patient.getEmail());
        tvPatientContact.setText(patient.getContact());



        btnDeletePatient.setOnClickListener(v -> showDeleteConfirmation(
                "Delete Patient",
                "Are you sure you want to delete " + patient.getFullName() + "?",
                () -> deletePatient(patient)));

        patientsLayout.addView(patientView);
    }

    private void deletePatient(Patient patient) {
        db.collection("patients").document(patient.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Patient deleted", Toast.LENGTH_SHORT).show();
                    refreshAllLists();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error deleting patient: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadDoctorsForAdmin() {
        db.collection("doctors").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Doctor doctor = document.toObject(Doctor.class);
                    doctor.setId(document.getId());
                    addDoctorViewForAdmin(doctor);
                }
            } else {
                Toast.makeText(this, "Error loading doctors", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addDoctorViewForAdmin(Doctor doctor) {
        View doctorView = getLayoutInflater().inflate(R.layout.item_doctor_simple, doctorsLayout, false);

        TextView tvDoctorName = doctorView.findViewById(R.id.tvDoctorName);
        TextView tvDoctorSpecialty = doctorView.findViewById(R.id.tvDoctorSpecialty);
        Button btnEditDoctor = doctorView.findViewById(R.id.btnEditDoctor);
        Button btnDeleteDoctor = doctorView.findViewById(R.id.btnDeleteDoctor);

        tvDoctorName.setText(doctor.getName());
        tvDoctorSpecialty.setText(doctor.getSpecialty());

        btnEditDoctor.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, EditDoctorActivity.class);
            intent.putExtra("doctorId", doctor.getId());
            startActivity(intent);
        });

        btnDeleteDoctor.setOnClickListener(v -> showDeleteConfirmation(
                "Delete Doctor",
                "Are you sure you want to delete Dr. " + doctor.getName() + "?",
                () -> deleteDoctor(doctor)));

        doctorsLayout.addView(doctorView);
    }

    private void deleteDoctor(Doctor doctor) {
        db.collection("doctors").document(doctor.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Doctor deleted", Toast.LENGTH_SHORT).show();
                    refreshAllLists();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error deleting doctor: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadAppointmentsForAdmin() {
        db.collection("appointments").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Appointment appointment = document.toObject(Appointment.class);
                    appointment.setAppointmentId(document.getId());
                    addAppointmentViewForAdmin(appointment);
                }
            } else {
                Toast.makeText(this, "Error loading appointments", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addAppointmentViewForAdmin(Appointment appointment) {
        View appointmentView = getLayoutInflater().inflate(R.layout.item_appointment_simple, appointmentsLayout, false);

        TextView tvPatientName = appointmentView.findViewById(R.id.tvPatientName);
        TextView tvDoctorName = appointmentView.findViewById(R.id.tvDoctorName);
        TextView tvAppointmentDate = appointmentView.findViewById(R.id.tvAppointmentDate);

        Button btnDeleteAppointment = appointmentView.findViewById(R.id.btnDeleteAppointment);

        tvPatientName.setText(appointment.getPatientName());
        tvDoctorName.setText(appointment.getDoctorName());
        tvAppointmentDate.setText(appointment.getFormattedDate());


        btnDeleteAppointment.setOnClickListener(v -> showDeleteConfirmation(
                "Delete Appointment",
                "Are you sure you want to delete this appointment with " + appointment.getDoctorName() + "?",
                () -> deleteAppointment(appointment)));

        appointmentsLayout.addView(appointmentView);
    }

    private void deleteAppointment(Appointment appointment) {
        db.collection("appointments").document(appointment.getAppointmentId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Appointment deleted", Toast.LENGTH_SHORT).show();
                    refreshAllLists();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error deleting appointment: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showDeleteConfirmation(String title, String message, Runnable onConfirm) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Delete", (dialog, which) -> onConfirm.run())
                .setNegativeButton("Cancel", null)
                .show();
    }
}
