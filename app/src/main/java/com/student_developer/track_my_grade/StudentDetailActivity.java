package com.student_developer.track_my_grade;

import static android.widget.Toast.LENGTH_SHORT;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.snackbar.Snackbar;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentDetailActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout[] semesterLayouts;
    private LinearLayout mainContainer, layoutMarkSheets;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private ProgressDialog progressDialog;
    private LineChart lineChart;
    private String rollNo, DBofClg;
    private int currentSemester;
    private Map<String, String> departmentNames;
    private ImageView pro_photo;
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
        DBofClg = getIntent().getStringExtra("SearchOnClg");
        setProText(pro_roll, rollNo);

        database = FirebaseDatabase.getInstance("https://app1-ec550-default-rtdb.asia-southeast1.firebasedatabase.app/");
        myRef = database.getReference(DBofClg);

        checkInternetAndProcess();

        pro_photo.setOnClickListener(v -> {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.full_image_view);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            ImageView fullImageView = dialog.findViewById(R.id.full_image_View);
            TextView tvClose = dialog.findViewById(R.id.tv_close);
            fullImageView.setImageDrawable(pro_photo.getDrawable());
            tvClose.setOnClickListener(v2 -> dialog.dismiss());

            dialog.show();
        });
    }

    private void checkInternetAndProcess() {

            fetchStudentDetails();
            loadGPAData();
            loadCredientData();
            loadSemesterData();
            displayFiles();
        }
    private void initUI() {
        mainContainer = findViewById(R.id.main_container);
        layoutMarkSheets = findViewById(R.id.layout_marksheets);

        lineChart = findViewById(R.id.chart);

        tvpro1 = findViewById(R.id.tv_pro1);
        tvpro2 = findViewById(R.id.tv_pro2);
        tvpro3 = findViewById(R.id.tv_pro3);
        tvpro4 = findViewById(R.id.tv_pro4);
        tvpro5 = findViewById(R.id.tv_pro5);
        tvpro6 = findViewById(R.id.tv_pro6);
        tvpro7 = findViewById(R.id.tv_pro7);
        tvpro8 = findViewById(R.id.tv_pro8);
        tvCGPATotal = findViewById(R.id.tvCgpaTotal);

        pro_photo = findViewById(R.id.pro_photo);
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

    private void fetchStudentDetails() {
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot collegeSnapshot) {
                boolean rollNoFound = false;

                for (DataSnapshot deptSnapshot : collegeSnapshot.getChildren()) {
                    if (deptSnapshot.hasChild(rollNo)) {
                        rollNoFound = true;

                        DataSnapshot rollNoSnapshot = deptSnapshot.child(rollNo);
                        String name = rollNoSnapshot.child("Name").getValue(String.class).toUpperCase();
                        String regNo = rollNoSnapshot.child("RegNo").getValue(String.class).toUpperCase();
                        String clg = rollNoSnapshot.child("Clg").getValue(String.class).toUpperCase();
                        String dept = deptSnapshot.getKey();
                        String sem = rollNoSnapshot.child("SEM").getValue(String.class);
                        String dob = rollNoSnapshot.child("DOB").getValue(String.class).toUpperCase();
                        String phno = rollNoSnapshot.child("PhNo").getValue(String.class);
                        String profileUrl = rollNoSnapshot.child("Profile").getValue(String.class);

                        if (profileUrl != null) {
                            new LoadImageFromURL(pro_photo).execute(profileUrl);
                        }

                        String fullDept = departmentNames.getOrDefault(dept, dept);
                        currentSemester = Integer.parseInt(sem);
                        removeExtraSemester(currentSemester);

                        setProText(pro_name, name);
                        setProText(pro_reg, regNo);
                        setProText(pro_clg, clg);
                        setProText(pro_dob, dob);
                        setProText(pro_phno, phno);
                        pro_dept.setText(fullDept + ", SEM-" + sem);
                        break;
                    }
                }

                if (!rollNoFound) {
                    Toast.makeText(getApplicationContext(), "Roll number not found in any department.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Error fetching data. Please try again later.", Toast.LENGTH_SHORT).show();
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


    private static class LoadImageFromURL extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

        public LoadImageFromURL(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            Bitmap bitmap = null;
            try {
                URL url = new URL(urlDisplay);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            } else {
                imageView.setImageResource(R.drawable.profile);
            }
        }
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

            List<Entry> gpaEntries = new ArrayList<>();
            String[] semesters = {"SEM 1", "SEM 2", "SEM 3", "SEM 4", "SEM 5", "SEM 6", "SEM 7", "SEM 8"};

            for (int i = 0; i < semesters.length; i++) {
                Float gpa = gpas[i];
                if (gpa != null && gpa != 0) {
                    gpaEntries.add(new Entry(i, gpa));
                }
            }

            updateLineChart(gpaEntries,semesters);

            TextView[] gpaTextViews = {tvpro1, tvpro2, tvpro3, tvpro4, tvpro5, tvpro6, tvpro7, tvpro8};
            for (int i = 0; i < gpas.length; i++) {
                setGPAColorAndText(gpaTextViews[i], gpas[i]);
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
        semesterCardView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.gradient_bf, null));

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
        String[] headers = {"S.No","Sub Name", "CR", "GP"};
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

        int sNo = 1;
        for (Map<String, Object> subject : subjects) {
            TableRow tableRow = new TableRow(this);
            tableRow.setPadding(40, 20, 40, 20);
            semesterCardView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.gradient_bf, null));

            TextView sno = new TextView(this);
            String sNO = sNo + ")";
            sno.setText(sNO);
            sno.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            sno.setPadding(20, 20, 0, 20);
            sno.setGravity(Gravity.CENTER);
            sno.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.cell_background, null));
            sno.setTextColor(Color.BLACK);

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

            tableRow.addView(sno);
            tableRow.addView(subjectName);
            tableRow.addView(credit);
            tableRow.addView(gradePoint);

            tableLayout.addView(tableRow);
            sNo++;
        }

        semesterCardView.addView(tableLayout);
        mainContainer.addView(semesterCardView);
    }


    private void updateLineChart(List<Entry> gpaEntries, String[] semesters) {
        LineDataSet lineDataSet = new LineDataSet(gpaEntries, "GPA over Semester");
        lineDataSet.setValueTextColor(Color.BLACK); // Set value text color
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleHoleRadius(2.0f);
        lineDataSet.setColor(Color.BLUE);
        lineDataSet.setCircleColor(ContextCompat.getColor(this, R.color.red));
        lineDataSet.setCircleHoleColor(ContextCompat.getColor(this, R.color.white));
        lineDataSet.setCircleRadius(5f);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        configureChartAppearance(semesters);

        lineChart.animateXY(1000, 1000);
        lineChart.invalidate();
    }


    private void configureChartAppearance(String[] semesters) {

        lineChart.getDescription().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // X-axis at the bottom
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(9f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(semesters));
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(semesters.length);
        xAxis.setTypeface(Typeface.DEFAULT_BOLD);

        // Configure Y-axis
        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setDrawGridLines(true);
        yAxis.setGranularity(0.5f);
        yAxis.setAxisMinimum(1f);
        yAxis.setAxisMaximum(10f);
        yAxis.setDrawLabels(true);


        Legend legend = lineChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(ContextCompat.getColor(this, R.color.black));
        legend.setTextSize(14f);
        legend.setTypeface(Typeface.DEFAULT_BOLD);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setForm(Legend.LegendForm.LINE);
    }

    private void displayFiles() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("Documents").child(rollNo).child("MarkSheets");
        storageRef.listAll().addOnSuccessListener(listResult -> {
            int count=1;
            for (StorageReference fileRef : listResult.getItems()) {
                addFileTextView(fileRef, layoutMarkSheets, count);
                count ++;
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load files: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void addFileTextView(StorageReference fileRef, LinearLayout layout, int count) {
        String fName = count +") "+fileRef.getName();
        TextView fileTextView = new TextView(this);
        fileTextView.setText(fName);
        fileTextView.setTextSize(18);
        fileTextView.setTextColor(getResources().getColor(R.color.black));
        fileTextView.setPadding(8, 8, 8, 8);

        fileTextView.setOnClickListener(v -> showFileOptions(fileRef));

        layout.addView(fileTextView);
    }
    private void showFileOptions(StorageReference fileRef) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(fileRef.getName())
                .setItems(new String[]{"View", "Download","Share"}, (dialog, which) -> {
                    if (which == 0) {
                        viewFile(fileRef);
                    } else if (which==1) {
                        downloadFile(fileRef);
                    }else{
                        shareFile(fileRef);
                    }
                })
                .show();
    }

    private void shareFile(StorageReference fileRef) {
        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            File localFile = new File(getExternalFilesDir(null), fileRef.getName());
            fileRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                shareDownloadedFile(localFile);
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Download for sharing failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to get download URL for sharing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void shareDownloadedFile(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType(getMimeType(Uri.fromFile(file)));
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share file via"));
    }

    private void viewFile(StorageReference fileRef) {
        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            File localFile = new File(getExternalFilesDir(null), fileRef.getName());
            fileRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                openDownloadedFile(localFile);
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void openDownloadedFile(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, getMimeType(uri));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "No app available to view the file", Toast.LENGTH_SHORT).show();
        }
    }


    private String getMimeType(Uri uri) {
        String mimeType = "*/*";
        String fileName = getFileName(uri);

        if (fileName.endsWith(".pdf")) {
            mimeType = "application/pdf";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            mimeType = "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            mimeType = "image/png";
        } else if (fileName.endsWith(".txt")) {
            mimeType = "text/plain";
        } else if (fileName.endsWith(".doc") || fileName.endsWith(".docx")) {
            mimeType = "application/msword";
        } else if (fileName.endsWith(".xls") || fileName.endsWith(".xlsx")) {
            mimeType = "application/vnd.ms-excel";
        } else if (fileName.endsWith(".csv")) {
            mimeType = "text/csv";
        } else if (fileName.endsWith(".zip")) {
            mimeType = "application/zip";
        } else if (fileName.endsWith(".rar")) {
            mimeType = "application/x-rar-compressed";
        } else if (fileName.endsWith(".7z")) {
            mimeType = "application/x-7z-compressed";
        } else if (fileName.endsWith(".mp4")) {
            mimeType = "video/mp4";
        } else if (fileName.endsWith(".mp3")) {
            mimeType = "audio/mpeg";
        } else if (fileName.endsWith(".ppt") || fileName.endsWith(".pptx")) {
            mimeType = "application/vnd.ms-powerpoint";
        } else if (fileName.endsWith(".apk")) {
            mimeType = "application/vnd.android.package-archive";
        } else if (fileName.endsWith(".gif")) {
            mimeType = "image/gif";
        } else if (fileName.endsWith(".html")) {
            mimeType = "text/html";
        } else if (fileName.endsWith(".xml")) {
            mimeType = "application/xml";
        } else if (fileName.endsWith(".tar")) {
            mimeType = "application/x-tar";
        } else if (fileName.endsWith(".avi")) {
            mimeType = "video/x-msvideo";
        } else if (fileName.endsWith(".flv")) {
            mimeType = "video/x-flv";
        } else if (fileName.endsWith(".mkv")) {
            mimeType = "video/x-matroska";
        } else if (fileName.endsWith(".mov")) {
            mimeType = "video/quicktime";
        } else if (fileName.endsWith(".wav")) {
            mimeType = "audio/x-wav";
        } else if (fileName.endsWith(".ogg")) {
            mimeType = "audio/ogg";
        } else if (fileName.endsWith(".json")) {
            mimeType = "application/json";
        }

        return mimeType;
    }

    private String getFileName(Uri uri) {
        String fileName = "unknown_file";
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
            }
        }
        return fileName;
    }

    private void downloadFile(StorageReference fileRef) {
        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            File downloadsDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Track MyGrade");
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            File destinationFile = new File(downloadsDir, fileRef.getName());

            DownloadManager.Request request = new DownloadManager.Request(uri)
                    .setTitle(fileRef.getName() + " - Downloading")
                    .setDescription("Downloading file...")
                    .setDestinationUri(Uri.fromFile(destinationFile))
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);

            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            long downloadId = downloadManager.enqueue(request);

            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Downloading " + fileRef.getName());
            progressDialog.setMessage("Please wait...");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.show();

            new Thread(() -> {
                boolean downloading = true;
                while (downloading) {
                    DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
                    try (Cursor cursor = downloadManager.query(query)) {
                        if (cursor != null && cursor.moveToFirst()) {
                            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                            switch (status) {
                                case DownloadManager.STATUS_SUCCESSFUL:
                                    downloading = false;
                                    break;
                                case DownloadManager.STATUS_FAILED:
                                    onDownloadFailed();
                                    downloading = false;
                                    break;
                                case DownloadManager.STATUS_RUNNING:
                                    updateDownloadProgress(cursor);
                                    break;
                                default:
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        onDownloadFailed();
                        downloading = false;
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                onDownloadComplete();
            }).start();

            showToast("Download Started");
        }).addOnFailureListener(e -> {
            showToast("Download failed: " + e.getMessage());
            dismissProgressDialog();
        });
    }

    private void updateDownloadProgress(Cursor cursor) {
        int bytesDownloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
        int totalBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

        if (totalBytes > 0) {
            int progress = (int) ((bytesDownloaded * 100L) / totalBytes);
            runOnUiThread(() -> progressDialog.setProgress(progress));
        }
    }

    private void onDownloadFailed() {
        runOnUiThread(() -> {
            dismissProgressDialog();
            showToast("Download failed!");
        });
    }

    private void onDownloadComplete() {
        runOnUiThread(() -> {
            dismissProgressDialog();
            showToast("Download completed!");
        });
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public void showToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


@Override
    public void onBackPressed() {
        Utils.intend(this,StaffActivity.class);
        finish();
    }
}
