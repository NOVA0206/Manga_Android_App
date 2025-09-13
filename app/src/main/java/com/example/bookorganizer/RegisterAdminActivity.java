package com.example.bookorganizer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookorganizer.databinding.ActivityRegisterBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterAdminActivity extends AppCompatActivity {

    // View binding
    private ActivityRegisterBinding binding;

    // Firebase auth
    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Initialize View Binding before using it
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.backBtn.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        binding.registerbtn.setOnClickListener(v -> validateData());
    }

    private String name = "", email = "", password = "";

    private void validateData() {
        name = binding.nameEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        password = binding.passwordEt.getText().toString().trim();
        String cpassword = binding.cpasswordEt.getText().toString().trim();

        // Validate Data
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Enter your Name", Toast.LENGTH_SHORT).show();
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email pattern!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter password!", Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(cpassword)) {
            Toast.makeText(this, "Confirm Password!", Toast.LENGTH_SHORT).show();
        } else if (!password.equals(cpassword)) {
            Toast.makeText(this, "Passwords don't match!", Toast.LENGTH_SHORT).show();
        } else {
            createAdminAccount();
        }
    }

    private void createAdminAccount() {
        progressDialog.setMessage("Creating Admin Account...");
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d("FirebaseAuth", "Admin registered successfully: " + authResult.getUser().getUid());
                        saveAdminInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Log.e("FirebaseAuth", "Registration failed: " + e.getMessage());
                        Toast.makeText(RegisterAdminActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveAdminInfo() {
        progressDialog.setMessage("Saving Admin Info...");

        long timestamp = System.currentTimeMillis();

        // ✅ Ensure user is authenticated before getting UID
        if (firebaseAuth.getCurrentUser() == null) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error: User not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = firebaseAuth.getCurrentUser().getUid();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", uid);
        hashMap.put("email", email);
        hashMap.put("name", name);
        hashMap.put("profileImage", ""); // Can add default image URL
        hashMap.put("userType", "admin"); // ✅ Changed from "user" to "admin"
        hashMap.put("timestamp", timestamp);

        // ✅ Storing Admin data in "Admins" node instead of "Users"
        DatabaseReference ref = FirebaseDatabase.getInstance("https://book-organizer-dd855-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Admins");
        ref.child(uid)
                .setValue(hashMap)
                .addOnSuccessListener(unused -> {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterAdminActivity.this, "Admin Account Created!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterAdminActivity.this, LoginAdminActivity.class)); // ✅ Redirect to Admin Login
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(RegisterAdminActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
