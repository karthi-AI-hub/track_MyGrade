package com.student_developer.track_my_grade;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class StaffActivity extends BaseActivity {

    private EditText searchBar;
    private ListView searchResultsListView;
    private Button btnLogOut;
    private TextView tvNeedHelp;
    private ImageView ivNeedHelp;
    private DatabaseReference databaseReference;
    private StudentAdapter adapter;
    private List<String> studentList;
    private List<String> rollNoList;
    private ConnectivityManager.NetworkCallback networkCallback;
    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);
        EdgeToEdge.enable(this);


        searchBar = findViewById(R.id.searchBar);
        searchBar.setEnabled(true);
        searchBar.requestFocus();
        btnLogOut = findViewById(R.id.btnlogOut);
        tvNeedHelp = findViewById(R.id.tv_need_help);
        ivNeedHelp = findViewById(R.id.iv_need_help);
        searchResultsListView = findViewById(R.id.searchResultsListView);
        databaseReference = FirebaseDatabase.getInstance("https://app1-ec550-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Students");

        studentList = new ArrayList<>();
        rollNoList = new ArrayList<>();
        adapter = new StudentAdapter(this, studentList);
        searchResultsListView.setAdapter(adapter);

        ivNeedHelp.setOnClickListener(v -> openHelpActivity());
        tvNeedHelp.setOnClickListener(v -> openHelpActivity());

        btnLogOut.setOnClickListener(v -> {
            Utils.intend(StaffActivity.this, LoginActivity.class);
            finish();
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handleSearchTextChanged(s.toString().trim().toUpperCase());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchResultsListView.setOnItemClickListener((parent, view, position, id) -> openStudentDetail(position));
    }

    private void openHelpActivity() {
        Intent intent = new Intent(StaffActivity.this, NeedHelpActivity.class);
        intent.putExtra("from_staff", true);
        startActivity(intent);
    }

    private void handleSearchTextChanged(String rollNo) {
        if (Utils.isNetworkAvailable(this)) {
            searchBar.setEnabled(true);
            searchResultsListView.setEnabled(true);
            searchBar.requestFocus();
            searchBar.setBackgroundResource(R.drawable.edittext_backgrouond);
            if (!rollNo.isEmpty()) {
                searchStudent(rollNo);
            } else {
                studentList.clear();
                rollNoList.clear();
                adapter.notifyDataSetChanged();
            }
        } else {
            searchBar.setEnabled(false);
            searchResultsListView.setEnabled(false);
            searchBar.setBackgroundResource(R.drawable.edit_text_round_corner);
            Utils.Snackbar(findViewById(android.R.id.content), "No Internet. Waiting for connection...", "long");

            connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivityManager != null) {
                networkCallback = new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(@NonNull Network network) {
                        runOnUiThread(() -> {
                            Utils.Snackbar(findViewById(android.R.id.content), "Network connected. You can now search for student details...", "long");
                            searchStudent(rollNo);
                            searchBar.setEnabled(true);
                            searchResultsListView.setEnabled(true);
                            searchBar.requestFocus();
                            searchBar.setBackgroundResource(R.drawable.edittext_backgrouond);
                        });
                        connectivityManager.unregisterNetworkCallback(this);
                    }

                    @Override
                    public void onLost(@NonNull Network network) {
                        runOnUiThread(() -> Utils.Snackbar(findViewById(android.R.id.content), "Network lost...", "long"));
                    }
                };
                connectivityManager.registerDefaultNetworkCallback(networkCallback);
            }
        }
    }

    private void searchStudent(String rollNo) {
        databaseReference.orderByKey().startAt(rollNo).endAt(rollNo + "\uf8ff")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        studentList.clear();
                        rollNoList.clear();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String name = snapshot.child("Name").getValue(String.class);
                            String rollNumber = snapshot.getKey();
                            if (name != null && rollNumber != null) {
                                studentList.add("NAME    : " + name + "\nROLL NO  : " + rollNumber);
                                rollNoList.add(rollNumber);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Utils.Snackbar(findViewById(android.R.id.content), "Error retrieving data.", "long");
                    }
                });
    }

    private void openStudentDetail(int position) {
        String selectedRollNo = rollNoList.get(position);
        Intent intent = new Intent(this, StudentDetailActivity.class);
        intent.putExtra("rollNo", selectedRollNo.toUpperCase());
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }
}
