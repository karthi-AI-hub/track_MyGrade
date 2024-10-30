package com.student_developer.track_my_grade;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 2000;
    private static final int NETWORK_CHECK_INTERVAL = 1000;
    private String rollNO;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseAuth authLogin;
    private FirebaseFirestore db;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable networkCheckRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        // Initialize Firebase references
        database = FirebaseDatabase.getInstance("https://app1-ec550-default-rtdb.asia-southeast1.firebasedatabase.app/");
        myRef = database.getReference("Students");

        // Retrieve roll number from SharedPreferences
        SharedPreferences sharedPref = getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        rollNO = sharedPref.getString("roll_no", null);

        authLogin = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI Elements
        ImageView logo = findViewById(R.id.logo);
        TextView title = findViewById(R.id.titleTextView);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        // Animation and initial delay
        animateSplashScreen(logo, title, progressBar);
        handler.postDelayed(this::checkNetworkAndProceed, SPLASH_DISPLAY_LENGTH);
    }

    private void checkNetworkAndProceed() {
        networkCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (isNetworkConnected()) {
                    if (isUserLoggedIn()) {
                        String userId = authLogin.getCurrentUser().getUid();
                        checkIfAdmin(userId, isAdmin -> {
                            if (isAdmin) {
                                navigateTo(MainActivity.class);
                            } else {
                                Toast.makeText(SplashActivity.this,"Roll No is : "+rollNO,Toast.LENGTH_SHORT).show();
                                myRef.child(rollNO).get().addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        if (task.getResult().exists()) {
                                            navigateTo(CalculatorActivity.class);
                                        } else {
                                            navigateTo(UserInputActivity.class);
                                        }
                                    } else {
                                        Toast.makeText(SplashActivity.this, "Error fetching data. Please try again later.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    } else {
                        Toast.makeText(SplashActivity.this,"User not Logged In",Toast.LENGTH_SHORT).show();
                        navigateTo(RegisterActivity.class);
                    }
                } else {
                    Snackbar.make(findViewById(android.R.id.content), "No Internet. Waiting for connection...", Snackbar.LENGTH_LONG).show();
                    handler.postDelayed(this, NETWORK_CHECK_INTERVAL);
                }
            }
        };
        handler.post(networkCheckRunnable);
    }


    private boolean isUserLoggedIn() {
        return authLogin.getCurrentUser() != null;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    private void checkIfAdmin(String userId, OnAdminCheckListener listener) {
        db.collection("Admins").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> listener.onAdminCheck(documentSnapshot.exists()))
                .addOnFailureListener(e -> Toast.makeText(SplashActivity.this, "Failed to check admin status", Toast.LENGTH_SHORT).show());
    }

    private void navigateTo(Class<?> activityClass) {
        startActivity(new Intent(SplashActivity.this, activityClass));
        overridePendingTransition(0, 0);
        finish();
    }

    private void animateSplashScreen(ImageView logo, TextView title, ProgressBar progressBar) {
        logo.setVisibility(View.INVISIBLE);
        title.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        logo.setScaleX(0f);
        logo.setScaleY(0f);
        logo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(1500)
                .setStartDelay(500)
                .withStartAction(() -> logo.setVisibility(View.VISIBLE))
                .start();

        title.setTranslationY(100f);
        title.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(1500)
                .setStartDelay(1000)
                .withStartAction(() -> title.setVisibility(View.VISIBLE))
                .start();

        progressBar.setAlpha(0f);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.animate()
                .alpha(1f)
                .setDuration(800)
                .setStartDelay(1500)
                .start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && networkCheckRunnable != null) {
            handler.removeCallbacks(networkCheckRunnable);
        }
    }

    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }

    private interface OnAdminCheckListener {
        void onAdminCheck(boolean isAdmin);
    }
}
