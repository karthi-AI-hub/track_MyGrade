package com.student_developer.track_my_grade;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class StudentDetailActivity extends AppCompatActivity {

    private TextView fullDetailsTextView;
    private DatabaseReference databaseReference;
    private String rollNo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        fullDetailsTextView = findViewById(R.id.fullDetailsTextView);
        rollNo = getIntent().getStringExtra("rollNo");

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance("https://app1-ec550-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Students").child(rollNo);

        fetchStudentDetails();
    }

    private void fetchStudentDetails() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                StringBuilder details = new StringBuilder();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    details.append(snapshot.getKey()).append(": ").append(snapshot.getValue()).append("\n");
                }
                fullDetailsTextView.setText(details.toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(StudentDetailActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();

            }
        });
    }
}
