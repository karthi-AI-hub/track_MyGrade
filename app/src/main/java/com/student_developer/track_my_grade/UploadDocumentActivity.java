package com.student_developer.track_my_grade;

import static com.student_developer.track_my_grade.Utils.isNetworkConnected;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Objects;

public class UploadDocumentActivity extends AppCompatActivity {

    private LinearLayout layoutMarkSheets, layoutCertificates, layoutParticipations;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private String currentFolder;
    private String currentCustomFileName, rollNO;
    SharedPreferences sharedPref;
    private TextView btnAddMarksheet, btnAddCertificate, btnAddParticipation;
    private File trackMyGradeFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_document);

        layoutMarkSheets = findViewById(R.id.layout_marksheets);
        layoutCertificates = findViewById(R.id.layout_certificates);
        layoutParticipations = findViewById(R.id.layout_participations);


        sharedPref = this.getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        rollNO = sharedPref.getString("roll_no", null);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("Documents").child(rollNO);

        displayFiles("MarkSheets", layoutMarkSheets);
        displayFiles("Course Certificates", layoutCertificates);
        displayFiles("Outside Participations", layoutParticipations);

        btnAddMarksheet = findViewById(R.id.btn_add_marksheet);
        btnAddCertificate = findViewById(R.id.btn_add_certificate);
        btnAddParticipation = findViewById(R.id.btn_add_outsideParticipation);

        btnAddMarksheet.setOnClickListener(v -> {
            currentFolder = "MarkSheets";
            chooseFile();
        });
        btnAddCertificate.setOnClickListener(v -> {
            currentFolder = "Course Certificates";
            chooseFile();
        });
        btnAddParticipation.setOnClickListener(v -> {
            currentFolder = "Outside Participations";
            chooseFile();
        });

        trackMyGradeFolder = new File(getExternalFilesDir(null), "Track MyGrade");
        if (!trackMyGradeFolder.exists()) {
            trackMyGradeFolder.mkdir();
        }
    }

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) {
                        promptForFileName(fileUri);
                    }
                }
            });

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select File"));
    }


    private void promptForFileName(Uri fileUri) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter File Name");

        final EditText input = new EditText(this);
        input.setHint("\t\t\t\tFile name (e.g., Sem1 Marksheet)");
        input.setTextColor(getResources().getColor(R.color.black));
        input.setTextSize(16);
        input.setPadding(20, 20, 20, 20);
        input.setBackgroundResource(R.drawable.cell_background);

        int horizontalMarginInDp = 50;
        int horizontalMarginInPx = (int) (horizontalMarginInDp * getResources().getDisplayMetrics().density);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(horizontalMarginInPx, 10, horizontalMarginInPx, 0); // Horizontal margins only
        input.setLayoutParams(params);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String customFileName = input.getText().toString().trim();
            if (!customFileName.isEmpty()) {
                if (isImageFile(fileUri)) {
                    startCropActivity(fileUri, customFileName); // Start crop with custom resolution for images
                } else {
                    uploadFile(fileUri, customFileName); // Upload PDFs directly
                }
            } else {
                input.setBackgroundResource(R.drawable.edit_text_round_corner);
                Utils.Snackbar(findViewById(androidx.appcompat.R.id.content), "File name cannot be empty", "short");
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }



    private boolean isImageFile(Uri uri) {
        String fileName = getFileName(uri);
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png");
    }

    private boolean isPdfFile(Uri uri) {
        String fileName = getFileName(uri);
        return fileName.endsWith(".pdf");
    }

    private void startCropActivity(Uri uri, String customFileName) {
        this.currentCustomFileName = customFileName; // Store the custom file name
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), customFileName));

        UCrop.of(uri, destinationUri)
                .withAspectRatio(0, 0)
                .withMaxResultSize(1000, 1000) 
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null && currentCustomFileName != null) {
                uploadFile(resultUri, currentCustomFileName);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Toast.makeText(this, "Crop error: " + Objects.requireNonNull(UCrop.getError(data)).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadFile(Uri fileUri, String customFileName) {
        if (!isNetworkConnected(this)) {
            Toast.makeText(this, "No network connection available", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference fileRef = storageRef.child(currentFolder).child(customFileName);

        fileRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(this, "File uploaded successfully!", Toast.LENGTH_SHORT).show();
            displayUploadedFile(customFileName, currentFolder);
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void displayUploadedFile(String fileName, String folder) {
        LinearLayout targetLayout;
        switch (folder) {
            case "MarkSheets":
                targetLayout = layoutMarkSheets;
                break;
            case "Course Certificates":
                targetLayout = layoutCertificates;
                break;
            case "Outside Participations":
                targetLayout = layoutParticipations;
                break;
            default:
                return;
        }

        TextView fileTextView = new TextView(this);
        fileTextView.setText(fileName);
        fileTextView.setTextSize(16);
        fileTextView.setTextColor(getResources().getColor(R.color.black));
        fileTextView.setPadding(8, 8, 8, 8);
        targetLayout.addView(fileTextView);

        fileTextView.setOnClickListener(v -> showFileOptions(storageRef.child(folder).child(fileName)));
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

    private void displayFiles(String folderName, LinearLayout layout) {
        StorageReference folderRef = storageRef.child(folderName);

        folderRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference fileRef : listResult.getItems()) {
                addFileTextView(fileRef, layout);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load files: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void addFileTextView(StorageReference fileRef, LinearLayout layout) {
        TextView fileTextView = new TextView(this);
        fileTextView.setText(fileRef.getName());
        fileTextView.setTextSize(16);
        fileTextView.setTextColor(getResources().getColor(R.color.black));
        fileTextView.setPadding(8, 8, 8, 8);

        fileTextView.setOnClickListener(v -> showFileOptions(fileRef));

        layout.addView(fileTextView);
    }

    private void showFileOptions(StorageReference fileRef) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(fileRef.getName())
                .setItems(new String[]{"View", "Download"}, (dialog, which) -> {
                    if (which == 0) {
                        viewFile(fileRef);
                    } else {
                        downloadFile(fileRef);
                    }
                })
                .show();
    }

    private void viewFile(StorageReference fileRef) {
        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, getMimeType(uri)); // Set MIME type based on the file
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "No app available to view the file", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "View failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Helper method to determine MIME type based on the file extension
    private String getMimeType(Uri uri) {
        String mimeType = "*/*"; // Default MIME type
        String fileName = getFileName(uri);
        if (fileName.endsWith(".pdf")) {
            mimeType = "application/pdf";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            mimeType = "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            mimeType = "image/png";
        }
        return mimeType;
    }


    private void downloadFile(StorageReference fileRef) {
        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File trackMyGradeFolder = new File(downloadsDir, "Track MyGrade");
            if (!trackMyGradeFolder.exists()) {
                trackMyGradeFolder.mkdir(); // Create the folder if it doesn't exist
            }

            File destinationFile = new File(trackMyGradeFolder, fileRef.getName());

            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(uri);

            request.setDestinationUri(Uri.fromFile(destinationFile));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            downloadManager.enqueue(request);

            Toast.makeText(this, "Download started...", Toast.LENGTH_SHORT).show(); // Optional: Show a toast message
        }).addOnFailureListener(e -> Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }



    @Override
    public void onBackPressed() {
        Utils.intend(this, CalculatorActivity.class);
    }
}
