package com.student_developer.track_my_grade;

import static android.app.PendingIntent.getActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class CalculatorActivity extends BaseActivity {

    private boolean isProfileLoading = false;
    private ImageView btnProfile,btnCalculator,btnGraph;
    private ImageView ivNeedHelp;
    private TextView tvNeedHelp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        loadFragment(new ProfileFragment());

        btnProfile = findViewById(R.id.btn_profile);
        btnCalculator = findViewById(R.id.btn_calculator);
        btnGraph = findViewById(R.id.btn_graph);
        ivNeedHelp = findViewById(R.id.iv_need_help);
        tvNeedHelp = findViewById(R.id.tv_need_help);

        btnProfile.setEnabled(!isProfileLoading);
        btnCalculator.setEnabled(!isProfileLoading);
        btnGraph.setEnabled(!isProfileLoading);

        ivNeedHelp.setOnClickListener(v -> {
          Utils.intend(this, NeedHelpActivity.class);
        });

        tvNeedHelp.setOnClickListener(v -> {
            Utils.intend(this, NeedHelpActivity.class);
        });

        btnProfile.setOnClickListener(v -> {
            if (!isProfileLoading) {
                loadFragment(new ProfileFragment());
            }
        });

        btnCalculator.setOnClickListener(v -> {
            if (!isProfileLoading) {
                loadFragment(new CalculatorFragment());
            }
        });

        btnGraph.setOnClickListener(v -> {
            if (!isProfileLoading) {
                loadFragment(new GraphFragment());
            }
        });

    }
    // Method to load fragments into the FrameLayout
    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            // Get the FragmentManager to manage the fragments
            FragmentManager fragmentManager = getSupportFragmentManager();
            // Begin a transaction to replace the current fragment
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // Replace the fragment in the container (R.id.fragment_container should be the ID of your FrameLayout in XML)
            fragmentTransaction.replace(R.id.fragment_container, fragment);
            // Commit the transaction to finalize the change
            fragmentTransaction.commit();
        }
    }

    public void setProfileLoading(boolean loading) {
        isProfileLoading = loading;
        ImageView btnProfile = findViewById(R.id.btn_profile);
        ImageView btnCalculator = findViewById(R.id.btn_calculator);
        ImageView btnGraph = findViewById(R.id.btn_graph);

        btnProfile.setEnabled(!loading);
        btnCalculator.setEnabled(!loading);
        btnGraph.setEnabled(!loading);
    }

    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }

}

