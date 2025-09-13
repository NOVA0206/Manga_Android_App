package com.example.bookorganizer;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreateSupportNavigateUpTaskStack(savedInstanceState);
        setContentView(R.layout.activity_splash);

        firebaseAuth = FirebaseAuth.getInstance();

        // Delay splash screen for 1 second
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkUser();
            }
        }, 1000);
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser == null) {
            // Not logged in → go to MainActivity
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finalize();
        } else {
            // Logged in → check user type from Firebase Realtime Database
            DatabaseReference ref = FirebaseDatabase.getInstance(
                            "https://book-organizer-dd855-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("Users");

            ref.child(firebaseUser.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String userType = "" + snapshot.child("userType").getValue();

                            if (userType.equals("user")) {
                                startActivity(new Intent(SplashActivity.this, DashboardUserActivity.class));
                                finalize();
                            } else if (userType.equals("admin")) {
                                startActivity(new Intent(SplashActivity.this, DashboardAdminActivity.class));
                                finalize();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Handle DB error (optional: log or show message)
                        }
                    });
        }
    }
}
