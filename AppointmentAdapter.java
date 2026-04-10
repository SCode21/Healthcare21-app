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
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.ViewHolder> {
    private List<Appointment> appointmentsList;
    private final Context context;

    public AppointmentAdapter(List<Appointment> appointmentsList, Context context) {
        this.appointmentsList = appointmentsList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_appointment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Appointment appointment = appointmentsList.get(position);
        holder.tvPatientName.setText(appointment.getPatientName());
        holder.tvDoctorName.setText(appointment.getDoctorName());
        holder.tvAppointmentDate.setText(appointment.getFormattedDate());
        holder.tvAppointmentTime.setText(appointment.getAppointmentTime());
        holder.tvStatus.setText(appointment.getStatus());

        // Update Status Button Listener
        holder.btnUpdateStatus.setOnClickListener(v -> {
            String appointmentId = appointment.getAppointmentId();
            if (appointmentId != null) {
                updateAppointmentStatus(holder, appointmentId);
            }
        });

        // Chat with Patient Button Listener
        holder.btnChatWithPatient.setOnClickListener(v -> {
            String patientId = appointment.getPatientId();
            String doctorId = appointment.getDoctorId();
            String patientName = appointment.getPatientName();
            String doctorName = appointment.getDoctorName();

            if (patientId != null && doctorId != null) {
                startDoctorChatActivity(patientId, doctorId, patientName, doctorName);
            }
        });
    }

    private void updateAppointmentStatus(ViewHolder holder, String appointmentId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("appointments").document(appointmentId)
                .update("status", "Completed")
                .addOnSuccessListener(aVoid -> {
                    holder.tvStatus.setText("Completed");
                    Toast.makeText(context, "Status Updated", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Update Failed", Toast.LENGTH_SHORT).show();
                    Log.e("UPDATE_ERROR", "Failed to update status", e);
                });
    }

    private void startDoctorChatActivity(String patientId, String doctorId, String patientName, String doctorName) {
        Intent intent = new Intent(context, DoctorChatsActivity.class);
        intent.putExtra("patientId", patientId);
        intent.putExtra("doctorId", doctorId);
        intent.putExtra("patientName", patientName);
        intent.putExtra("doctorName", doctorName);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return appointmentsList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPatientName, tvDoctorName, tvAppointmentDate, tvAppointmentTime, tvStatus;
        Button btnUpdateStatus, btnChatWithPatient;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPatientName = itemView.findViewById(R.id.tvPatientName);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvAppointmentDate = itemView.findViewById(R.id.tvAppointmentDate);
            tvAppointmentTime = itemView.findViewById(R.id.tvAppointmentTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
            btnChatWithPatient = itemView.findViewById(R.id.btnChatWithPatient);
        }
    }
}