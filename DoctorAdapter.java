package com.example.signuploginfirebasee;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import java.util.List;

public class DoctorAdapter extends BaseAdapter {
    private final Context context;
    private final List<Doctor> doctorList;

    public DoctorAdapter(Context context, List<Doctor> doctorList) {
        this.context = context;
        this.doctorList = doctorList;
    }

    @Override
    public int getCount() {
        return doctorList.size();
    }

    @Override
    public Object getItem(int position) {
        return doctorList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_doctors, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Doctor doctor = doctorList.get(position);

        holder.tvDoctorName.setText(doctor.getName());
        holder.tvSpecialty.setText(doctor.getSpecialty());

        // Always show 4.5 rating with star
        holder.tvRating.setText("4.5");
        holder.ivStar.setVisibility(View.VISIBLE);

        // Load image
        Glide.with(context)
                .load(doctor.getImageUrl())
                .placeholder(R.drawable.ic_doctor)
                .error(R.drawable.ic_doctor)
                .into(holder.ivDoctorImage);

        return convertView;
    }

    private static class ViewHolder {
        ImageView ivDoctorImage;
        TextView tvDoctorName, tvSpecialty, tvRating;
        ImageView ivStar;

        public ViewHolder(View view) {
            ivDoctorImage = view.findViewById(R.id.ivDoctorImage);
            tvDoctorName = view.findViewById(R.id.tvDoctorName);
            tvSpecialty = view.findViewById(R.id.tvSpecialty);
            tvRating = view.findViewById(R.id.tvRating);
            ivStar = view.findViewById(R.id.ivStar);
        }
    }
}