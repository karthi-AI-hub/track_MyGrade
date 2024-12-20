package com.student_developer.track_my_grade;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GraphFragment extends Fragment {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    SharedPreferences sharedPref;
    TextView tvGraph, tvSemSubject, tvNoData;
    String rollNO ;
    LinearLayout mainContainer;
    private ProgressBar proBar;
    private LineChart lineChart;
    private ConnectivityManager.NetworkCallback networkCallback;
    private ConnectivityManager connectivityManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_graph, container, false);

        sharedPref = requireActivity().getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        rollNO = sharedPref.getString("roll_no", null);

        proBar = view.findViewById(R.id.progressBar);
        mainContainer = view.findViewById(R.id.main_container);
        lineChart = view.findViewById(R.id.chart);
        tvGraph = view.findViewById(R.id.tv_Graph);
        tvSemSubject = view.findViewById(R.id.tv_SemDetails);
        tvNoData = view.findViewById(R.id.tv_noData);


        setupActivityViews();

        if(Utils.isNetworkAvailable(requireContext())) {
           loadData();
        } else {

            tvNoData.setVisibility(View.VISIBLE);
            proBar.setVisibility(View.VISIBLE);
            tvSemSubject.setVisibility(View.GONE);
            tvGraph.setVisibility(View.GONE);
            lineChart.setVisibility(View.GONE);
            tvGraph.setVisibility(View.GONE);

            Utils.Snackbar(requireActivity().findViewById(android.R.id.content), "No Internet . Waiting for connection...", "long");
            connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    requireActivity().runOnUiThread(() -> {
                        try {
                            Utils.Snackbar(view, "Network connected. Retrieving your data...", "long");
                            loadData();
                        } finally {
                            tvNoData.setVisibility(View.GONE);
                            proBar.setVisibility(View.GONE);
                            tvSemSubject.setVisibility(View.VISIBLE);
                            tvGraph.setVisibility(View.VISIBLE);
                            lineChart.setVisibility(View.VISIBLE);
                            tvGraph.setVisibility(View.VISIBLE);

                        }
                    });
                    connectivityManager.unregisterNetworkCallback(this);
                }

                @Override
                public void onLost(@NonNull Network network) {
                    requireActivity().runOnUiThread(() -> {
                        Utils.Snackbar(view, "Network lost...", "long");
                        tvNoData.setVisibility(View.VISIBLE);
                        proBar.setVisibility(View.VISIBLE);
                        tvGraph.setVisibility(View.GONE);
                        tvSemSubject.setVisibility(View.GONE);
                        lineChart.setVisibility(View.GONE);
                        tvGraph.setVisibility(View.GONE);
                    });
                }
            };

            if (connectivityManager != null) {
                connectivityManager.registerDefaultNetworkCallback(networkCallback);
            }
        }
        return view;
    }

    private  void loadData(){
        if (rollNO != null) {
            fetchGPAData(rollNO);
        } else {

            tvNoData.setVisibility(View.GONE);
            proBar.setVisibility(View.GONE);
            tvGraph.setVisibility(View.VISIBLE);
            lineChart.setVisibility(View.VISIBLE);
            tvSemSubject.setVisibility(View.VISIBLE);

        }

        loadSemesterData();
    }
    private void loadSemesterData() {
        DocumentReference rollDocRef = db.collection("GPA").document(rollNO);
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
            Utils.Snackbar(requireActivity().findViewById(android.R.id.content),
                    "Failed to load semester data.", "long");
        });
    }


    private void createSemesterTable(String semesterName, List<Map<String, Object>> subjects) {
        CardView semesterCardView = new CardView(getContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(8, 8, 8, 8);
        semesterCardView.setLayoutParams(cardParams);
        semesterCardView.setRadius(22f);
        semesterCardView.setUseCompatPadding(true);
        semesterCardView.setPadding(8, 8, 8, 8);
        semesterCardView.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.gradient_bf, null));


        TableLayout tableLayout = new TableLayout(getContext());
        tableLayout.setStretchAllColumns(true);
        tableLayout.setPadding(0, 0, 0, 0);
        tableLayout.setBackgroundColor(Color.TRANSPARENT);


        TableRow semesterRow = new TableRow(getContext());
        semesterRow.setPadding(10, 8, 10, 8);
        TextView semesterTitle = new TextView(getContext());
        semesterTitle.setText(semesterName);
        semesterTitle.setTextSize(18);
        semesterTitle.setTextColor(ResourcesCompat.getColor(getResources(), R.color.black, null));
        semesterTitle.setTypeface(Typeface.DEFAULT_BOLD);
        semesterTitle.setPadding(0, 20, 0, 8);
        semesterTitle.setGravity(Gravity.START);
        semesterRow.addView(semesterTitle);
        tableLayout.addView(semesterRow);

        TableRow headerRow = new TableRow(getContext());
        headerRow.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.light_violet, null));
        headerRow.setPadding(12, 12, 12, 12);
        String[] headers = {"Sub Name", "CR", "GP"};
        for (String headerText : headers) {
            TextView headerTitle = new TextView(getContext());
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
            TableRow tableRow = new TableRow(getContext());
            tableRow.setPadding(40, 20, 40, 20);
            tableRow.setBackground(ResourcesCompat.getDrawable(getResources(), R.color.lightGray, null));

            TextView subjectName = new TextView(getContext());
            subjectName.setText((String) subject.get("name"));
            subjectName.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            subjectName.setPadding(20, 20, 0, 20);
            subjectName.setGravity(Gravity.CENTER);
            subjectName.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.cell_background, null));
            subjectName.setTextColor(Color.BLACK);


            TextView credit = new TextView(getContext());
            credit.setText((String) subject.get("credits"));
            credit.setLayoutParams(new TableRow.LayoutParams(
                    0, TableRow.LayoutParams.WRAP_CONTENT, 1f));
            credit.setPadding(10, 20, 10, 20);
            credit.setGravity(Gravity.CENTER);
            credit.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.cell_background, null));
            credit.setTextColor(Color.BLACK);


            TextView gradePoint = new TextView(getContext());
            gradePoint.setText((String) subject.get("grade"));
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

    private void setupActivityViews() {
        if (getActivity() != null) {
            TextView tvActivityProfile = getActivity().findViewById(R.id.tv_profile);
            TextView tvCgpa = getActivity().findViewById(R.id.tv_cgpa);
            TextView tvGraph = getActivity().findViewById(R.id.tv_graph);
            View vProfile = getActivity().findViewById(R.id.v_profile);
            View vCgpa = getActivity().findViewById(R.id.v_cgpa);
            View vGraph = getActivity().findViewById(R.id.v_graph);

            if (tvActivityProfile != null) {
                tvActivityProfile.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_600));
            }
            if (tvCgpa != null) {
                tvCgpa.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_600));
            }
            if (tvGraph != null) {
                tvGraph.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
            }

            if (vProfile != null) {
                vProfile.setVisibility(View.GONE);
            }
            if (vCgpa != null) {
                vCgpa.setVisibility(View.GONE);
            }
            if (vGraph != null) {
                vGraph.setVisibility(View.VISIBLE);
            }
        }
    }

    private void fetchGPAData(String rollNO) {
        DocumentReference docRef = db.collection("GPA").document(rollNO);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Float[] gpas = new Float[8];
                for (int i = 1; i <= 8; i++) {
                    Double semGPA = documentSnapshot.getDouble("Sem " + i);
                    gpas[i - 1] = (semGPA != null) ? semGPA.floatValue() : null;
                }

                if (isAdded()) {
                    setupCharts(gpas);
                }


                tvNoData.setVisibility(View.GONE);
                proBar.setVisibility(View.GONE);
                tvGraph.setVisibility(View.VISIBLE);
                lineChart.setVisibility(View.VISIBLE);
                tvGraph.setVisibility(View.VISIBLE);

            } else {

                tvNoData.setVisibility(View.GONE);
                proBar.setVisibility(View.GONE);
                tvGraph.setVisibility(View.VISIBLE);
                lineChart.setVisibility(View.VISIBLE);
                tvGraph.setVisibility(View.VISIBLE);

            }
        });
    }

    private void setupCharts(Float[] gpas) {
        if (!isAdded()) {
            return;
        }

        LineDataSet lineDataSet = new LineDataSet(getLineChartData(gpas), "SEMESTER");
        customizeLineDataSet(lineDataSet);
        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        setupLineChart(lineChart);

        lineDataSet.setDrawFilled(true);
        lineDataSet.setFillColor(ContextCompat.getColor(requireContext(), R.color.blue_600));
        lineChart.invalidate();
    }

    private void customizeLineDataSet(LineDataSet lineDataSet) {
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setLineWidth(3f);
        lineDataSet.setCircleRadius(7f);
        lineDataSet.setDrawCircleHole(true);
        lineDataSet.setCircleHoleRadius(2.0f);
        lineDataSet.setValueTextSize(15f);
        lineDataSet.setColor(ContextCompat.getColor(requireContext(), R.color.blue_600));
        lineDataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
        lineDataSet.setCircleHoleColor(ContextCompat.getColor(requireContext(), R.color.white));
        lineDataSet.setDrawFilled(false);
        lineDataSet.setFillColor(ContextCompat.getColor(requireContext(), R.color.green));
        lineDataSet.setFillAlpha(40);
    }

    private List<Entry> getLineChartData(Float[] gpas) {
        ArrayList<Entry> dataValue = new ArrayList<>();
        for (int i = 0; i < gpas.length; i++) {
            if (gpas[i] != null) {
                dataValue.add(new Entry(i + 1, gpas[i]));
            }
        }
        return dataValue;
    }

    private void setupLineChart(LineChart lineChart) {
        lineChart.getDescription().setText("");
        lineChart.getDescription().setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
        lineChart.getDescription().setTextSize(12f);
        lineChart.animateX(1500);
        lineChart.animateY(1500);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        xAxis.setTextSize(16f);
        xAxis.setTypeface(Typeface.DEFAULT_BOLD);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawLabels(false);
        leftAxis.setDrawGridLines(false);
        leftAxis.setGranularity(0f);
        leftAxis.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        leftAxis.setTextSize(16f);
        leftAxis.setDrawGridLines(false);
        leftAxis.setTypeface(Typeface.DEFAULT_BOLD);

        lineChart.getAxisRight().setEnabled(false);

        Legend legend = lineChart.getLegend();
        legend.setEnabled(true);
        legend.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        legend.setTextSize(22f);
        legend.setTypeface(Typeface.DEFAULT_BOLD);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setForm(Legend.LegendForm.NONE);
    }


    public void onDestroyView() {
        super.onDestroyView();

        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }
    }


}
