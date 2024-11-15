package com.student_developer.track_my_grade;

import static com.student_developer.track_my_grade.Utils.isNetworkConnected;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class UploadDocumentActivity extends AppCompatActivity {


    private static final int STORAGE_PERMISSION_CODE = 101;
    private long downloadId;
    private ProgressDialog progressDialog;
    private LinearLayout layoutMarkSheets, layoutDocuments ;
    private FirebaseStorage storage;
    private StorageReference storageRef;
    private AtomicInteger markSheetsCount, documentsCount;
    private String currentFolder;
    private String currentCustomFileName, rollNO;
    private SharedPreferences sharedPref;
    private TextView btnAddMarksheet, btnAdddocuments;
    private File trackMyGradeFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_document);

        layoutMarkSheets = findViewById(R.id.layout_marksheets);
        layoutDocuments = findViewById(R.id.layout_documents);
        markSheetsCount = new AtomicInteger(1);
        documentsCount = new AtomicInteger(1);

        sharedPref = this.getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        rollNO = sharedPref.getString("roll_no", null);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("Documents").child(rollNO);

        displayFiles("MarkSheets", layoutMarkSheets, markSheetsCount);
        displayFiles("Documents", layoutDocuments, documentsCount);

        btnAddMarksheet = findViewById(R.id.btn_add_marksheet);
        btnAdddocuments = findViewById(R.id.btn_add_documents);

        btnAddMarksheet.setOnClickListener(v -> {
            currentFolder = "MarkSheets";
            requestStoragePermission();
        });
        btnAdddocuments.setOnClickListener(v -> {
            currentFolder = "Documents";
            requestStoragePermission();
        });

        trackMyGradeFolder = new File(getExternalFilesDir(null), "Track MyGrade");
        if (!trackMyGradeFolder.exists()) {
            trackMyGradeFolder.mkdir();
        }
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                chooseFile();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                chooseFile();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                chooseFile();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
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
                String fileExtension = getFileExtension(fileUri);  // Get the correct file extension
                String fileNameWithExtension = customFileName + fileExtension;  // Append the extension

                if (isImageFile(fileUri)) {
                    startCropActivity(fileUri, fileNameWithExtension); // Start crop with custom resolution for images
                } else if (isPdfFile(fileUri)){
                    uploadFile(fileUri, fileNameWithExtension); // Upload PDFs directly with extension
                }else{
                    uploadFile(fileUri,fileNameWithExtension);
                }
            } else {
                input.setBackgroundResource(R.drawable.edit_text_round_corner);
                Toast.makeText(this, "File name cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
private String getFileExtension(Uri fileUri) {
        String mimeType = getContentResolver().getType(fileUri);

        if (mimeType == null) {
             String fileName = getFileName(fileUri);
            if (fileName != null && fileName.contains(".")) {
                return fileName.substring(fileName.lastIndexOf("."));
            } else {
                return ".unknown";        }
        }

        switch (mimeType) {
            case "application/pdf":
                return ".pdf";
            case "image/jpeg":
                return ".jpg";
            case "image/png":
                return ".png";
            case "application/msword":
                return ".doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                return ".docx";
            case "application/vnd.ms-excel":
                return ".xls";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                return ".xlsx";
            case "text/csv":
                return ".csv";
            case "text/plain":
                return ".txt";
            case "application/vnd.ms-powerpoint":
                return ".ppt";
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                return ".pptx";
            case "application/zip":
                return ".zip";
            case "application/x-rar-compressed":
                return ".rar";
            case "application/x-7z-compressed":
                return ".7z";
            case "application/json":
                return ".json";
            case "application/xml":
                return ".xml";
            case "application/vnd.android.package-archive":
                return ".apk";
            case "video/mp4":
                return ".mp4";
            case "audio/mpeg":
                return ".mp3";
            case "image/gif":
                return ".gif";
            case "image/bmp":
                return ".bmp";
            case "application/x-tar":
                return ".tar";
            case "application/x-gtar":
                return ".gtar";
            case "application/x-iso9660-image":
                return ".iso";
            case "application/x-compressed":
            case "application/gzip":
                return ".gz";
            default:
                return "";
        }
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
        this.currentCustomFileName = customFileName;
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

    private void uploadFile(Uri fileUri, String fileNameWithExtension) {
        if (!isNetworkConnected(this)) {
            showToast("No network connection available");
            return;
        }

        if (fileUri == null) {
            showToast("File URI is null!");
            return;
        }

        long fileSize = getFileSize(fileUri);
        final long MAX_FILE_SIZE = 20 * 1024 * 1024;

        if (fileSize > MAX_FILE_SIZE) {
            showToast("File size exceeds 20 MB limit!");
            return;
        }

        StorageReference fileRef = storageRef.child(currentFolder).child(fileNameWithExtension);

        showProgressDialog("Uploading...", "Please wait while the file is being uploaded...");
        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> handleUploadSuccess(fileNameWithExtension))
                .addOnFailureListener(this::handleUploadFailure);
    }

    private long getFileSize(Uri fileUri) {
        long fileSize = 0;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(fileUri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (sizeIndex != -1) {
                    fileSize = cursor.getLong(sizeIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return fileSize;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showProgressDialog(String title, String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }
        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.show();
    }

    private void handleUploadSuccess(String fileNameWithExtension) {
        showToast("File uploaded successfully!");
        displayUploadedFile(fileNameWithExtension, currentFolder);
        dismissProgressDialog();
    }

    private void handleUploadFailure(Exception e) {
        showToast("Upload failed: " + e.getMessage());
        dismissProgressDialog();
    }


    private void displayUploadedFile(String fileName, String folder) {
        LinearLayout targetLayout;
        switch (folder) {
            case "MarkSheets":
                targetLayout = layoutMarkSheets;
                break;
            case "Documents":
                targetLayout = layoutDocuments;
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

    private void displayFiles(String folderName, LinearLayout layout, AtomicInteger couNt) {
        StorageReference folderRef = storageRef.child(folderName);
        folderRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference fileRef : listResult.getItems()) {
                addFileTextView(fileRef, layout, couNt);
                couNt.getAndIncrement();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load files: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void addFileTextView(StorageReference fileRef, LinearLayout layout, AtomicInteger counT) {
        TextView fileTextView = new TextView(this);
        String fName = counT.get() + ") " + (fileRef.getName());
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
            downloadId = downloadManager.enqueue(request);

            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Downloading " + fileRef.getName());
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();

            new Thread(() -> {
                boolean downloading = true;
                while (downloading) {
                    try (Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(downloadId))) {
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
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                onDownloadComplete();
            }).start();

            Toast.makeText(this, "Download started...", Toast.LENGTH_SHORT).show();

        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Download failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "Download failed!", Toast.LENGTH_SHORT).show();
        });
    }

    private void onDownloadComplete() {
        runOnUiThread(() -> {
            dismissProgressDialog();
            Toast.makeText(this, "Download completed!", Toast.LENGTH_SHORT).show();
        });
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

}
