package com.student_developer.track_my_grade;


import android.app.AlertDialog;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
    }

    // Method to show the exit confirmation dialog
    protected void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit App");
        builder.setMessage("Do you really want to exit the app?");
        builder.setPositiveButton("Yes", (dialog, which) -> finish());
        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onBackPressed() {
        // Create an AlertDialog builder
        new AlertDialog.Builder(this)
                .setMessage("Do you really want to exit the app?")
                .setCancelable(false)  // Prevent the dialog from being dismissed by clicking outside
                .setPositiveButton("Yes", (dialog, which) -> {
                    // Exit the application when user clicks "Yes"
                    finishAffinity();  // Close the application
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Dismiss the dialog and do nothing when user clicks "No"
                    dialog.dismiss();
                })
                .show();
    }
}
