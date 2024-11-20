package com.student_developer.track_my_grade;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class GetResultActivity extends AppCompatActivity {

    private SharedPreferences sharedPref;
    private WebView webView;
    private Button btnDownload, btnShare;
    private Uri pdfUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_get_result);
        btnDownload = findViewById(R.id.btnDownload);
        btnShare = findViewById(R.id.btnShare);
        initializeWebView();
        btnDownload.setOnClickListener(v -> saveWebViewAsPDF());
        btnShare.setOnClickListener(v -> {
            if (pdfUri != null) {
                shareResultFile(pdfUri);
            } else {
                Toast.makeText(this, "Please download the result first.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeWebView() {
        sharedPref = getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        String stuDob = sharedPref.getString("stuDoB", null);
        String stuRegNO = sharedPref.getString("stuRegNo", null);

        if (stuDob == null || stuRegNO == null) {
            return;
        }

        String CorrectstuDob = convertMonthToNumber(stuDob);

        System.out.println("stuRegNo : " + stuRegNO);
        System.out.println("stuDoB : " + stuDob);

        webView = findViewById(R.id.webView);
        webView.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams layoutParams = webView.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        webView.setLayoutParams(layoutParams);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportZoom(false);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        webView.getSettings().setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                if (url.contains("index.php")) {
                    view.loadUrl("javascript:document.getElementById('txtLoginId').value = '" + stuRegNO + "';" +
                            "document.getElementById('txtPassword').value = '" + CorrectstuDob + "';" +
                            "document.getElementsByTagName('input')[2].click();");
                    System.out.println("navigate aith click the button");
                }
            }
        });

        webView.loadUrl("http://103.105.40.112/students/");
    }

    private String convertMonthToNumber(String stuDob) {
        String[] months = {
                "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
                "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
        };
        String[] dateParts = stuDob.split("-");
        String day = dateParts[0];
        String month = dateParts[1].toUpperCase();
        String year = dateParts[2];

        int monthNumber = -1;
        for (int i = 0; i < months.length; i++) {
            if (months[i].equals(month)) {
                monthNumber = i + 1;
                break;
            }
        }

        if (monthNumber == -1) {
            return stuDob;
        }

        String formattedMonth = String.format("%02d", monthNumber);

        return day + "-" + formattedMonth + "-" + year;
    }

    private void saveWebViewAsPDF() {
        // Get the full content height and width of the WebView (scaled)
        int contentWidth = (int) (webView.getWidth() * webView.getScale());
        int contentHeight = (int) (webView.getContentHeight() * webView.getScale());

        // Define the A4 page size (595 x 842 points) for the PDF
        int pdfWidth = 595; // A4 width in points
        int pdfHeight = 842; // A4 height in points

        // Calculate the scaling factor to fit the full content into a single page
        float scaleX = (float) pdfWidth / contentWidth; // Scaling factor for width
        float scaleY = (float) pdfHeight / contentHeight; // Scaling factor for height
        float scaleFactor = Math.min(scaleX, scaleY); // Use the smaller scaling factor to fit within A4 size

        // Apply the scale factor
        int scaledWidth = (int) (contentWidth * scaleFactor);
        int scaledHeight = (int) (contentHeight * scaleFactor);

        // Create a PDF document
        PdfDocument pdfDocument = new PdfDocument();

        // Create a page with the scaled dimensions
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pdfWidth, pdfHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        // Get the canvas of the page
        Canvas canvas = page.getCanvas();
        canvas.scale(scaleFactor, scaleFactor); // Apply the scaling factor

        // Measure and draw the WebView content
        webView.measure(View.MeasureSpec.makeMeasureSpec(contentWidth, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(contentHeight, View.MeasureSpec.EXACTLY));
        webView.layout(0, 0, webView.getMeasuredWidth(), webView.getMeasuredHeight());
        webView.draw(canvas);

        // Finish the page
        pdfDocument.finishPage(page);

        // Save the PDF to the Downloads/Track MyGrade directory
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Downloads.DISPLAY_NAME, "ResultDocument.pdf");
        contentValues.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
        contentValues.put(MediaStore.Downloads.RELATIVE_PATH, "Download/Track MyGrade");

        Uri pdfUri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
        if (pdfUri == null) {
            Toast.makeText(this, "Failed to create file in Downloads", Toast.LENGTH_SHORT).show();
            return;
        }

        try (OutputStream outputStream = getContentResolver().openOutputStream(pdfUri)) {
            pdfDocument.writeTo(outputStream);
            Toast.makeText(this, "PDF saved successfully in Track MyGrade folder", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            pdfDocument.close();
        }
    }



    private void shareResultFile(Uri fileUri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out my result!");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share Result"));
    }



    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) {
            webView.onPause();
            webView.removeAllViews();
            webView.destroy();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (webView != null) {
            webView.removeAllViews();
            webView.destroy();
        }
    }
}
