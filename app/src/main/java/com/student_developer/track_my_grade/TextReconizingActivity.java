package com.student_developer.track_my_grade;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.TextView;
import android.widget.Toast;


import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import java.io.IOException;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextReconizingActivity extends BaseActivity {

    private static final int STORAGE_PERMISSION_CODE = 101;
    TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
    TextView recognizedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_text_reconizing);
        recognizedTextView = findViewById(R.id.tv_Textresult);

        requestStoragePermission();
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                chooseFile();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_MEDIA_IMAGES}, STORAGE_PERMISSION_CODE);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
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
                        if (isImageFile(fileUri)) {
                            startCropActivity(fileUri);
                        } else {
                            Toast.makeText(this, "Upload image only supports JPEG, PNG, JPG", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    private void chooseFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select Image"));
    }

    private boolean isImageFile(Uri uri) {
        String fileName = getFileName(uri);
        return fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png");
    }

    private void startCropActivity(Uri uri) {
        File croppedImageFile = new File(getCacheDir(), "cropped_image_" + System.currentTimeMillis() + ".jpg");
        Uri destinationUri = Uri.fromFile(croppedImageFile);

        UCrop.of(uri, destinationUri)
                .withMaxResultSize(1000, 1000)
                .start(this);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                getResultText(resultUri);
            }
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Toast.makeText(this, "Crop error: " + Objects.requireNonNull(UCrop.getError(data)).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void getResultText(Uri imageUri) {
        Bitmap processedBitmap = preprocessImage(imageUri);
        if (processedBitmap == null) {
            Toast.makeText(this, "Failed to preprocess image.", Toast.LENGTH_SHORT).show();
            return;
        }

        InputImage image = InputImage.fromBitmap(processedBitmap, 0);
        recognizer.process(image)
                .addOnSuccessListener(this::processTextRecognitionResult)
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to recognize text: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void processTextRecognitionResult(Text result) {
        List<String> crValues = new ArrayList<>();
        List<String> gpValues = new ArrayList<>();

        for (Text.TextBlock block : result.getTextBlocks()) {
            String blockText = block.getText();
            recognizedTextView.append("BlockText: " + blockText + "\n\n");

            Pattern crPattern = Pattern.compile("\\b\\d+\\b");
            Pattern gpPattern = Pattern.compile("\\b\\d+(\\.\\d{1,2})?\\b");

            Matcher crMatcher = crPattern.matcher(blockText);
            Matcher gpMatcher = gpPattern.matcher(blockText);

            while (crMatcher.find()) {
                crValues.add(crMatcher.group());
            }

            while (gpMatcher.find()) {
                gpValues.add(gpMatcher.group());
            }
        }

        String[] CR = crValues.toArray(new String[0]);
        String[] GP = gpValues.toArray(new String[0]);

        recognizedTextView.append("CR values: " + Arrays.toString(CR) + "\nGP values: " + Arrays.toString(GP));
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

    private Bitmap preprocessImage(Uri imageUri) {
        Bitmap originalBitmap;
        try {
            // Load the image as a Bitmap
            originalBitmap = InputImage.fromFilePath(this, imageUri).getBitmapInternal();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        // Convert to grayscale
        Bitmap grayscaleBitmap = Bitmap.createBitmap(
                originalBitmap.getWidth(),
                originalBitmap.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        for (int x = 0; x < originalBitmap.getWidth(); x++) {
            for (int y = 0; y < originalBitmap.getHeight(); y++) {
                int pixel = originalBitmap.getPixel(x, y);
                int grayValue = (int) (0.3 * Color.red(pixel) + 0.59 * Color.green(pixel) + 0.11 * Color.blue(pixel));
                int grayPixel = Color.rgb(grayValue, grayValue, grayValue);
                grayscaleBitmap.setPixel(x, y, grayPixel);
            }
        }

        // Increase contrast
        Bitmap contrastBitmap = Bitmap.createBitmap(
                grayscaleBitmap.getWidth(),
                grayscaleBitmap.getHeight(),
                Bitmap.Config.ARGB_8888
        );

        int contrastLevel = 50; // Adjust this value for more or less contrast
        double contrastFactor = (259 * (contrastLevel + 255)) / (255 * (259 - contrastLevel));

        for (int x = 0; x < grayscaleBitmap.getWidth(); x++) {
            for (int y = 0; y < grayscaleBitmap.getHeight(); y++) {
                int pixel = grayscaleBitmap.getPixel(x, y);
                int red = (int) (contrastFactor * (Color.red(pixel) - 128) + 128);
                int green = (int) (contrastFactor * (Color.green(pixel) - 128) + 128);
                int blue = (int) (contrastFactor * (Color.blue(pixel) - 128) + 128);

                red = Math.min(255, Math.max(0, red));
                green = Math.min(255, Math.max(0, green));
                blue = Math.min(255, Math.max(0, blue));

                int contrastPixel = Color.rgb(red, green, blue);
                contrastBitmap.setPixel(x, y, contrastPixel);
            }
        }

        return contrastBitmap;
    }


    @Override
    public void onBackPressed() {
        Utils.intend(this, CalculatorActivity.class);
    }
}
//which  OCR libraries is best option for my task, ass well as table structure