package com.student_developer.track_my_grade;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StaffActivity extends AppCompatActivity {

    private EditText searchBar;
    private ListView searchResultsListView;
    private DatabaseReference databaseReference;
    private ArrayAdapter<String> adapter;
    private List<String> studentList;
    private List<String> rollNoList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_staff);

        searchBar = findViewById(R.id.searchBar);
        searchResultsListView = findViewById(R.id.searchResultsListView);
        databaseReference = FirebaseDatabase.getInstance("https://app1-ec550-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Students");
        studentList = new ArrayList<>();
        rollNoList = new ArrayList<>();

        adapter = new CustomAdapter(this,studentList);
        searchResultsListView.setAdapter(adapter);

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String rollNo = s.toString().trim().toUpperCase();
                if (!rollNo.isEmpty()) {
                    searchStudent(rollNo);
                } else {
                    studentList.clear();
                    rollNoList.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchResultsListView.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            String selectedRollNo = rollNoList.get(position);
            Intent intent = new Intent(this, StudentDetailActivity.class);
            intent.putExtra("rollNo", selectedRollNo);
            startActivity(intent);
        });
    }

    private void searchStudent(String rollNo) {
        databaseReference.orderByKey().startAt(rollNo).endAt(rollNo + "\uf8ff")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        studentList.clear();
                        rollNoList.clear();

                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String name = snapshot.child("Name").getValue(String.class);
                            String rollNumber = snapshot.getKey();

                            if (name != null && rollNumber != null) {
                                studentList.add(name + " (" + rollNumber + ")");
                                rollNoList.add(rollNumber);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(StaffActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();

                    }
                });
    }

}