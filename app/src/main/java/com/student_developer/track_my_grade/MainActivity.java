package com.student_developer.track_my_grade;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.activity.EdgeToEdge;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private ListView listView;
    private CustomAdapter adapter;
    private List<String> gpaDataList;
    private FirebaseFirestore db;
    private Button btn_logOut;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);
        btn_logOut = findViewById(R.id.btn_logout);
        listView = findViewById(R.id.list_view);
        gpaDataList = new ArrayList<>();
        adapter = new CustomAdapter(this, gpaDataList);
        listView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        loadHome();

        btn_logOut.setOnClickListener(v->{
            startActivity(new Intent(this, CalculatorActivity.class));
            finish();
        });

        bottomNavigationView.findViewById(R.id.nav_user_data).setOnClickListener(v -> loadUSERData());

        bottomNavigationView.findViewById(R.id.nav_second_button).setOnClickListener(v -> loadHome());

        bottomNavigationView.findViewById(R.id.nav_collection_gpa).setOnClickListener(v -> loadCollectionGPA());
    }

    private void loadHome() {
        bottomNavigationView.setSelectedItemId(R.id.nav_second_button);
        gpaDataList.clear();
        adapter.notifyDataSetChanged();
    }

    private void loadCollectionGPA() {
        bottomNavigationView.setSelectedItemId(R.id.nav_collection_gpa);
        db.collection("GPA").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                gpaDataList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String rollNo = document.getId();

                    StringBuilder gpaInfo = new StringBuilder("Roll No : " + rollNo + "\n");

                    for (int i = 1; i <= 8; i++) {
                        Double semGPA = document.getDouble("Sem " + i);

                        if (semGPA != null) {
                            double availableGPA = semGPA;
                            gpaInfo.append("SEM ").append(i)
                                    .append("   :    ")
                                    .append(String.format("%.2f", availableGPA))
                                    .append("\n");

                        }
                    }

                    gpaDataList.add(gpaInfo.toString());
                }

                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Error getting GPA data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadUSERData() {
        bottomNavigationView.setSelectedItemId(R.id.nav_user_data);

        db.collection("Users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                gpaDataList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String Email = document.getString("Email");
                    String RollNo = document.getString("Roll No");

                    if (Email != null && RollNo != null) {
                        String Users = "Email: " + Email + "\nRoll No: " + RollNo;
                        gpaDataList.add(Users);
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Error getting documents: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }
}
