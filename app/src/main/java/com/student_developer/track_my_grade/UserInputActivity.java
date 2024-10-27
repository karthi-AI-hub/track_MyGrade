package com.student_developer.track_my_grade;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UserInputActivity extends AppCompatActivity {

    private Spinner spinnerDepartment;
    private EditText etName, etRegNo, etDob, etPhoneNo, etSem, etClg;
    private Button btnSubmit;

    private ProgressBar progressBar;
    FirebaseDatabase database;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_userinput);

        init();

        checkIfRollNumberExists();
    }

    private void init() {
        SharedPreferences sharedPref = getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        String rollNO = sharedPref.getString("roll_no", null);

        database = FirebaseDatabase.getInstance("https://app1-ec550-default-rtdb.asia-southeast1.firebasedatabase.app/");
        myRef = database.getReference("Students").child(rollNO);

        etName = findViewById(R.id.ur_name);
        etRegNo = findViewById(R.id.ur_reg);
        etDob = findViewById(R.id.ur_dob);
        etPhoneNo = findViewById(R.id.ur_ph);
        etSem = findViewById(R.id.ur_sem);
        etClg = findViewById(R.id.ur_clg);
        spinnerDepartment = findViewById(R.id.ur_dept);
        btnSubmit = findViewById(R.id.btn_ur_Submit);
        progressBar = findViewById(R.id.progressBar);

        ArrayAdapter<CharSequence> adapterDept = ArrayAdapter.createFromResource(this,
                R.array.departments, R.layout.spinner_item);
        adapterDept.setDropDownViewResource(R.layout.spinner_dropdown_item);
        spinnerDepartment.setAdapter(adapterDept);
    }

    private void checkIfRollNumberExists() {
        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setVisibility(View.GONE);

        myRef.get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            btnSubmit.setVisibility(View.VISIBLE);
            if (task.isSuccessful() && task.getResult().exists()) {
                startActivity(new Intent(this, CalculatorActivity.class));
                finish();
            } else  if (task.isSuccessful()) {
                setOnclick();
            } else {
                Toast.makeText(this, "Error fetching data. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setOnclick() {
        etDob.setOnClickListener(v -> showDatePickerDialog());
        btnSubmit.setOnClickListener(v -> {
            resetErrorStates();
            btnSubmit.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            saveStudentInfo();
        });
    }

    private void saveStudentInfo() {
        String name = etName.getText().toString().trim();
        String regNo = etRegNo.getText().toString().trim();
        String phoneNo = etPhoneNo.getText().toString().trim();
        String department = spinnerDepartment.getSelectedItem().toString();
        String dob = etDob.getText().toString().trim();
        String sem = etSem.getText().toString().trim();
        String clg = etClg.getText().toString().trim();

        boolean valid = true;

        if (TextUtils.isEmpty(name) || name.length() < 3) {
            etName.setError("Enter Valid Name");
            valid = false;
        }

        if (TextUtils.isEmpty(regNo) || regNo.length() < 7) {
            etRegNo.setError("Enter Valid Roll No");
            valid = false;
        }

        if (TextUtils.isEmpty(phoneNo) || phoneNo.length() != 10) {
            etPhoneNo.setError("Enter Valid Phone No");
            valid = false;
        }

        if (TextUtils.isEmpty(dob)) {
            etDob.setError("DOB is Required");
            valid = false;
        }

        if (TextUtils.isEmpty(sem) || Integer.parseInt(sem) < 1 || Integer.parseInt(sem) > 8) {
            etSem.setError("SEM should be between 1 to 8");
            valid = false;
        }

        if (TextUtils.isEmpty(clg) || clg.length() < 3) {
            etClg.setError("Enter Valid College name");
            valid = false;
        }

        if (department.equals("Select Department")) {
            spinnerDepartment.setBackgroundResource(R.drawable.edit_text_round_corner);
            valid = false;
        }

        if (valid) {
            resetErrorStates();
            saveToFirebase(name, regNo, phoneNo, dob, sem, clg, department);
        } else {
            Toast.makeText(this, "Please fill all the fields correctly", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            btnSubmit.setVisibility(View.VISIBLE);
        }
    }

    private void saveToFirebase(String name, String regNo, String phoneNo,
                                String dob, String sem, String clg, String department) {

        Map<String, String> studentData = new HashMap<>();
        studentData.put("Name", name);
        studentData.put("RegNo", regNo);
        studentData.put("PhoneNo", phoneNo);
        studentData.put("Dept", department);
        studentData.put("DOB", dob);
        studentData.put("SEM", sem);
        studentData.put("Clg", clg);

        myRef.setValue(studentData)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setVisibility(View.VISIBLE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Student information saved successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(UserInputActivity.this, CalculatorActivity.class));
                    } else {
                        Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String dob = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    etDob.setText(dob);
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void resetErrorStates() {
        etName.setBackgroundResource(R.drawable.edittext_backgrouond);
        etRegNo.setBackgroundResource(R.drawable.edittext_backgrouond);
        etPhoneNo.setBackgroundResource(R.drawable.edittext_backgrouond);
        etDob.setBackgroundResource(R.drawable.edittext_backgrouond);
        etSem.setBackgroundResource(R.drawable.edittext_backgrouond);
        etClg.setBackgroundResource(R.drawable.edittext_backgrouond);
        spinnerDepartment.setBackgroundResource(R.drawable.edittext_backgrouond);
        btnSubmit.setVisibility(View.VISIBLE);
    }
}
