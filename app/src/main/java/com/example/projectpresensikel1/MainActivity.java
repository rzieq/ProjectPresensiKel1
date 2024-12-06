package com.example.projectpresensikel1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView welcomeTextView;
    private TextView textStatus, statusKeterangan;

    private CardView cardAbsen, cardRiwayat, cardLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        welcomeTextView = findViewById(R.id.welcomeTextView);
        cardAbsen = findViewById(R.id.cardAbsen);
        cardRiwayat = findViewById(R.id.cardRiwayat);
        cardLogout = findViewById(R.id.cardLogout);
        textStatus = findViewById(R.id.textStatus);
        statusKeterangan = findViewById(R.id.statusKeterangan);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadUserData(currentUser.getUid());
            checkAttendance(currentUser.getUid());
        } else {
            redirectToLogin();
        }

        cardAbsen.setOnClickListener(view -> {
            checkAttendanceForAbsenButton(mAuth.getCurrentUser().getUid());

        });

        cardRiwayat.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
        });

        cardLogout.setOnClickListener(view -> {
            mAuth.signOut();
            redirectToLogin();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            checkAttendance(mAuth.getCurrentUser().getUid());
        }
    }

    private void loadUserData(String userId) {
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String name = document.getString("name");
                        welcomeTextView.setText("Selamat Datang, " + name + "!");
                    } else {
                        Toast.makeText(MainActivity.this, "Gagal Memuat Data Pengguna", Toast.LENGTH_SHORT).show();
                    }
                });
    }



    private void checkAttendance(String userId) {
        // Menggunakan Calendar untuk mendapatkan waktu hari ini dalam format Timestamp
        Date currentDate = new Date(); SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = dateFormat. format(currentDate);

        String startOfDay = today + " 00:00:00";
        String endOfDay = today + " 23:59:59";

        // Query untuk mencari absensi pada hari ini
        db.collection("Attendance")
                .whereEqualTo("userId", userId)
                .whereGreaterThan("waktuKehadiran", startOfDay)
                .whereLessThan("waktuKehadiran", endOfDay)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() > 0) {
                        textStatus.setText("Anda sudah presensi hari ini");
                        statusKeterangan.setText("Terimakasih!");
                    } else {
                        textStatus.setText("Anda belum presensi hari ini");
                        statusKeterangan.setText("Silahkan Lakukan Presensi");
                    }
                })
                .addOnFailureListener(e -> {
                    textStatus.setText("Gagal mengecek absensi");
                    Log.e("Absensi Error", "Query gagal: ", e);
                });
    }

    private void checkAttendanceForAbsenButton(String userId) {
        // Menggunakan Calendar untuk mendapatkan waktu hari ini dalam format Timestamp
        Date currentDate = new Date(); SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String today = dateFormat. format(currentDate);

        String startOfDay = today + " 00:00:00";
        String endOfDay = today + " 23:59:59";

        // Query untuk mencari absensi pada hari ini
        db.collection("Attendance")
                .whereEqualTo("userId", userId)
                .whereGreaterThan("waktuKehadiran", startOfDay)
                .whereLessThan("waktuKehadiran", endOfDay)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() > 0) {
                        textStatus.setText("Anda sudah presensi hari ini");
                        statusKeterangan.setText("Terimakasih!");
                        Toast.makeText(this, "Anda Sudah Melakukan Presensi Hari ini", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        textStatus.setText("Anda belum presensi hari ini");
                        statusKeterangan.setText("Silahkan Lakukan Presensi");
                        if (isWithinAllowedTime()) {
                            Intent intent = new Intent(MainActivity.this, AbsenActivity.class);
                            startActivityForResult(intent, 1);
                        } else {
                            Toast.makeText(MainActivity.this, "Presensi hanya dapat dilakukan pada jam 07.00 hingga 16.00", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    textStatus.setText("Gagal mengecek absensi");
                    Log.e("Absensi Error", "Query gagal: ", e);
                });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private boolean isWithinAllowedTime() {
        // Tentukan jam awal dan akhir
        int startHour = 7; // Jam mulai
        int endHour = 16;  // Jam selesai

        // Dapatkan waktu saat ini
        Calendar currentTime = Calendar.getInstance();
        int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);

        // Periksa apakah waktu saat ini dalam rentang
        return currentHour >= startHour && currentHour < endHour;
    }

}
