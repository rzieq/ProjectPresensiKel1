package com.example.projectpresensikel1.Adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectpresensikel1.Model.Attendance; // Ensure the correct import for Attendance model
import com.example.projectpresensikel1.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
    private List<Attendance> attendanceList;

    public HistoryAdapter(List<Attendance> attendanceList) {
        this.attendanceList = attendanceList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_attendance, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Attendance attendance = attendanceList.get(position);

        holder.nameTextView.setText(attendance.getName());
        holder.statusTextView.setText("Status: " + attendance.getStatus());
        holder.locationTextView.setText("Lokasi: " + attendance.getLokasi());
        holder.timeTextView.setText("Waktu: " + attendance.getWaktuKehadiran());

        if (attendance.getFotoBase64() != null) {
            byte[] decodedString = Base64.decode(attendance.getFotoBase64(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.photoImageView.setImageBitmap(decodedByte);
        } else {
            holder.photoImageView.setImageResource(R.drawable.ic_placeholder_photo);
        }
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView photoImageView;
        public TextView nameTextView;
        public TextView statusTextView;
        public TextView locationTextView;
        public TextView timeTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.photoImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            locationTextView = itemView.findViewById(R.id.locationTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
        }
    }
}
