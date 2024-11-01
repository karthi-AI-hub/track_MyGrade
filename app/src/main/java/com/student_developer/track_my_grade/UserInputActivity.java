package com.student_developer.track_my_grade;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class UserInputActivity extends BaseActivity {

    private Spinner spinnerDepartment;
    private EditText etName, etRegNo, etDob, etPhoneNo, etSem, etClg;
    private Button btnSubmit;

    private ProgressBar progressBar;
    FirebaseDatabase database;
    DatabaseReference myRef;
    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_userinput);

        init();

        checkIfRollNumberExists();
    }

    private void init() {
        sharedPref = getSharedPreferences("UserPref", Context.MODE_PRIVATE);
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

    @SuppressLint("ClickableViewAccessibility")
    private void setOnclick() {
        etDob.setOnTouchListener((v, event) -> {
            final int DRAWABLE_END = 2;
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (etDob.getRight() - etDob.getCompoundDrawables()[DRAWABLE_END].getBounds().width())) {
                    showDatePickerDialog();
                    return true;
                }
            }
            return false;
        });

        etDob.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;
            private String currentText = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String input = s.toString().toUpperCase().replaceAll("[^\\dA-Z]", ""); // Allow only digits and letters
                StringBuilder formatted = new StringBuilder();
                int len = input.length();

                if(len==9){
                    etPhoneNo.requestFocus();
                }

                if (len <= 2) {
                    etDob.setInputType(InputType.TYPE_CLASS_NUMBER);
                    if (len == 2) {
                        formatted.append(input).append("-");
                        etDob.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
                    } else {
                        formatted.append(input);
                    }
                } else if (len > 2 && len <= 5) {
                    formatted.append(input.substring(0, 2)).append("-");

                    if (len == 5) {
                        formatted.append(input.substring(2)).append("-");
                         etDob.setInputType(InputType.TYPE_CLASS_NUMBER);
                    } else {
                        formatted.append(input.substring(2));
                    }
                } else if (len > 5) {
                     etDob.setInputType(InputType.TYPE_CLASS_NUMBER);
                    formatted.append(input.substring(0, 2)).append("-");
                    formatted.append(input.substring(2, 5)).append("-");
                    formatted.append(input.substring(5));
                }

                currentText = formatted.toString();
                etDob.setText(currentText);
                etDob.setSelection(currentText.length());

                isFormatting = false;
            }
        });

     btnSubmit.setOnClickListener(v -> {
            resetErrorStates();
            btnSubmit.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            saveStudentInfo();
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String inputDate = etDob.getText().toString();
        if (!inputDate.isEmpty()) {
            try {
                SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
                format.setLenient(false);
                Date date = format.parse(inputDate);
                if (date != null) {
                    calendar.setTime(date);
                    year = calendar.get(Calendar.YEAR);
                    month = calendar.get(Calendar.MONTH);
                    day = calendar.get(Calendar.DAY_OF_MONTH);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }


        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String[] monthNames = new String[]{
                            "JAN", "FEB", "MAR", "APR",
                            "MAY", "JUN", "JUL", "AUG",
                            "SEP", "OCT", "NOV", "DEC"
                    };

                    // Fetch the correct month name from monthNames using selectedMonth as the index.
                    String dob = String.format("%02d-%s-%d", selectedDay, monthNames[selectedMonth], selectedYear);
                    etDob.setText(dob);
                },
                year, month, day
        );

        // Set max date to today
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
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
            etName.setBackgroundResource(R.drawable.edit_text_round_corner);
            etName.setError("Enter Valid Name");
            valid = false;
        }

        if (TextUtils.isEmpty(regNo) || regNo.length() < 7) {
            etRegNo.setBackgroundResource(R.drawable.edit_text_round_corner);
            etRegNo.setError("Enter Valid Roll No");
            valid = false;
        }


        if (!isValidPhoneNumber(phoneNo)) {
            etPhoneNo.setBackgroundResource(R.drawable.edit_text_round_corner);
            etPhoneNo.setError("Enter Valid Phone No");
            valid = false;
        }


        if (TextUtils.isEmpty(dob) || dob.length()!=11) {
            etDob.setBackgroundResource(R.drawable.edit_text_round_corner);
            etDob.setError("Enter Valid DOB");
            valid = false;
        }

        if (TextUtils.isEmpty(sem) || Integer.parseInt(sem) < 1 || Integer.parseInt(sem) > 8) {
            etSem.setBackgroundResource(R.drawable.edit_text_round_corner);
            etSem.setError("SEM should be between 1 to 8");
            valid = false;
        }

        if (TextUtils.isEmpty(clg) || clg.length() < 3) {
            etClg.setBackgroundResource(R.drawable.edit_text_round_corner);
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

    private boolean isValidPhoneNumber(String phoneNo) {
        if (phoneNo.startsWith("+91")) {
            return phoneNo.length() == 13
                    && (phoneNo.charAt(3) == '9' || phoneNo.charAt(3) == '8' || phoneNo.charAt(3) == '7' || phoneNo.charAt(3) == '6')
                    && TextUtils.isDigitsOnly(phoneNo.substring(1));
        } else if (phoneNo.startsWith("9") || phoneNo.startsWith("8") || phoneNo.startsWith("7") || phoneNo.startsWith("6")) {
            return phoneNo.length() == 10 && TextUtils.isDigitsOnly(phoneNo);
        } else {
            return false;
        }
    }




    private void saveToFirebase(String name, String regNo, String phoneNo,
                                String dob, String sem, String clg, String department) {

        Map<String, String> studentData = new HashMap<>();
        studentData.put("Name", name);
        studentData.put("RegNo", regNo);
        studentData.put("Dept", department);
        studentData.put("DOB", dob);
        studentData.put("SEM", sem);

        SharedPreferences.Editor editor =sharedPref.edit();
        editor.putInt("sem", Integer.parseInt(sem));
        editor.apply();

        if (phoneNo.startsWith("+91")) {
            studentData.put("PhNo", phoneNo.substring(3));
        } else {
            studentData.put("PhNo", phoneNo);
        }

        if (clg.contains("excel") ||
                clg.contains("excel engineering") ||
                clg.contains("excel enginerring college autonomous") ||
                clg.contains("excel enginerring college (autonomous)") ||
                clg.contains("excel enginerring college(autonomous)") ||
                clg.contains("excel enginerring college") ||
                clg.contains("excel engg college") ||
                clg.contains("eec") ||
                clg.contains("excel engg")) {
            studentData.put("EXCEL ENGINEERING COLLEGE", clg);
        }else{
            studentData.put("Clg", clg);
        }
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

    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }
}
