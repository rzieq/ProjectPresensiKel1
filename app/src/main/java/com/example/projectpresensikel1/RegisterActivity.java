package com.example.projectpresensikel1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projectpresensikel1.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {
    private EditText nameInput, classInput, emailInput, passwordInput;
    private Button btnRegister;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nameInput = findViewById(R.id.nameInput);
        classInput = findViewById(R.id.classInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        btnRegister = findViewById(R.id.btnRegister);

        // Call registerUser() when the button is clicked
        btnRegister.setOnClickListener(view -> registerUser());
    }

    private void registerUser() {
        String name = nameInput.getText().toString().trim();
        String kelas = classInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Basic validation
        if (name.isEmpty() || kelas.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Harap lengkapi semua field!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a new user with email and password
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String userId = firebaseUser.getUid();
                            User user = new User(userId, name, kelas);

                            // Save user data to Firestore
                            db.collection("users").document(userId)
                                    .set(user)
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(this, "Registrasi Berhasil!", Toast.LENGTH_SHORT).show()
                                    )
                                    .addOnFailureListener(e ->
                                            Toast.makeText(this, "Gagal menyimpan data pengguna", Toast.LENGTH_SHORT).show()
                                    );
                        }
                    } else {
                        Toast.makeText(this, "Registrasi Gagal! " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
