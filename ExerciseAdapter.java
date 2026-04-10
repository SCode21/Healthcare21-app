package com.example.signuploginfirebasee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class ExerciseAdapter extends RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder> {

    private Context context;
    private List<ExerciseModel> exerciseList;

    public ExerciseAdapter(Context context, List<ExerciseModel> exerciseList) {
        this.context = context;
        this.exerciseList = exerciseList;
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_exercise, parent, false);
        return new ExerciseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        ExerciseModel exercise = exerciseList.get(position);

        holder.title.setText(exercise.getTitle());
        holder.description.setText(exercise.getDescription());

        // Load GIF with Glide
        Glide.with(context)
                .asGif()  // Explicitly specify this is a GIF
                .load(exercise.getGifResource())
                   // Add error image
                .into(holder.gifImageView);
    }

    @Override
    public int getItemCount() {
        return exerciseList.size();
    }

    public static class ExerciseViewHolder extends RecyclerView.ViewHolder {
        TextView title, description;
        ImageView gifImageView;

        public ExerciseViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            gifImageView = itemView.findViewById(R.id.image);
        }
    }
}