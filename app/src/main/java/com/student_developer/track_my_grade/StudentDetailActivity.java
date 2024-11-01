package com.student_developer.track_my_grade;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentDetailActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseFirestore db;
    private LinearLayout[] semesterLayouts;
    LinearLayout mainContainer;
    private String rollNo, name;
    private int currentSemester;
    private Map<String, String> departmentNames;
    private TextView pro_name, pro_roll, pro_reg, pro_dob, pro_clg, pro_dept, pro_phno, pro_email, pro_cgpa;
    private TextView tvpro1, tvpro2, tvpro3, tvpro4, tvpro5, tvpro6, tvpro7, tvpro8, tvCGPATotal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student_detail);

        initUI();
        db = FirebaseFirestore.getInstance();

        rollNo = getIntent().getStringExtra("rollNo");
        setProText(pro_roll, rollNo);

        databaseReference = FirebaseDatabase.getInstance("https://app1-ec550-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Students").child(rollNo);

        fetchStudentDetails();
        loadGPAData();
        loadCredientData();
        loadSemesterData();
    }

    private void initUI() {
        mainContainer = findViewById(R.id.main_container);

        tvpro1 = findViewById(R.id.tv_pro1);
        tvpro2 = findViewById(R.id.tv_pro2);
        tvpro3 = findViewById(R.id.tv_pro3);
        tvpro4 = findViewById(R.id.tv_pro4);
        tvpro5 = findViewById(R.id.tv_pro5);
        tvpro6 = findViewById(R.id.tv_pro6);
        tvpro7 = findViewById(R.id.tv_pro7);
        tvpro8 = findViewById(R.id.tv_pro8);
        tvCGPATotal = findViewById(R.id.tvCgpaTotal);

        pro_name = findViewById(R.id.pro_name);
        pro_roll = findViewById(R.id.pro_roll);
        pro_reg = findViewById(R.id.pro_reg);
        pro_dob = findViewById(R.id.pro_dob);
        pro_email = findViewById(R.id.pro_email);
        pro_cgpa = findViewById(R.id.pro_cgpa);
        pro_clg = findViewById(R.id.pro_clg);
        pro_phno = findViewById(R.id.pro_phno);
        pro_dept = findViewById(R.id.pro_dept);

        initializeDepartmentNames();

        semesterLayouts = new LinearLayout[]{
                findViewById(R.id.llS1),
                findViewById(R.id.llS2),
                findViewById(R.id.llS3),
                findViewById(R.id.llS4),
                findViewById(R.id.llS5),
                findViewById(R.id.llS6),
                findViewById(R.id.llS7),
                findViewById(R.id.llS8)
        };
    }

    private void loadSemesterData() {
        DocumentReference rollDocRef = db.collection("GPA").document(rollNo);
        CollectionReference semesterCollection = rollDocRef.collection("Semester");

        semesterCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot semesterDocs = task.getResult();
                for (QueryDocumentSnapshot semesterDoc : semesterDocs) {
                    String semesterName = semesterDoc.getId();
                    List<Map<String, Object>> subjects = (List<Map<String, Object>>) semesterDoc.get("subjects");
                    createSemesterTable(semesterName, subjects);
                }
            }
        }).addOnFailureListener(e -> {
            Utils.Snackbar(findViewById(android.R.id.content),
                    "Failed to load semester data.", "long");
        });
    }

    private void setProText(TextView textView, String value) {
        textView.setText(value);
        if (textView == pro_clg && value.toLowerCase().contains("excel")) {
            pro_clg.setText("EXCEL ENGINEERING COLLEGE");
        }
    }

    private void fetchStudentDetails(){
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("Name").getValue(String.class).toUpperCase();
                    String regNo = dataSnapshot.child("RegNo").getValue(String.class).toUpperCase();
                    String clg = dataSnapshot.child("Clg").getValue(String.class).toUpperCase();
                    String dept = dataSnapshot.child("Dept").getValue(String.class);
                    String sem = dataSnapshot.child("SEM").getValue(String.class);
                    String dob = dataSnapshot.child("DOB").getValue(String.class).toUpperCase();
                    String phno = dataSnapshot.child("PhNo").getValue(String.class);

                    String fullDept = departmentNames.getOrDefault(dept, dept);
                    currentSemester = Integer.parseInt(sem);
                    removeExtraSemester(currentSemester);
                    setProText(pro_name, name.toUpperCase());
                    setProText(pro_reg, regNo.toUpperCase());
                    setProText(pro_clg, clg.toUpperCase());
                    setProText(pro_dob, dob.toUpperCase());
                    setProText(pro_phno, phno);
                    pro_dept.setText(fullDept + ", SEM-" + sem);

                } else {
                    Toast.makeText(StudentDetailActivity.this, "No Data Found.", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(StudentDetailActivity.this, "Error in Retriving Data.", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void loadGPAData() {
        DocumentReference documentRef = db.collection("GPA").document(rollNo);
        documentRef.get().addOnSuccessListener(this::processGPAData)
                .addOnFailureListener(e -> Log.e("StudentDetailActivity", "Error fetching GPA data", e));
    }

    private void loadCredientData() {
        DocumentReference documentRef = db.collection("Users").document(rollNo);
        documentRef.get().addOnSuccessListener(this::processCredientData)
                .addOnFailureListener(e -> Log.e("StudentDetailActivity", "Error fetching GPA data", e));
    }

    private void processGPAData(DocumentSnapshot documentSnapshot) {
        if (documentSnapshot.exists()) {
            Float[] gpas = new Float[]{
                    getGpaFromDocument(documentSnapshot, "Sem 1"),
                    getGpaFromDocument(documentSnapshot, "Sem 2"),
                    getGpaFromDocument(documentSnapshot, "Sem 3"),
                    getGpaFromDocument(documentSnapshot, "Sem 4"),
                    getGpaFromDocument(documentSnapshot, "Sem 5"),
                    getGpaFromDocument(documentSnapshot, "Sem 6"),
                    getGpaFromDocument(documentSnapshot, "Sem 7"),
                    getGpaFromDocument(documentSnapshot, "Sem 8")
            };

            TextView[] gpaTextViews = {tvpro1, tvpro2, tvpro3, tvpro4, tvpro5, tvpro6, tvpro7, tvpro8};
            for (int i = 0; i < gpas.length; i++) {
                if (gpas[i] != null) {
                    setGPAColorAndText(gpaTextViews[i], gpas[i]);
                }else{
                    setProText(gpaTextViews[i], "N/A");
                    gpaTextViews[i].setTextColor(getResources().getColor(R.color.gray));
                }
            }
        }
    }

    private void processCredientData(DocumentSnapshot documentSnapshot) {
        if (documentSnapshot.exists()) {

            String gmail = documentSnapshot.contains("Email") ? documentSnapshot.getString("Email") : null;
            setProText(pro_email, gmail);
            String cgpa = documentSnapshot.contains("CGPA") ? documentSnapshot.getString("CGPA") : null;
            setProText(pro_cgpa, cgpa);
        }
    }

    private Float getGpaFromDocument(DocumentSnapshot documentSnapshot, String key) {
        try {
            return documentSnapshot.contains(key) ? documentSnapshot.getDouble(key).floatValue() : null;
        } catch (Exception e) {
            Log.e("StudentDetailActivity", "Error parsing GPA for " + key, e);
            return null;
        }
    }
    private void setGPAColorAndText(TextView textView, Float gpa) {
        if (gpa == null) {
            textView.setText("N/A");
            textView.setTextColor(getResources().getColor(R.color.gray));
            return;
        }

        textView.setText(String.valueOf(gpa));
        if (gpa >= 7.5) {
            textView.setTextColor(getResources().getColor(R.color.green));
        } else if (gpa >= 5.0) {
            textView.setTextColor(getResources().getColor(R.color.orange));
        } else {
            textView.setTextColor(getResources().getColor(R.color.red));
        }

    }


    private void initializeDepartmentNames() {
        departmentNames = new HashMap<>();
        departmentNames.put("AIDS", "Artificial Intelligence & Data Science");
        departmentNames.put("AERO", "Aerospace Engineering");
        departmentNames.put("AGRI", "Agricultural Engineering");
        departmentNames.put("BME", "Bio Medical Engineering");
        departmentNames.put("CSE", "Computer Science and Engineering");
        departmentNames.put("CIVIL", "Civil Engineering");
        departmentNames.put("ECE", "Electronics and Communication Engineering");
        departmentNames.put("EEE", "Electrical and Electronics Engineering");
        departmentNames.put("IT", "Information Technology");
        departmentNames.put("FT", "Food Technology");
        departmentNames.put("MECH", "Mechanical Engineering");
        departmentNames.put("PCT", "Petroleum Chemical Technology");
        departmentNames.put("SF", "Safety & Fire");
    }


    private void removeExtraSemester(int currentSemester) {
        for (int i = 0; i < semesterLayouts.length; i++) {
            semesterLayouts[i].setVisibility(i < currentSemester ? View.VISIBLE : View.GONE);
        }
    }


    private void createSemesterTable(String semesterName, List<Map<String, Object>> subjects) {
        CardView semesterCardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(8, 8, 8, 8);
        semesterCardView.setLayoutParams(cardParams);
        semesterCardView.setRadius(22f);
        semesterCardView.setUseCompatPadding(true);
        semesterCardView.setPadding(8, 8, 8, 8);
        semesterCardView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.gradient_bf, null));


        TableLayout tableLayout = new TableLayout(this);
        tableLayout.setStretchAllColumns(true);
        tableLayout.setPadding(0, 0, 0, 0);
        tableLayout.setBackgroundColor(Color.TRANSPARENT);


        TableRow semesterRow = new TableRow(this);
        semesterRow.setPadding(10, 8, 10, 8);
        TextView semesterTitle = new TextView(this);
        semesterTitle.setText(semesterName);
        semesterTitle.setTextSize(18);
        semesterTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.black, null));
        semesterTitle.setTypeface(Typeface.DEFAULT_BOLD);
        semesterTitle.setPadding(0, 20, 0, 8);
        semesterTitle.setGravity(Gravity.START);
        semesterRow.addView(semesterTitle);
        tableLayout.addView(semesterRow);

        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.light_violet, null));
        headerRow.setPadding(12, 12, 12, 12);
        String[] headers = {"Sub Name", "CR", "GP"};
        for (String headerText : headers) {
            TextView headerTitle = new TextView(this);
            headerTitle.setText(headerText);
            headerTitle.setTextSize(16);
            headerTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null));
            headerTitle.setTypeface(Typeface.DEFAULT_BOLD);
            headerTitle.setPadding(0, 20, 0, 8);
            headerTitle.setGravity(Gravity.CENTER);
            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            headerTitle.setLayoutParams(params);
            headerRow.addView(headerTitle);
        }
        tableLayout.addView(headerRow);

        for (Map<String, Object> subject : subjects) {
            TableRow tableRow = new TableRow(this);
            tableRow.setPadding(40, 20, 40, 20);
            tableRow.setBackground(ResourcesCompat.getDrawable(getResources(), R.color.lightGray, null));

            TextView subjectName = new TextView(this);
            subjectName.setText((String) subject.get("subjectName"));
            subjectName.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            subjectName.setPadding(20, 20, 0, 20);
            subjectName.setGravity(Gravity.CENTER);
            subjectName.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.cell_background, null));
            subjectName.setTextColor(Color.BLACK);


            TextView credit = new TextView(this);
            credit.setText((String) subject.get("cr"));
            credit.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            credit.setPadding(10, 20, 10, 20);
            credit.setGravity(Gravity.CENTER);
            credit.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.cell_background, null));
            credit.setTextColor(Color.BLACK);


            TextView gradePoint = new TextView(this);
            gradePoint.setText((String) subject.get("gp"));
            gradePoint.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            gradePoint.setPadding(0, 20, 20, 20);
            gradePoint.setGravity(Gravity.CENTER);
            gradePoint.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.cell_background, null));
            gradePoint.setTextColor(Color.BLACK);


            tableRow.addView(subjectName);
            tableRow.addView(credit);
            tableRow.addView(gradePoint);

            tableLayout.addView(tableRow);
        }

        semesterCardView.addView(tableLayout);
        mainContainer.addView(semesterCardView);
    }

    @Override
    public void onBackPressed() {
        Utils.intend(this,StaffActivity.class);
    }
}
