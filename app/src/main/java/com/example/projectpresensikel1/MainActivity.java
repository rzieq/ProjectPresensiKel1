package com.example.projectpresensikel1;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView welcomeTextView,statusPresensi;
    private Button absenButton, historyButton, logoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        welcomeTextView = findViewById(R.id.welcomeTextView);
        absenButton = findViewById(R.id.absenButton);
        historyButton = findViewById(R.id.historyButton);
        logoutButton = findViewById(R.id.logoutButton);
        statusPresensi = findViewById(R.id.statusPresensi);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            loadUserData(currentUser.getUid());
            checkAttendance(currentUser.getUid());
        } else {
            redirectToLogin();
        }

        absenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(MainActivity.this, AbsenActivity.class));
                Intent intent = new Intent(MainActivity.this, AbsenActivity.class);
                startActivityForResult(intent, 1); // 1 adalah requestCode
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            }
        });

        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, HistoryActivity.class));
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                redirectToLogin();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Panggil metode untuk mengambil data baru
            checkAttendance(mAuth.getCurrentUser().getUid()); // Periksa kehadiran lagi
        }
    }

    private void loadUserData(String userId) {
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        String name = document.getString("name");
                        welcomeTextView.setText("Selamat Datang, "+ name + "!");
                    } else {
                        Toast.makeText(MainActivity.this, "Gagal Memuat Data Pengguna", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkAttendance(String userId) {
        // Format tanggal untuk mendapatkan hari ini
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String today = sdf.format(new Date());

        // Query ke koleksi Attendance
        db.collection("Attendance")
                .whereEqualTo("userId", userId) // Filter berdasarkan userId
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        boolean hasAttended = false;
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Jika ada dokumen yang ditemukan, berarti sudah absen
                            hasAttended = true;
                            break; // Keluar dari loop setelah menemukan satu dokumen
                        }

                        // Update TextView berdasarkan status kehadiran
                        if (hasAttended) {
                            statusPresensi.setText("User telah hadir hari ini.");
                        } else {
                            statusPresensi.setText("User belum hadir hari ini.");
                        }
                    } else {

                        statusPresensi.setText("Gagal mengecek absensi.");
                    }
                });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
