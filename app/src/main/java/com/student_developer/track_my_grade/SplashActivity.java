package com.student_developer.track_my_grade;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity {

    private static final int SPLASH_DISPLAY_LENGTH = 3500;
    private static final int NETWORK_CHECK_INTERVAL = 1000;
    private String rollNO ,staffName, staffClg;
    private FirebaseDatabase database;
    private FirebaseAuth authLogin;
    private FirebaseFirestore db;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable networkCheckRunnable;
    private SharedPreferences sharedPref;
    private static final int NOTIFICATION_PERMISSION_CODE = 100;
    private boolean notificationPermissionGranted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            } else {
                notificationPermissionGranted = true;
            }
        } else {
            notificationPermissionGranted = true;  // No need for permission check on versions below Android 13
        }
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e("SplashActivity", "Fetching FCM token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.d("SplashActivity", "FCM Token: " + token);

                });


        database = FirebaseDatabase.getInstance("https://app1-ec550-default-rtdb.asia-southeast1.firebasedatabase.app/");

        sharedPref = getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        rollNO = sharedPref.getString("roll_no", "null");

        authLogin = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ImageView logo = findViewById(R.id.logo);
        TextView title = findViewById(R.id.titleTextView);
        TextView title2 = findViewById(R.id.titleTextView2);
        ProgressBar progressBar = findViewById(R.id.progressBar);

        animateSplashScreen(logo, title,title2, progressBar);
        if (notificationPermissionGranted) {
            handler.postDelayed(this::checkNetworkAndProceed, SPLASH_DISPLAY_LENGTH);
        }
    }

    private void checkNetworkAndProceed() {
        if (!notificationPermissionGranted) {
            return;
        }
        networkCheckRunnable = new Runnable() {
            @Override
            public void run() {
                if (Utils.isNetworkConnected(SplashActivity.this)) {
                    if (isUserLoggedIn()) {
                        String userId = authLogin.getCurrentUser().getUid();
                        checkIfStaff(userId, isStaff -> {
                            if (isStaff) {
                                Intent intent = new Intent(SplashActivity.this, StaffActivity.class);
                                intent.putExtra("staff_Name", staffName);
                                intent.putExtra("staff_Clg", staffClg);
                                startActivity(intent);
                                finish();
                            }else{
                                DatabaseReference rootRef = database.getReference();
                                rootRef.get().addOnCompleteListener(task -> {
                                    if (task.isSuccessful() && task.getResult() != null) {
                                        boolean rollNoFound = false;

                                        for (DataSnapshot collegeSnapshot : task.getResult().getChildren()) {
                                            String collegeName = collegeSnapshot.getKey();
                                            if ("StudentList".equals(collegeName)) {
                                                continue;
                                            }
                                            for (DataSnapshot deptSnapshot : collegeSnapshot.getChildren()) {
                                                String departmentName = deptSnapshot.getKey();

                                                if (deptSnapshot.hasChild(rollNO)) {
                                                    rollNoFound = true;
                                                    SharedPreferences.Editor editor = sharedPref.edit();
                                                    editor.putString("collegeName", collegeName);
                                                    editor.putString("departmentName", departmentName);
                                                    editor.apply();
                                                    Intent intent = new Intent(SplashActivity.this, CalculatorActivity.class);
                                                    startActivity(intent);
                                                    finish();
                                                    break;
                                                }
                                            }
                                            if (rollNoFound) break;
                                        }

                                        if (!rollNoFound) {
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

    private void checkIfStaff(String userId, OnAStaffCheckListener listener) {
        db.collection("Staff").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        staffName = documentSnapshot.getString("User");
                        staffClg = documentSnapshot.getString("College");
                        listener.onStaffCheck(true);
                    } else {
                        listener.onStaffCheck(false);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SplashActivity.this, "Failed to check Staff status", Toast.LENGTH_SHORT).show();
                    listener.onStaffCheck(false);
                });
    }
    private void navigateTo(Class<?> activityClass) {
        startActivity(new Intent(SplashActivity.this, activityClass));
        overridePendingTransition(0, 0);
        finish();
    }

    private void animateSplashScreen(ImageView logo, TextView title,TextView title2, ProgressBar progressBar) {
        logo.setVisibility(View.INVISIBLE);
        title.setVisibility(View.INVISIBLE);
        title2.setVisibility(View.INVISIBLE);
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
                .withStartAction(() ->title.setVisibility(View.VISIBLE))
                .start();

        title2.setTranslationY(100f);
        title2.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(1500)
                .setStartDelay(1500)
                .withStartAction(() ->title2.setVisibility(View.VISIBLE))
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
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                notificationPermissionGranted = true;
                handler.postDelayed(this::checkNetworkAndProceed, SPLASH_DISPLAY_LENGTH);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.POST_NOTIFICATIONS)) {
                    Snackbar.make(findViewById(android.R.id.content),
                                    "Notification permission is required for important updates.",
                                    Snackbar.LENGTH_LONG)
                            .setAction("Retry", v -> requestNotificationPermission())
                            .show();
                } else {
                    Snackbar.make(findViewById(android.R.id.content),
                                    "Notification permission is required for important updates. Please enable it in settings.",
                                    Snackbar.LENGTH_LONG)
                            .setAction("Enable", v -> {
                                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            })
                            .show();
                }
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_CODE);
        }
    }



    private interface OnAStaffCheckListener {
        void onStaffCheck(boolean isAdmin);
    }
}
