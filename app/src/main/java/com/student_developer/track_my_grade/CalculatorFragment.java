package com.student_developer.track_my_grade;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalculatorFragment extends Fragment {

    private TextView tv_gpa_result, tvGpa, tvNOCPorGP;
    private EditText etNoOfSubs, etsvToSem, etConfirmRoll;
    private Button btnGenerateSubs, btnsvToPro, btnsvToSem, btnConfirmRoll;
    private LinearLayout ll_no_of_sub;
    private ScrollView sv_containers;
    private LinearLayout ll_subjects_container;
    private LinearLayout ll_results;
    private LinearLayout ll_SvSem;
    private LinearLayout llconfirmRoll;
    private String rollno;
    private String rollNO;
    private int sem, TextViewIndex;
    private float gpa;
    private String noOfSubjects;
    private int numberOfSubjects, saveToSem;
    private ConnectivityManager.NetworkCallback networkCallback;
    private ConnectivityManager connectivityManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calculator, container, false);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        rollNO = sharedPref.getString("roll_no", "null");
        sem = sharedPref.getInt("current_sem", 1);
        TextViewIndex = sharedPref.getInt("TextViewIndex", 1);
        ((CalculatorActivity) requireActivity()).setFabVisibility(View.VISIBLE);

        FirebaseFirestore.setLoggingEnabled(true);

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
                tvCgpa.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
            }
            if (tvGraph != null) {
                tvGraph.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_600));
            }

            if (vProfile != null) {
                vProfile.setVisibility(View.GONE);
            }
            if (vCgpa != null) {
                vCgpa.setVisibility(View.VISIBLE);
            }
            if (vGraph != null) {
                vGraph.setVisibility(View.GONE);
            }
        }


        tv_gpa_result = view.findViewById(R.id.tv_gpa_res);
        etNoOfSubs = view.findViewById(R.id.et_no_of_subjects);
        etNoOfSubs.requestFocus();
        btnGenerateSubs = view.findViewById(R.id.btn_generate_subjects);
        ll_no_of_sub = view.findViewById(R.id.ll_no_of_subs);
        ll_subjects_container = view.findViewById(R.id.ll_subjects_container);
        ll_subjects_container.setVisibility(View.GONE);
        sv_containers = view.findViewById(R.id.sv_container);
        ll_results = view.findViewById(R.id.ll_result);
        ll_SvSem = view.findViewById(R.id.ll_sv_sem);
        btnsvToPro = view.findViewById(R.id.btn_svToPro);
        btnsvToSem = view.findViewById(R.id.btn_svToSem);
        etsvToSem = view.findViewById(R.id.et_svToSem);
        etsvToSem.setText(String.valueOf(TextViewIndex));
        llconfirmRoll = view.findViewById(R.id.ll_confirm_roll);
        llconfirmRoll.setVisibility(View.GONE);
        etConfirmRoll = view.findViewById(R.id.et_confirmRoll);
        btnConfirmRoll = view.findViewById(R.id.btn_confirmRoll);
        tvGpa = view.findViewById(R.id.tv_gpa);
        tvNOCPorGP = view.findViewById(R.id.tv_NoCRorGP);

        tvNOCPorGP.setOnClickListener(v -> {
            Utils.intend(getContext(), SubCodeActivity.class);
        });
        btnConfirmRoll.setOnClickListener((View v) -> {
            hideKeyboard(v);
            rollno = etConfirmRoll.getText().toString().trim().toUpperCase();

            if (TextUtils.isEmpty(rollno)) {
                etConfirmRoll.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                etConfirmRoll.requestFocus();
            } else if (!rollno.equals(rollNO)) {
                etConfirmRoll.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                etConfirmRoll.setError("Roll No does not match");
                etConfirmRoll.requestFocus();
            } else {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("Users")
                        .whereEqualTo("Roll No", rollno.toUpperCase())
                        .get()
                        .addOnCompleteListener(rollNoTask -> {
                            if (rollNoTask.isSuccessful() && rollNoTask.getResult() != null) {
                                if (!rollNoTask.getResult().isEmpty()) {
                                    DocumentSnapshot documentSnapshot = rollNoTask.getResult().getDocuments().get(0);
                                    String firestoreRollNo = documentSnapshot.getString("Roll No");
                                    if (firestoreRollNo != null && firestoreRollNo.equals(rollno.toUpperCase())) {
                                        llconfirmRoll.setVisibility(View.GONE);
                                        tvGpa.setText("CGPA RESULT (" + rollno.toUpperCase() + ")");
                                        ll_SvSem.setVisibility(View.VISIBLE);
                                    } else {
                                        etConfirmRoll.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                                        Toast.makeText(requireContext(), "Roll No does not match", Toast.LENGTH_SHORT).show();

                                    }
                                } else {
                                    etConfirmRoll.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                                    etConfirmRoll.setError("Authendication Failed, Enter your Roll No to Proceed.");
                                }
                            } else {
                                Toast.makeText(requireContext(), "Error Confirming Roll No", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(requireContext(), "Error Confirming Roll No", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        btnsvToSem.setOnClickListener((View v) -> {
            if (Utils.isNetworkAvailable(requireContext())) {
                hideKeyboard(v);
                String semesterInput = etsvToSem.getText().toString().trim();

                // Validate semester input
                if (TextUtils.isEmpty(semesterInput)) {
                    etsvToSem.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                    etsvToSem.requestFocus();
                    Toast.makeText(requireContext(), "Please enter a valid semester number.", Toast.LENGTH_SHORT).show();
                } else {
                    saveToSem = Integer.parseInt(semesterInput);

                    // Validate semester range
                    if (saveToSem > 0 && saveToSem <= sem) {
                        etsvToSem.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edittext_backgrouond));

                        // Validate GPA and Roll No
                        if (ValidateData(saveToSem, gpa, rollno)) {
                            try {
                                // Save GPA and subjects
                                saveGpa(saveToSem, gpa, rollno);
                                saveAllSubjects(saveToSem);
                            } catch (Exception e) {
                                Log.e("DatabaseError", "Error saving data: ", e);
                                FirebaseCrashlytics.getInstance().recordException(e);
                                Toast.makeText(requireContext(), "Failed to save GPA", Toast.LENGTH_SHORT).show();
                            }
                            tv_gpa_result.setText("  Your GPA is : " + String.format("%.2f", gpa) + " for Sem " + saveToSem + " saved successfully.");
                        }
                    } else {
                        etsvToSem.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                        etsvToSem.requestFocus();
                        Toast.makeText(requireContext(), "Not eligible to set GPA in SEM-" + saveToSem, Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                handleNoInternet();
            }
        });

        btnsvToPro.setOnClickListener(v -> {
            hideKeyboard(v);
            ll_subjects_container.setVisibility(View.GONE);
            btnsvToPro.setVisibility(View.GONE);
            ll_SvSem.setVisibility(View.GONE);
            llconfirmRoll.setVisibility(View.VISIBLE);
        });
        btnGenerateSubs.setOnClickListener(v -> {
            hideKeyboard(v);
            noOfSubjects = etNoOfSubs.getText().toString().trim();


            if (TextUtils.isEmpty(noOfSubjects)) {
                etNoOfSubs.setError("Please enter the number of subjects");
                etNoOfSubs.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                return;
            }

            try {
                numberOfSubjects = Integer.parseInt(noOfSubjects);
                if (numberOfSubjects <= 0 || numberOfSubjects > 11) {
                    etNoOfSubs.setError("Your input is not valid");
                    etNoOfSubs.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                    return;
                }

                ll_subjects_container.removeAllViews();
                ll_no_of_sub.setVisibility(View.GONE);
                ll_results.setVisibility(View.GONE);
                ll_subjects_container.setVisibility(View.VISIBLE);
                sv_containers.setVisibility(View.VISIBLE);

                addSubjectLabels();

                for (int i = 1; i <= numberOfSubjects; i++) {
                    createSubjectDetailView(i);
                }

                ((CalculatorActivity) requireActivity()).setFabVisibility(View.GONE);
                addCalculateButton();

            } catch (NumberFormatException e) {
                etNoOfSubs.setError("Please enter a valid number");
                etNoOfSubs.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
            }
        });
        return view;
    }


    private void createSubjectDetailView(int subjectNumber) {
        LinearLayout llSubjectDetail = new LinearLayout(requireContext());
        LinearLayout.LayoutParams llparam = new LinearLayout.LayoutParams(
                MATCH_PARENT, WRAP_CONTENT);
        llparam.setMargins(0,0,0,0);
        llSubjectDetail.setLayoutParams(llparam);
        llSubjectDetail.setPadding(40, 20, 40, 20);
        llSubjectDetail.setOrientation(LinearLayout.HORIZONTAL);


        EditText etSubjectName = new EditText(requireContext());


        etSubjectName.setId(View.generateViewId());
        etSubjectName.setTag("sub" + subjectNumber);

        LinearLayout.LayoutParams subjectNameParams = new LinearLayout.LayoutParams(
                0, WRAP_CONTENT,1f);
        subjectNameParams.setMargins(0,0,0,0);
        etSubjectName.setLayoutParams(subjectNameParams);
        etSubjectName.setHint("Subject " + subjectNumber);
        etSubjectName.setTextSize(16);
        etSubjectName.setPadding(20, 20, 0, 20);
        etSubjectName.setHintTextColor(ContextCompat.getColor(getContext(), R.color.gray));
        etSubjectName.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
        etSubjectName.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.cell_background));
        etSubjectName.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
         etSubjectName.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        // CR EditText
        EditText etCr = new EditText(requireContext());


        etCr.setId(View.generateViewId());
        etCr.setTag("cr" + subjectNumber);

        LinearLayout.LayoutParams crParams = new LinearLayout.LayoutParams(
                0, WRAP_CONTENT,1f);
        crParams.setMargins(0,0,0,0);
        etCr.setLayoutParams(crParams);
        etCr.setHint("Enter CR");
        etCr.setTextSize(16);
        etCr.setHintTextColor(ContextCompat.getColor(getContext(), R.color.gray));
        etCr.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
        etCr.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.cell_background));
        etCr.setInputType(InputType.TYPE_CLASS_NUMBER);
        etCr.setPadding(20, 20, 0, 20);
        etSubjectName.setHintTextColor(ContextCompat.getColor(getContext(), R.color.gray));
        etCr.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        EditText etGp = new EditText(requireContext());

        etGp.setId(View.generateViewId());
        etGp.setTag("gp" + subjectNumber);

        LinearLayout.LayoutParams gpParams = new LinearLayout.LayoutParams(
                0, WRAP_CONTENT,1f);
        gpParams.setMargins(0,0,0,0);
        etGp.setLayoutParams(gpParams);
        etGp.setHint("Enter GP");
        etGp.setTextSize(16);
        etGp.setHintTextColor(ContextCompat.getColor(getContext(), R.color.gray));
        etGp.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
        etGp.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.cell_background));
        etGp.setInputType(InputType.TYPE_CLASS_NUMBER);
        etGp.setPadding(20, 20, 0, 20);
        etGp.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        llSubjectDetail.addView(etSubjectName);
        llSubjectDetail.addView(etCr);
        llSubjectDetail.addView(etGp);

        ll_subjects_container.addView(llSubjectDetail);
    }

    private void addSubjectLabels() {
        TableRow headerRow = new TableRow(getContext());
        headerRow.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.light_violet, null));
        headerRow.setPadding(12, 12, 12, 12);

        String[] headers = {"Sub Name", "CR" , "GP"};
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
            params.setMargins(0,0,0,10);
            headerTitle.setLayoutParams(params);
            headerRow.addView(headerTitle);
        }
        ll_subjects_container.addView(headerRow);

    }

    private void addCalculateButton() {
        Button btnCalculate = new Button(requireContext());
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, WRAP_CONTENT);
        buttonLayoutParams.setMargins(0, 0, 0, 0);
        btnCalculate.setLayoutParams(buttonLayoutParams);
        btnCalculate.setText("Calculate GPA");
        btnCalculate.setTextSize(16);
        btnCalculate.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.blue_600));
        btnCalculate.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        btnCalculate.setPadding(20, 20, 20, 20);
        btnCalculate.setGravity(Gravity.CENTER);


        btnCalculate.setOnClickListener(v -> {
            if (isInputValid()) {
                hideKeyboard(v);
                calculate();
            }
        });

        ll_subjects_container.addView(btnCalculate);
    }

    private List<Subject> collectSubject() {
        List<Subject> subjects = new ArrayList<>();
        for (int i = 1; i <= numberOfSubjects; i++) {
            View subjectDetailView = ll_subjects_container.getChildAt(i);
            EditText etSubjectName = subjectDetailView.findViewWithTag("sub" + (i));
            EditText etCr = subjectDetailView.findViewWithTag("cr" + (i));
            EditText etGp = subjectDetailView.findViewWithTag("gp" + (i));

            String subjectName = etSubjectName.getText().toString();
            String credit = etCr.getText().toString();
            String gradePoint = etGp.getText().toString();

            subjects.add(new Subject(subjectName, credit, gradePoint));
        }
        return subjects;
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private boolean isInputValid() {
        for (int i = 0; i < ll_subjects_container.getChildCount(); i++) {
            View child = ll_subjects_container.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout subjectLayout = (LinearLayout) child;
                for (int j = 0; j < subjectLayout.getChildCount(); j++) {
                    View inputView = subjectLayout.getChildAt(j);
                    if (inputView instanceof EditText) {
                        EditText editText = (EditText) inputView;

                        if (TextUtils.isEmpty(editText.getText().toString().trim())) {
                            editText.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                            editText.requestFocus();
                            return false;
                        } else {
                            editText.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.cell_background));
                        }

                        if (editText.getTag().toString().startsWith("gp")) {
                            try {
                                float gpValue = Float.parseFloat(editText.getText().toString().trim());
                                if (gpValue > 10 || gpValue < 1) {
                                    Toast.makeText(requireContext(), "GP should be 0 to 10", Toast.LENGTH_SHORT).show();
                                    editText.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                                    editText.requestFocus();
                                    return false;
                                }
                            } catch (NumberFormatException e) {
                                Toast.makeText(requireContext(), "Please enter a valid number for GP", Toast.LENGTH_SHORT).show();
                                editText.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                                editText.requestFocus();
                                return false;
                            }
                        }

                        if (editText.getTag().toString().startsWith("cr")) {
                            try {
                                int crValue = Integer.parseInt(editText.getText().toString().trim());
                                if (crValue > 10 || crValue < 1) {
                                    Toast.makeText(requireContext(), "CR should be 0 to 10", Toast.LENGTH_SHORT).show();
                                    editText.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                                    editText.requestFocus();
                                    return false;
                                }
                            } catch (NumberFormatException e) {
                                Toast.makeText(requireContext(), "Please enter a valid number for CR", Toast.LENGTH_SHORT).show();
                                editText.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                                editText.requestFocus();
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private void calculate() {
        collectSubject();
        String[] subjectNames = new String[ll_subjects_container.getChildCount()];
        int[] creditHours = new int[ll_subjects_container.getChildCount()];
        float[] gradePoints = new float[ll_subjects_container.getChildCount()];

        for (int i = 0; i < ll_subjects_container.getChildCount(); i++) {
            View child = ll_subjects_container.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout subjectLayout = (LinearLayout) child;
                for (int j = 0; j < subjectLayout.getChildCount(); j++) {
                    View inputView = subjectLayout.getChildAt(j);
                    if (inputView instanceof EditText) {
                        EditText editText = (EditText) inputView;
                        if (j == 0) {
                            subjectNames[i] = editText.getText().toString().trim();
                        } else if (j == 1) {
                            creditHours[i] = Integer.parseInt(editText.getText().toString().trim());
                        } else if (j == 2) {
                            gradePoints[i] = Float.parseFloat(editText.getText().toString().trim());
                        }
                    }
                }
            }
        }

        ll_subjects_container.setVisibility(View.GONE);
        ll_results.setVisibility(View.VISIBLE);
        gpa = calculateCGPA(creditHours, gradePoints);
        gpa = Float.parseFloat(String.format("%.2f", gpa));
        gpa = Float.parseFloat(String.format(Locale.US, "%.2f", gpa));
        tv_gpa_result.setText("  Your GPA is : " + String.format("%.2f", gpa));
        System.out.println(gpa);

    }

    private float calculateCGPA(int[] creditHours, float[] gradePoints) {
        int totalCreditHours = 0;
        float totalGradePoints = 0;

        for (int i = 0; i < creditHours.length; i++) {
            totalCreditHours += creditHours[i];
            totalGradePoints += creditHours[i] * gradePoints[i];
        }
        return totalCreditHours > 0 ? totalGradePoints / totalCreditHours : 0;
    }

    private void saveGpa(int intsem, float gpa, String rollnoInput) {
        ll_SvSem.setVisibility(View.GONE);
        FirebaseCrashlytics.getInstance().setCustomKey("GPA", gpa);
        FirebaseCrashlytics.getInstance().setCustomKey("Semester", intsem);
        FirebaseCrashlytics.getInstance().setCustomKey("RollNo", rollnoInput);

        Log.d("SavingData", "Semester: " + intsem + ", GPA: " + gpa + ", RollNo: " + rollnoInput);

        String sem = String.valueOf(intsem);
        float roundedGpa = Math.round(gpa * 100) / 100.0f;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("Users").document(rollnoInput.toUpperCase());


        Log.d("DEBUG", "User Input Roll No: " + rollnoInput);

        String finalRollnoInput = rollnoInput;
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String rollNoFromDb = documentSnapshot.getString("Roll No");

                if (!finalRollnoInput.equals(rollNoFromDb)) {
                    Toast.makeText(requireContext(), "You can only save GPA for your own roll number.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> userData = new HashMap<>();
                userData.put("Sem " + sem, roundedGpa);
                System.out.println(roundedGpa);

                DocumentReference docRef = db.collection("GPA").document(finalRollnoInput);

                docRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            if (document != null && document.exists() && document.contains("Sem " + sem)) {
                                docRef.update(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Firestore", "GPA updated successfully");
                                            Toast.makeText(requireContext(), "GPA updated successfully", Toast.LENGTH_SHORT).show();
                                            navigateToProfileFragment();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(requireContext(), "Failed to update GPA", Toast.LENGTH_SHORT).show();
                                            handleFirestoreError(e, "Failed to update GPA");
                                        });
                            } else {
                                docRef.set(userData, SetOptions.merge())
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("Firestore", "GPA added successfully");
                                            Toast.makeText(requireContext(), "New semester GPA added successfully", Toast.LENGTH_SHORT).show();
                                            navigateToProfileFragment();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(requireContext(), "Failed to add new semester GPA", Toast.LENGTH_SHORT).show();
                                            handleFirestoreError(e, "Failed to save GPA");
                                        });
                            }
                        } else {
                            docRef.set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d("FirestoreSuccess", "GPA saved successfully for RollNo: " + finalRollnoInput);
                                        Toast.makeText(requireContext(), "GPA saved successfully", Toast.LENGTH_SHORT).show();
                                        navigateToProfileFragment();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FirestoreFailure", "Failed to save GPA for RollNo: " + finalRollnoInput, e);
                                        Toast.makeText(requireContext(), "Failed to save GPA", Toast.LENGTH_SHORT).show();
                                        handleFirestoreError(e, "Failed to save GPA");
                                    });
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to check GPA document", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error fetching GPA document", Toast.LENGTH_SHORT).show();
                    Log.e("ERROR", "Error fetching GPA document", e);
                    handleFirestoreError(e, "Error fetching GPA document");
                });
            } else {
                Toast.makeText(requireContext(), "Error fetching user data: Document does not exist", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> handleFirestoreError(e, "Error fetching user data"));
    }


    private void navigateToProfileFragment() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();

        transaction.replace(R.id.fragment_container, new ProfileFragment());
        transaction.addToBackStack(null);
        transaction.commitAllowingStateLoss();
    }


    public void saveAllSubjects(int saveToSem) {
        List<Subject> subjectList = collectSubject();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String semesterDocumentPath = "GPA/" + rollNO + "/Semester/SEM - " + saveToSem;

        db.document(semesterDocumentPath).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        db.document(semesterDocumentPath)
                                .update("subjects", subjectList)
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Subjects updated successfully"))
                                .addOnFailureListener(e -> handleFirestoreError(e, "Failed to update subjects"));
                    } else {
                        db.document(semesterDocumentPath)
                                .set(Collections.singletonMap("subjects", subjectList))
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Subjects saved successfully"))
                                .addOnFailureListener(e -> handleFirestoreError(e, "Failed to save subjects"));
                    }
                })
                .addOnFailureListener(e -> handleFirestoreError(e, "Failed to check subjects document"));    }
    private boolean ValidateData(int sem, float gpa, String rollno) {
        if (sem <= 0 || gpa <= 0 || TextUtils.isEmpty(rollno)) {
            Log.e("ValidationError", "Invalid data: Sem=" + sem + ", GPA=" + gpa + ", RollNo=" + rollno);
            FirebaseCrashlytics.getInstance().log("Validation Error: Sem=" + sem + ", GPA=" + gpa + ", RollNo=" + rollno);
            Toast.makeText(requireContext(), "Invalid data. Cannot save GPA.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    private void handleFirestoreError(Exception e, String message) {
        Log.e("FirestoreError", message, e);
        FirebaseCrashlytics.getInstance().recordException(e);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
    private void handleNoInternet() {
        Utils.Snackbar(requireView(), "No Internet. Waiting for connection...", "long");
        btnsvToSem.setEnabled(false);

        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                requireActivity().runOnUiThread(() -> {
                    btnsvToSem.setEnabled(true);
                    Utils.Snackbar(requireView(), "Network connected. Now submit your GPA", "long");
                });
            }

            @Override
            public void onLost(Network network) {
                requireActivity().runOnUiThread(() -> btnsvToSem.setEnabled(false));
            }
        };

        if (connectivityManager != null) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        }
    }


    public void onDestroyView() {
        super.onDestroyView();

        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }

    }
    @Override
    public void onResume() {
        super.onResume();
        ((CalculatorActivity) getActivity()).setFabVisibility(View.VISIBLE);
    }


}






