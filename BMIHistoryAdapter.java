package com.example.signuploginfirebasee;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class BMIHistoryAdapter extends RecyclerView.Adapter<BMIHistoryAdapter.ViewHolder> {

    private List<BMIRecord> recordList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    public BMIHistoryAdapter(List<BMIRecord> recordList) {
        this.recordList = recordList;
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bmi_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BMIRecord record = recordList.get(position);

        holder.dateText.setText(record.getTimestamp());
        holder.bmiText.setText(String.format("BMI: %.1f", record.getBmi()));
        holder.categoryText.setText(record.getCategory());
        holder.idealWeightText.setText(record.getIdealWeight());

        holder.deleteButton.setOnClickListener(v -> {
            deleteRecord(record.getId(), position, holder.itemView);
        });
    }

    private void deleteRecord(String documentId, int position, View itemView) {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("users")
                .document(userId)
                .collection("bmiRecords")
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    recordList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, recordList.size());
                    Toast.makeText(itemView.getContext(), "Record deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(itemView.getContext(), "Error deleting record", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return recordList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView dateText, bmiText, categoryText, idealWeightText;
        Button deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            dateText = itemView.findViewById(R.id.dateText);
            bmiText = itemView.findViewById(R.id.bmiText);
            categoryText = itemView.findViewById(R.id.categoryText);
            idealWeightText = itemView.findViewById(R.id.idealWeightText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}