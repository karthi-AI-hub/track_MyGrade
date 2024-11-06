package com.student_developer.track_my_grade;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.github.mikephil.charting.charts.LineChart;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
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
    private TextView tvNeedHelp, tv_logOut, tvTitle;
    private ImageView ivNeedHelp;
    private String staffName, userName;
    private DatabaseReference databaseReference;
    private SharedPreferences sharedPref;
    private StudentAdapter adapter;
    private List<String> studentList;
    private List<String> rollNoList;
    private FloatingActionButton fab_menu, fab_logOut;
    private boolean isMenuOpen = false;
    private ConnectivityManager.NetworkCallback networkCallback;
    private ConnectivityManager connectivityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);
        EdgeToEdge.enable(this);

        sharedPref = StaffActivity.this.getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        userName = sharedPref.getString("user_name", "Staff");
        staffName = getIntent().getStringExtra("staff_Name");
        searchBar = findViewById(R.id.searchBar);
        searchBar.setEnabled(true);
        searchBar.requestFocus();
        tvTitle = findViewById(R.id.title);
        tvNeedHelp = findViewById(R.id.tv_need_help);
        ivNeedHelp = findViewById(R.id.iv_need_help);
        tv_logOut = findViewById(R.id.tv_logout);
        fab_menu = findViewById(R.id.fab_menu);
        fab_logOut = findViewById(R.id.fab_logout);

        if(staffName != null){
            tvTitle.setText(staffName);
        }else{
            tvTitle.setText(userName);
        }

        searchResultsListView = findViewById(R.id.searchResultsListView);
        databaseReference = FirebaseDatabase.getInstance("https://app1-ec550-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("AIDS");

        studentList = new ArrayList<>();
        rollNoList = new ArrayList<>();
        adapter = new StudentAdapter(this, studentList);
        searchResultsListView.setAdapter(adapter);

        fab_menu.setOnClickListener(v -> {
            float startRotation = isMenuOpen ? 225f : 0f;
            float endRotation = isMenuOpen ? 0f : 225f;
            ObjectAnimator rotation = ObjectAnimator.ofFloat(fab_menu, "rotation", startRotation, endRotation);
            rotation.setDuration(300);
            rotation.start();
            if (isMenuOpen) {
                closeMenu();
            } else {
                openMenu();
            }
        });
        fab_logOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Utils.intend(StaffActivity.this, LoginActivity.class);
            finish();});
        ivNeedHelp.setOnClickListener(v -> openHelpActivity());
        tvNeedHelp.setOnClickListener(v -> openHelpActivity());


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

    private void openMenu() {
        fab_logOut.setVisibility(View.VISIBLE);
        tv_logOut.setVisibility(View.VISIBLE);

        fab_logOut.animate().translationY(-100f).alpha(1f).start();
        tv_logOut.animate().translationY(-160f).alpha(1f).start();


        isMenuOpen = true;
    }

    private void closeMenu() {
        fab_logOut.animate().translationY(0).alpha(0f).withEndAction(() -> fab_logOut.setVisibility(View.GONE)).start();
        tv_logOut.animate().translationY(0).alpha(0f).withEndAction(() -> tv_logOut.setVisibility(View.GONE)).start();


        isMenuOpen = false;
    }

    private void openHelpActivity() {
        Intent intent = new Intent(StaffActivity.this, NeedHelpActivity.class);
        intent.putExtra("from_staff", true);
        startActivity(intent);
        finish();
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
                                studentList.add("NAME      : " + name + "\nROLL NO : " + rollNumber);
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
        finish();
    }

    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }
}
