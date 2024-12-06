package com.example.projectpresensikel1;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectpresensikel1.Adapter.HistoryAdapter;
import com.example.projectpresensikel1.Model.Attendance;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;
    private List<Attendance> attendanceList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private ImageButton btnBack2;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Pengguna Belum Login", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        btnBack2 = findViewById(R.id.btnBack2);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter(attendanceList);
        historyRecyclerView.setAdapter(historyAdapter);




        loadAttendanceHistory();
        btnBack2.setOnClickListener(v -> finish());
    }

    private void loadAttendanceHistory() {


        db.collection("Attendance") // Fixed spelling from 'collcection' to 'collection'
                .whereEqualTo("userId", currentUserId)

                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) { // Fixed spelling from 'isSuccesfull' to 'isSuccessful'
                        attendanceList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            try {
                                Attendance attendance = document.toObject(Attendance.class);
                                attendanceList.add(attendance); // Add the attendance object to the list
                            } catch (Exception e) {
                                Log.d("HistoryActivity", "Error parsing document to Attendance object", e);
                            }
                        }
                        attendanceList.sort((a1, a2) -> a2.getWaktuKehadiran().compareTo(a1.getWaktuKehadiran()));
                        historyAdapter.notifyDataSetChanged(); // Notify adapter about data changes
                    } else {
                        Log.e("HistoryActivity", "Error getting documents:", task.getException());
                        Toast.makeText(this, "Gagal memuat data absensi", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
