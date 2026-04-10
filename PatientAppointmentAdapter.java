package com.example.signuploginfirebasee;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PatientAppointmentAdapter extends RecyclerView.Adapter<PatientAppointmentAdapter.ViewHolder> {

    private List<Appointment> appointmentsList;
    private Context context;

    public PatientAppointmentAdapter(List<Appointment> appointmentsList, Context context) {
        this.appointmentsList = appointmentsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointmentsList.get(position);

        // Set appointment details
        holder.tvPatientName.setText("Patient: " + appointment.getPatientName());
        holder.tvDoctorName.setText("Doctor: " + appointment.getDoctorName());
        holder.tvAppointmentDate.setText("Date: " + appointment.getFormattedDate());
        holder.tvAppointmentTime.setText("Time: " + appointment.getAppointmentTime());
        holder.tvStatus.setText("Status: " + appointment.getStatus());

        // Status color coding
        long currentTime = System.currentTimeMillis();
        if (appointment.getDate() < currentTime) {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.pastAppointmentColor));
        } else if (appointment.getDate() == currentTime) {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.presentAppointmentColor));
        } else {
            holder.tvStatus.setTextColor(ContextCompat.getColor(context, R.color.upcomingAppointmentColor));
        }

        // Button click listener
        holder.btnChatWithDoctor.setOnClickListener(v -> {
            String doctorId = appointment.getDoctorId();
            String patientId = appointment.getPatientId();

            if (doctorId != null && patientId != null) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("doctorId", doctorId);
                intent.putExtra("patientId", patientId);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appointmentsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvDoctorName, tvAppointmentDate, tvAppointmentTime, tvStatus;
        Button btnChatWithDoctor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvAppointmentDate = itemView.findViewById(R.id.tvAppointmentDate);
            tvAppointmentTime = itemView.findViewById(R.id.tvAppointmentTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnChatWithDoctor = itemView.findViewById(R.id.btnChatWithDoctor);
        }
    }
}