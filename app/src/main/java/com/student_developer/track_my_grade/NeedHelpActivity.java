package com.student_developer.track_my_grade;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NeedHelpActivity extends BaseActivity {

    TextView emailTextView, tvRequestMail, tvUpdateSem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_need_help);

        tvUpdateSem = findViewById(R.id.tvUpdateSem);
        tvUpdateSem.setOnClickListener(v -> {
            String email = "trackmygrade@gmail.com";
            String subject = "UPDATE_SEMESTER_REQUEST";
            String body = "Dear Administrator,\n\nI am requesting an update to my current academic record. Please assist in updating my semester details as outlined below:\n\n\n\nCurrent Semester: [YOUR_EXISTING_SEM] into\n\nUpdated Semester: [YOUR_CURRENT_SEM]\n\n\n\nThank you for your assistance.\n\nBest regards,\n[YOUR_Roll No]";

            String mailto = "mailto:" + email +
                    "?subject=" + Uri.encode(subject) +
                    "&body=" + Uri.encode(body);

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

            emailIntent.setData(Uri.parse(mailto));
            startActivity(Intent.createChooser(emailIntent, "Send email"));
        });

        emailTextView = findViewById(R.id.tvContactEmail);
        emailTextView.setOnClickListener(v -> {
            String email = "trackmygrade@gmail.com";
            String subject = "QUERIES";
            String body = "Dear Support Team,\n\nI have some queries in Track My Grade Application. Please assist me with this Queries.\n\n\n\n[YOUR_QUERIES]\n\n\n\nThank you,\n[YOUR_ROLL NO]";

            String mailto = "mailto:" + email +
                    "?subject=" + Uri.encode(subject) +
                    "&body=" + Uri.encode(body);

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

            emailIntent.setData(Uri.parse(mailto));
            startActivity(Intent.createChooser(emailIntent, "Send email"));
        });

        tvRequestMail = findViewById(R.id.tvRequestMail);
        tvRequestMail.setOnClickListener(v -> {
            String email = "trackmygrade@gmail.com";
            String subject = "PROFILE_EDIT_REQUEST";
            String body = "Dear Administrator,\n\nI would like to request a change to my personal details with below details. Please assist me with this process.\n\n\n\n[YOUR_NEW_DETAILS]\n\n\n\nThank you,\n[YOUR_ROLL NO]";

            String mailto = "mailto:" + email +
                    "?subject=" + Uri.encode(subject) +
                    "&body=" + Uri.encode(body);

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

            emailIntent.setData(Uri.parse(mailto));
            startActivity(Intent.createChooser(emailIntent, "Send email"));
        });


    }

    @Override
    public void onBackPressed() {
        boolean isFromStaff = getIntent().getBooleanExtra("from_staff", false);

        if (isFromStaff) {
            Utils.intend(this, StaffActivity.class);
        } else {
            Utils.intend(this, CalculatorActivity.class);
        }
    }

}