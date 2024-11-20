package com.student_developer.track_my_grade;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubCodeActivity extends AppCompatActivity {


    private Spinner spinnerDepartment, spinnerSem;
    private ProgressBar proBAR;
    private LinearLayout ll1, ll2, ll_svToPRO, ll_confirmRoll, ll_SvSem;
    private Button btn_input ,btn_GPA, btn_svToPRO, btn_confirmRoll, btn_SvToSem;
    private TextView tvNoDept, tvIncorrectList ,tv_gpa_res, tvGpa;
    private String department, semester;
    private TableLayout tableLayout;
    private DatabaseReference databaseReference;
    private List<Subject2> subjectList = new ArrayList<>();
    private EditText etConfirmRoll, et_svToSem;
    private String rollNO;
    private int saveToSem, sem;
    private float gpa;
    private int numberOfSubjects = 0;
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sub_code);
        SharedPreferences sharedPref = getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        rollNO = sharedPref.getString("roll_no", null);
        sem = sharedPref.getInt("current_sem", 1);


        initUI();
        setClick();
    }

    private void setClick() {
        btn_GPA.setOnClickListener(v -> {
            calculateGPA();
        });

        btn_SvToSem.setOnClickListener((View v) -> {
            hideKeyboard(v);
            if (Utils.isNetworkAvailable(this)) {
                String semesterInput = et_svToSem.getText().toString().trim();

                if (TextUtils.isEmpty(semesterInput)) {
                    et_svToSem.setBackground(ContextCompat.getDrawable(this, R.drawable.edit_text_round_corner));
                    v.requestFocus();
                } else {
                    saveToSem = Integer.parseInt(semesterInput);

                    if (saveToSem > 0 && saveToSem <= sem) {
                        et_svToSem.setBackground(ContextCompat.getDrawable(this, R.drawable.edittext_backgrouond));
                        saveGpa(saveToSem, gpa, rollNO);
                        //saveAllSubjects(saveToSem);
                        tv_gpa_res.setText("  Your GPA is : " + String.format("%.2f", gpa) + " for Sem " + saveToSem + " saved successfully.");
                    } else {
                        et_svToSem.setBackground(ContextCompat.getDrawable(this, R.drawable.edit_text_round_corner));
                        et_svToSem.requestFocus();
                        Toast.makeText(this, "Not eligible to set GPA in SEM-" + saveToSem, Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                btn_SvToSem.setEnabled(false);

                networkCallback = new ConnectivityManager.NetworkCallback() {
                    @Override
                    public void onAvailable(@NonNull Network network) {
                        runOnUiThread(() -> {
                            et_svToSem.setEnabled(true);
                            btn_SvToSem.setEnabled(true);
                            Toast.makeText(SubCodeActivity.this, "Network connected. Now submit your GPA", Toast.LENGTH_LONG).show();
                        });

                        connectivityManager.unregisterNetworkCallback(this);
                    }

                    @Override
                    public void onLost(@NonNull Network network) {
                        runOnUiThread(() -> {
                            et_svToSem.setEnabled(false);
                            btn_SvToSem.setEnabled(false);
                        });
                    }
                };

                if (connectivityManager != null) {
                    connectivityManager.registerDefaultNetworkCallback(networkCallback);
                }
            }
        });

        btn_svToPRO.setOnClickListener(v->{
            hideKeyboard(v);
            ll_confirmRoll.setVisibility(View.VISIBLE);
        });

        btn_input.setOnClickListener(v->{
            if(ValidInput()){
                databaseReference = FirebaseDatabase.getInstance("https://app1-ec550-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference("CR_List").child(department).child("SEM-"+semester);
                ll1.setVisibility(View.GONE);
                proBAR.setVisibility(View.VISIBLE);
                loadTable();
            }
        });

        btn_confirmRoll.setOnClickListener((View v) -> {
            hideKeyboard(v);
            String rollno = etConfirmRoll.getText().toString().trim().toUpperCase();

            if (TextUtils.isEmpty(rollno)) {
                etConfirmRoll.setBackground(ContextCompat.getDrawable(this, R.drawable.edit_text_round_corner));
                etConfirmRoll.requestFocus();
            } else if (!rollno.equals(rollNO)) {
                etConfirmRoll.setBackground(ContextCompat.getDrawable(this, R.drawable.edit_text_round_corner));
                etConfirmRoll.setError("Roll No does not match");
                etConfirmRoll.requestFocus();
            } else {
                proBAR.setVisibility(View.VISIBLE);
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Users")
                        .whereEqualTo("Roll No", rollno.toUpperCase())
                        .get()
                        .addOnCompleteListener(rollNoTask -> {
                            if (rollNoTask.isSuccessful() && rollNoTask.getResult() != null) {
                                if (!rollNoTask.getResult().isEmpty()) {
                                    DocumentSnapshot documentSnapshot = rollNoTask.getResult().getDocuments().get(0);
                                    String firestoreRollNo = documentSnapshot.getString("Roll No");
                                    if (firestoreRollNo != null && firestoreRollNo.equals(rollno.toUpperCase())) {
                                        ll_confirmRoll.setVisibility(View.GONE);
                                        tvGpa.setText("CGPA RESULT (" + rollno.toUpperCase() + ")");
                                        proBAR.setVisibility(View.GONE);
                                        ll_SvSem.setVisibility(View.VISIBLE);
                                    } else {
                                        etConfirmRoll.setBackground(ContextCompat.getDrawable(this, R.drawable.edit_text_round_corner));
                                        Toast.makeText(this, "Roll No does not match", Toast.LENGTH_SHORT).show();

                                    }
                                } else {
                                    etConfirmRoll.setBackground(ContextCompat.getDrawable(this, R.drawable.edit_text_round_corner));
                                    etConfirmRoll.setError("Authendication Failed, Enter your Roll No to Proceed.");
                                }
                            } else {
                                Toast.makeText(this, "Error Confirming Roll No", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error Confirming Roll No", Toast.LENGTH_SHORT).show();
                        });
            }
        });
        tvNoDept.setOnClickListener(v->{
            String email = "trackmygrade@gmail.com";
            String subject = "DEPARTMENT_NOT_FOUND";
            String body = "Dear Administrator.\n\n I didn't found my Department in Track My Grade. I would like to request you to add below Department. \n\n[YOUR_DEPARTMENT] .\n\nBest regards,\n[YOUR_Roll No]";

            String mailto = "mailto:" + email +
                    "?subject=" + Uri.encode(subject) +
                    "&body=" + Uri.encode(body);

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

            emailIntent.setData(Uri.parse(mailto));
            startActivity(Intent.createChooser(emailIntent, "Send email"));
        });
        tvIncorrectList.setOnClickListener(v->{
            String email = "trackmygrade@gmail.com";
            String subject = "INCORRECT_SUBJECT_LIST";
            String body = "Dear Administrator.\n\nI would like to inform that Subject Details is incorrect in GPA Calculation, \n\n[CORRECT_SUBJECT_LIST] .\n\nBest regards,\n[YOUR_Roll No]";

            String mailto = "mailto:" + email +
                    "?subject=" + Uri.encode(subject) +
                    "&body=" + Uri.encode(body);

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);

            emailIntent.setData(Uri.parse(mailto));
            startActivity(Intent.createChooser(emailIntent, "Send email"));
        });
    }

    private boolean ValidInput() {
        department = spinnerDepartment.getSelectedItem().toString();
        semester = spinnerSem.getSelectedItem().toString();
        if (department.equals("Department")) {
            spinnerDepartment.setBackgroundResource(R.drawable.edit_text_round_corner);
            spinnerDepartment.requestFocus();
            return false;
        }else{
            spinnerDepartment.setBackgroundResource(R.drawable.edittext_backgrouond);
            if (semester.equals("Semeseter")) {
                spinnerSem.setBackgroundResource(R.drawable.edit_text_round_corner);
                return false;
            }else{
                spinnerDepartment.setBackgroundResource(R.drawable.edittext_backgrouond);
                spinnerSem.setBackgroundResource(R.drawable.edittext_backgrouond);
                return true;

            }
        }
    }

    private void initUI(){
        spinnerDepartment = findViewById(R.id.SubCode_dept);
        spinnerSem = findViewById(R.id.SubCode_Sem);
        proBAR = findViewById(R.id.progressBar);
        ll1 = findViewById(R.id.ll_input);
        ll2 = findViewById(R.id.ll2);
        ll_svToPRO = findViewById(R.id.ll_svTOPro);
        ll_confirmRoll = findViewById(R.id.ll_confirm_roll);
        ll_SvSem = findViewById(R.id.ll_sv_sem);
        btn_svToPRO = findViewById(R.id.btn_svToPro);
        btn_confirmRoll = findViewById(R.id.btn_confirmRoll);
        btn_input = findViewById(R.id.btn_enter);
        btn_GPA = findViewById(R.id.btn_calGPA);
        tvNoDept = findViewById(R.id.tv_noDept);
        tvIncorrectList = findViewById(R.id.tv_noSubject);
        tableLayout = findViewById(R.id.tableLayout);
        tv_gpa_res = findViewById(R.id.tv_GPA_res);
        etConfirmRoll = findViewById(R.id.et_confirmRoll);
        tvGpa = findViewById(R.id.tv_gpa);
        et_svToSem = findViewById(R.id.et_svToSem);
        btn_SvToSem = findViewById(R.id.btn_svToSem);


        ArrayAdapter<CharSequence> adapterDept = ArrayAdapter.createFromResource(this,
                R.array.crDept, R.layout.spinner_item);
        adapterDept.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerDepartment.setAdapter(adapterDept);

        ArrayAdapter<CharSequence> adapterSem = ArrayAdapter.createFromResource(this,
                R.array.ArraySem, R.layout.spinner_item2);
        adapterSem.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerSem.setAdapter(adapterSem);
    }

    private void loadTable() {
        new Handler().postDelayed(() -> {
            proBAR.setVisibility(View.GONE);
            ll2.setVisibility(View.VISIBLE);
        }, 1000);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tableLayout.removeAllViews();
                subjectList.clear();
                numberOfSubjects = 0;
                TableRow headerRow = new TableRow(SubCodeActivity.this);
                addCellToRow(headerRow, "S.No", true);
                addCellToRow(headerRow, "Sub Code", true);
                addCellToRow(headerRow, "Credit", true);
                addCellToRow(headerRow, "Grade", true);
                tableLayout.addView(headerRow);

                int serialNumber = 1;
                for (DataSnapshot child : snapshot.getChildren()) {
                    String subjectCode = child.child("CODE").getValue(String.class);
                    String credit = child.child("CREDIT").getValue(String.class);
                    final int creditValue = Integer.parseInt(credit);

                    TableRow tableRow = new TableRow(SubCodeActivity.this);
                    tableRow.setBackgroundResource(R.drawable.gradient_bf);
                    tableRow.setPadding(10, 10, 10, 10);


                    addCellToRow(tableRow, String.valueOf(serialNumber), false);


                    addCellToRow(tableRow, subjectCode, false);


                    addCellToRow(tableRow, credit, false);

                    Spinner gpaSpinner = new Spinner(SubCodeActivity.this);
                    gpaSpinner.setLayoutParams(new TableRow.LayoutParams(
                            TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
                    gpaSpinner.setPadding(15, 15, 15, 15);
                    gpaSpinner.setGravity(Gravity.CENTER);

                    ArrayAdapter<String> gpaAdapter = new ArrayAdapter<>(
                            SubCodeActivity.this, R.layout.spinner_item3,
                            Arrays.asList("Grade", "A+", "A", "B+", "B", "C","U")
                    );
                    gpaAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
                    gpaSpinner.setAdapter(gpaAdapter);

                    gpaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            String grade = (String) parentView.getItemAtPosition(position);
                            if (!grade.equals("Grade")) {
                                Subject2 subject2 = new Subject2(subjectCode, creditValue, grade);
                                subjectList.add(subject2);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                        }
                    });

                    tableRow.addView(gpaSpinner);
                    tableLayout.addView(tableRow);
                    serialNumber++;
                    numberOfSubjects++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SubCodeActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    public void saveAllSubjects(int saveToSem) {
//        List<Subject> subjectList = collectSubject();
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//
//        String semesterDocumentPath = "GPA/" + rollNO + "/Semester/SEM - " + saveToSem;
//
//        db.document(semesterDocumentPath).get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        db.document(semesterDocumentPath)
//                                .update("subjects", subjectList)
//                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Updated Semester " + saveToSem + " data successfully!"))
//                                .addOnFailureListener(e -> Log.e("Firestore", "Failed to update semester data", e));
//                    } else {
//                        db.document(semesterDocumentPath)
//                                .set(Collections.singletonMap("subjects", subjectList))
//                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Created Semester " + saveToSem + " data successfully!"))
//                                .addOnFailureListener(e -> Log.e("Firestore", "Failed to create semester data", e));
//                    }
//                })
//                .addOnFailureListener(e -> Log.e("Firestore", "Failed to check for semester document", e));
//    }
//
//    private List<Subject> collectSubject() {
//        List<Subject> subjects = new ArrayList<>();
//        for (int i = 0; i < numberOfSubjects; i++) {
//            TableRow row = (TableRow) tableLayout.getChildAt(i + 1);
//            TextView tvSubjectCode = (TextView) row.getChildAt(1);
//            TextView tvCredit = (TextView) row.getChildAt(2);
//            Spinner spinnerGrade = (Spinner) row.getChildAt(3);
//
//            String subjectCode = tvSubjectCode.getText().toString();
//            String credit = tvCredit.getText().toString();
//            String grade = spinnerGrade.getSelectedItem().toString();
//
//            if (!grade.equals("Grade")) {
//                subjects.add(new Subject(subjectCode, credit, grade));
//            }
//        }
//        return subjects;
//    }


    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    private void addCellToRow(TableRow row, String text, boolean isHeader) {
        TextView cell = new TextView(SubCodeActivity.this);
        cell.setText(text);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(20, 20, 20, 20);
        cell.setTextSize(16);
        cell.setTextColor(isHeader ? getResources().getColor(android.R.color.white) : Color.BLACK); // Header is white; content is black
        if(isHeader){
            cell.setBackgroundResource(R.color.light_violet);
        }
         cell.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT, 1f));
        if (isHeader) {
            cell.setTypeface(null, Typeface.BOLD);
        }
        row.addView(cell);
    }

    private void calculateGPA() {
        for (int i = 1; i <= numberOfSubjects; i++) {
            TableRow row = (TableRow) tableLayout.getChildAt(i);
            Spinner gradeSpinner = (Spinner) row.getChildAt(3);

            String selectedGrade = (String) gradeSpinner.getSelectedItem();
            if (selectedGrade.equals("Grade")) {
                Toast.makeText(this, "Please select a grade for all subjects.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        int totalGradePoints = 0;
        int totalCredits = 0;

        for (Subject2 subject : subjectList) {
            totalGradePoints += subject.getGPAContribution();
            totalCredits += subject.getCredit();
        }

        if (totalCredits == 0) {
            Toast.makeText(this, "Error: Total credits cannot be zero.", Toast.LENGTH_SHORT).show();
            return;
        }

        gpa = (float) totalGradePoints / totalCredits;
        displayGPA(gpa);
    }


    private void displayGPA(double gpa) {
        ll2.setVisibility(View.GONE);
        ll_svToPRO.setVisibility(View.VISIBLE);
        String gpaStr = String.format("%.2f", gpa);
        tv_gpa_res.setText("  Your GPA is : " + gpaStr);
    }

    private void saveGpa(int intsem, float gpa, String rollnoInput) {
        ll_SvSem.setVisibility(View.GONE);
        rollnoInput = rollnoInput.toUpperCase();
        String sem = String.valueOf(intsem);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference userRef = db.collection("Users").document(rollnoInput);

        Log.d("DEBUG", "User Input Roll No: " + rollnoInput);

        String finalRollnoInput = rollnoInput;
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String rollNoFromDb = documentSnapshot.getString("Roll No");

                if (!finalRollnoInput.equals(rollNoFromDb)) {
                    Toast.makeText(this, "You can only save GPA for your own roll number.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> userData = new HashMap<>();
                userData.put("Sem " + sem, gpa);

                DocumentReference docRef = db.collection("GPA").document(finalRollnoInput);

                docRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            if (document.contains("Sem " + sem)) {
                                docRef.update(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "GPA updated successfully", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(this, CalculatorActivity.class);
                                            startActivity(intent);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Failed to update GPA", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                docRef.set(userData, SetOptions.merge())
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "New semester GPA added successfully", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(this, CalculatorActivity.class);
                                            startActivity(intent);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Failed to add new semester GPA", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            docRef.set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "GPA saved successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(this, CalculatorActivity.class);
                                        startActivity(intent);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to save GPA", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Failed to check GPA document", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching GPA document", Toast.LENGTH_SHORT).show();
                    Log.e("ERROR", "Error fetching GPA document", e);  // Log the exception
                });
            } else {
                Toast.makeText(this, "Error fetching user data: Document does not exist", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ERROR", "Error fetching user data", e);
        });
    }


    @Override
    public void onBackPressed() {
        if(ll2.getVisibility() == View.VISIBLE){
            ll1.setVisibility(View.VISIBLE);
            ll2.setVisibility(View.GONE);
        }else{
            super.onBackPressed();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }

}