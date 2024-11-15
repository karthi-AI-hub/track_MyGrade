package com.student_developer.track_my_grade;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
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
        PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
        PrintDocumentAdapter printAdapter = webView.createPrintDocumentAdapter("ResultDocument");

        File pdfFile = new File(getExternalFilesDir(null), "ResultDocument.pdf");
        pdfUri = Uri.fromFile(pdfFile);

        printManager.print("ResultDocument", new PrintDocumentAdapterWrapper(printAdapter, pdfFile),
                new PrintAttributes.Builder().build());
    }

    private class PrintDocumentAdapterWrapper extends PrintDocumentAdapter {
        private final PrintDocumentAdapter wrapped;
        private final File file;

        PrintDocumentAdapterWrapper(PrintDocumentAdapter wrapped, File file) {
            this.wrapped = wrapped;
            this.file = file;
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                             CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
            wrapped.onLayout(oldAttributes, newAttributes, cancellationSignal, callback, extras);
        }

        @Override
        public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                            CancellationSignal cancellationSignal, WriteResultCallback callback) {
            try (FileOutputStream out = new FileOutputStream(file);
                 ParcelFileDescriptor.AutoCloseInputStream in = new ParcelFileDescriptor.AutoCloseInputStream(destination)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

                callback.onWriteFinished(pages);
                Toast.makeText(GetResultActivity.this, "PDF saved successfully", Toast.LENGTH_SHORT).show();

            } catch (IOException e) {
                e.printStackTrace();
                callback.onWriteFailed(e.toString());
            }
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
