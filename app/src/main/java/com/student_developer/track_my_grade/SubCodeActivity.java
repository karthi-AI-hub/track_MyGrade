package com.student_developer.track_my_grade;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;

public class SubCodeActivity extends AppCompatActivity {


    private Spinner spinnerDepartment, spinnerSem;
    private ProgressBar proBAR;
    private LinearLayout ll1, ll2;
    private Button btn_input ,btn_GPA;
    private TextView tvNoDept, tvIncorrectList;
    private String department, semester;
    private TableLayout tableLayout;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sub_code);

        initUI();
        setClick();
    }

    private void setClick() {
        btn_input.setOnClickListener(v->{
            if(ValidInput()){
                databaseReference = FirebaseDatabase.getInstance("https://app1-ec550-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference("CR_List").child(department).child("SEM-"+semester);
                ll1.setVisibility(View.GONE);
                proBAR.setVisibility(View.VISIBLE);
                loadTable();
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
        btn_input = findViewById(R.id.btn_enter);
        btn_GPA = findViewById(R.id.btn_calGPA);
        tvNoDept = findViewById(R.id.tv_noDept);
        tvIncorrectList = findViewById(R.id.tv_noSubject);
        tableLayout = findViewById(R.id.tableLayout);


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


                TableRow headerRow = new TableRow(SubCodeActivity.this);
                addCellToRow(headerRow, "S.No", true);
                addCellToRow(headerRow, "Sub Code", true);
                addCellToRow(headerRow, "Credit", true);
                addCellToRow(headerRow, "GP", true);
                tableLayout.addView(headerRow);

                int serialNumber = 1;
                for (DataSnapshot child : snapshot.getChildren()) {
                    String subjectCode = child.child("CODE").getValue(String.class);
                    String credit = child.child("CREDIT").getValue(String.class);

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
                    tableRow.addView(gpaSpinner);

                    tableLayout.addView(tableRow);
                    serialNumber++;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SubCodeActivity.this, "Failed to load data.", Toast.LENGTH_SHORT).show();
            }
        });
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

    @Override
    public void onBackPressed() {
        if(ll2.getVisibility() == View.VISIBLE){
            ll1.setVisibility(View.VISIBLE);
            ll2.setVisibility(View.GONE);
        }else{
            super.onBackPressed();
        }
    }
}