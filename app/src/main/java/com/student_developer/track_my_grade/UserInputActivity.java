package com.student_developer.track_my_grade;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yalantis.ucrop.UCrop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private SharedPreferences sharedPref;
    private String SaveToClg, rollNO , downloadUrl;
    private static final int REQUEST_CODE_SELECT_IMAGE = 101;
    private static final int REQUEST_CODE_READ_MEDIA_IMAGES = 102;
    private ImageView imgProfilePicture;
    private Button btnUploadPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_userinput);

        init();
        imgProfilePicture = findViewById(R.id.imgProfilePicture);
        btnUploadPicture = findViewById(R.id.btnUploadPicture);

        btnUploadPicture.setOnClickListener(v -> {
            checkAndRequestPermissions();
        });

        checkIfRollNumberExists();
    }

    private void init() {
        sharedPref = getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        rollNO = sharedPref.getString("roll_no", null).toUpperCase();

        database = FirebaseDatabase.getInstance("https://app1-ec550-default-rtdb.asia-southeast1.firebasedatabase.app/");

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

        DatabaseReference rootRef = database.getReference();
        rootRef.get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            btnSubmit.setVisibility(View.VISIBLE);

            if (task.isSuccessful() && task.getResult() != null) {
                boolean rollNoFound = false;

                for (DataSnapshot collegeSnapshot : task.getResult().getChildren()) {
                    String collegeName = collegeSnapshot.getKey();
                    if ("StudentList".equals(collegeName)) {
                        continue;
                    }
                    for (DataSnapshot deptSnapshot : collegeSnapshot.getChildren()) {
                        String departmentName = deptSnapshot.getKey();

                        if (deptSnapshot.hasChild(rollNO)) {
                            rollNoFound = true;
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("collegeName", collegeName);
                            editor.putString("departmentName", departmentName);
                            editor.apply();
                            Intent intent = new Intent(this, CalculatorActivity.class);
                            startActivity(intent);
                            finish();
                            break;
                        }
                    }
                    if (rollNoFound) break;
                }

                if (!rollNoFound) {
                    setOnclick();
                }
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
            validateAndSaveStudentInfo();
        });
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_CODE_READ_MEDIA_IMAGES);
            } else {
                openImageSelector();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_READ_MEDIA_IMAGES);
            } else {
                openImageSelector();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_READ_MEDIA_IMAGES) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openImageSelector();
            } else {
                Toast.makeText(this, "Storage permission is required to select an image.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Open the image selector
    private void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                startCrop(selectedImageUri);
            } else {
                Toast.makeText(this, "Error selecting image. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK && data != null) {
            Uri croppedImageUri = UCrop.getOutput(data);

            if (croppedImageUri != null) {
                imgProfilePicture.setImageURI(croppedImageUri);
                Uri compressedImageUri = compressImage(croppedImageUri);
                if (compressedImageUri != null) {
                    uploadProfilePicture(compressedImageUri);
                    progressBar.setVisibility(View.VISIBLE);
                    btnUploadPicture.setVisibility(View.GONE);
                } else {
                    Toast.makeText(this, "Error compressing image. Please try again.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Error processing image. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable cropError = UCrop.getError(data);
            Toast.makeText(this, "Error during image cropping: " + (cropError != null ? cropError.getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
        }
    }








    private void startCrop(@NonNull Uri uri) {
        String destinationFileName = "CroppedImage.jpg";
        UCrop.Options options = new UCrop.Options();
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setCompressionQuality(100);
        UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)))
                .withAspectRatio(1, 1)
                .withMaxResultSize(1080, 1080)
                .withOptions(options)
                .start(this);
    }


    private void uploadProfilePicture(Uri imageUri) {
        if (imageUri == null) return;

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("Profile/" + rollNO + ".jpg");
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    downloadUrl = uri.toString();
                    btnSubmit.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    btnUploadPicture.setVisibility(View.GONE);
                    Toast.makeText(UserInputActivity.this, "Profile picture uploaded successfully", Toast.LENGTH_SHORT).show();
                     }))
                .addOnFailureListener(e ->{ Toast.makeText(UserInputActivity.this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    btnUploadPicture.setVisibility(View.VISIBLE);
                });
    }


    private Uri compressImage(Uri uri) {
        try {

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            while (outputStream.toByteArray().length / 1024 > 5120) {
                outputStream.reset();
                quality -= 10;
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                if (quality <= 10) break;
            }
            byte[] imageData = outputStream.toByteArray();
            File tempFile = File.createTempFile("compressed_image", ".jpg", getCacheDir());
            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
            fileOutputStream.write(imageData);
            fileOutputStream.close();
            return Uri.fromFile(tempFile);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error compressing image", Toast.LENGTH_SHORT).show();
            return null;
        }
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

                    String dob = String.format("%02d-%s-%d", selectedDay, monthNames[selectedMonth], selectedYear);
                    etDob.setText(dob);
                },
                year, month, day
        );


        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }




    private void validateAndSaveStudentInfo() {
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


        if (TextUtils.isEmpty(dob) || dob.length() != 11) {
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

        if (department.equals("Department")) {
            spinnerDepartment.setBackgroundResource(R.drawable.edit_text_round_corner);
            valid = false;
        }

        if (valid) {
            if (downloadUrl == null) {
                Toast.makeText(this,"Upload Profile photo before submitting",Toast.LENGTH_SHORT).show();
                btnSubmit.setVisibility(View.GONE);
            } else {
                resetErrorStates();
                saveToFirebase(name, regNo, phoneNo, dob, sem, clg, department, downloadUrl);

            }
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
                                String dob, String sem, String clg, String department, String profileUrl) {

        Map<String, String> studentData = new HashMap<>();
        studentData.put("Name", name);
        studentData.put("RegNo", regNo);
        studentData.put("Dept", department);
        studentData.put("DOB", dob);
        studentData.put("SEM", sem);
        studentData.put("Profile", profileUrl);

        SharedPreferences.Editor editor =sharedPref.edit();
        editor.putInt("sem", Integer.parseInt(sem));
        editor.apply();

        if (phoneNo.startsWith("+91")) {
            studentData.put("PhNo", phoneNo.substring(3));
        } else {
            studentData.put("PhNo", phoneNo);
        }

        String ClgName = clg.toLowerCase();
        if (ClgName.contains("excel") ||
                ClgName.contains("excel engineering") ||
                ClgName.contains("excel enginerring college autonomous") ||
                ClgName.contains("excel enginerring college (autonomous)") ||
                ClgName.contains("excel enginerring college(autonomous)") ||
                ClgName.contains("excel enginerring college") ||
                ClgName.contains("excel engg college") ||
                ClgName.contains("eec") ||
                ClgName.contains("excel engg")) {
            studentData.put("Clg", "EXCEL ENGINEERING COLLEGE");
            SaveToClg = "Excel";
        }else{
            studentData.put("Clg", clg);
            SaveToClg = clg;
        }
        myRef = database.getReference(SaveToClg).child(department).child(rollNO);
        myRef.setValue(studentData)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    btnSubmit.setVisibility(View.VISIBLE);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Student information saved successfully", Toast.LENGTH_SHORT).show();
                        editor.putString("collegeName", SaveToClg);
                        editor.putString("departmentName", department);
                        editor.apply();
                        Intent intent = new Intent(this, CalculatorActivity.class);
                        startActivity(intent);
                        finish();
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
