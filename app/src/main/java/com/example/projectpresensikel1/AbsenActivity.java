package com.example.projectpresensikel1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.projectpresensikel1.Model.Attendance;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AbsenActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_LOCATION_PERMISSION = 2;
    private static final int REQUEST_CAMERA_PERMISSION = 3;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FusedLocationProviderClient fusedLocationClient;
    private ImageView photoPreview;
    private Bitmap photoBitmap;
    private Button btnCapturePhoto, btnSubmitAttendance;
    private RadioGroup statusRadioGroup;
    private ImageButton btnBack;

    private double currentLat;
    private double currentLng;

    private final double TARGET_LAT = -7.552910;
    private final double TARGET_LNG = 110.797368;
    private final int MAX_DISTANCE_METERS = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_absen);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        photoPreview = findViewById(R.id.photoPreview);
        btnCapturePhoto = findViewById(R.id.btnCapturePhoto);
        btnSubmitAttendance = findViewById(R.id.btnSubmitAttendance);
        statusRadioGroup = findViewById(R.id.statusRadioGroup);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        btnCapturePhoto.setOnClickListener(v -> checkCameraPermissionAndCapturePhoto());

        btnSubmitAttendance.setOnClickListener(v -> submitAttendance());

        // Mendapatkan lokasi ketika aplikasi dimulai
        getLocation();
    }

    private void checkCameraPermissionAndCapturePhoto() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            capturePhoto();
        }
    }

    private void capturePhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            photoBitmap = (Bitmap) extras.get("data");
            photoPreview.setImageBitmap(photoBitmap);
        }
    }

    private void getLocation() {
        // Cek izin lokasi sebelum mengambil lokasi
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentLat = location.getLatitude();
                currentLng = location.getLongitude();
                checkDistanceFromTarget();
            } else {
                Toast.makeText(this, "Tidak dapat mengambil lokasi. Coba lagi.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkDistanceFromTarget() {
        // Mengecek jarak dari lokasi target
        float[] results = new float[1];
        Location.distanceBetween(currentLat, currentLng, TARGET_LAT, TARGET_LNG, results);
        if (results[0] > MAX_DISTANCE_METERS) {
            Toast.makeText(this, "Anda berada di luar batas jarak presensi!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Lokasi valid untuk presensi.", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitAttendance() {
        if (photoBitmap == null) {
            Toast.makeText(this, "Lengkapi foto sebelum presensi!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            db.collection("users").document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name") != null ? documentSnapshot.getString("name") : "Tidak Diketahui";
                            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                            String fotoBase64 = convertBitmapToBase64(photoBitmap);
                            String lokasi = currentLat + ", " + currentLng;
                            String status = getStatusFromRadioGroup();

                            if (status.isEmpty()) {
                                Toast.makeText(this, "Silakan pilih status kehadiran.", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // Jika status adalah "Hadir", cek jarak dari target
                            if ("Hadir".equals(status)) {
                                // Mengecek jarak dari lokasi target hanya jika status "Hadir"
                                if (!isWithinDistance(currentLat, currentLng)) {
                                    Toast.makeText(this, "Anda berada di luar batas jarak presensi!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }

                            // Membuat objek Attendance
                            Attendance attendance = new Attendance(userId, name, status, lokasi, timestamp, fotoBase64);

                            // Menyimpan data presensi ke Firestore
                            db.collection("Attendance").add(attendance)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(this, "Presensi Berhasil!", Toast.LENGTH_SHORT).show();
                                        setResult(RESULT_OK);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal menyimpan presensi", Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(this, "Data pengguna tidak ditemukan!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Gagal mengambil data pengguna", Toast.LENGTH_SHORT).show());
        }
    }

    private String getStatusFromRadioGroup() {
        int selectedId = statusRadioGroup.getCheckedRadioButtonId();
        if (selectedId == R.id.radioHadir) {
            return "Hadir";
        } else if (selectedId == R.id.radioSakit) {
            return "Sakit";
        } else if (selectedId == R.id.radioIzin) {
            return "Izin";
        }
        return "";
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) return null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);
        byte[] byteArray = baos.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                Toast.makeText(this, "Izin lokasi tidak diberikan", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                capturePhoto();
            } else {
                Toast.makeText(this, "Izin kamera tidak diberikan", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isWithinDistance(double lat, double lng) {
        // Mengecek jarak dari lokasi target
        float[] results = new float[1];
        Location.distanceBetween(lat, lng, TARGET_LAT, TARGET_LNG, results);
        return results[0] <= MAX_DISTANCE_METERS;
    }
}
