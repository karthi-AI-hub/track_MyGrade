package com.student_developer.track_my_grade;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.DisplayMetrics;
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

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculatorFragment extends Fragment {

    // Existing member variables

    private FirebaseFirestore firestore;
    TextView tv_gpa_result, tvGpa;
    EditText etNoOfSubs, etsvToSem, etConfirmRoll;
    Button btnGenerateSubs, btnsvToPro, btnsvToSem, btnConfirmRoll;
    LinearLayout ll_no_of_sub;
    ScrollView sv_containers;
    LinearLayout ll_subjects_container;
    LinearLayout ll_results;
    LinearLayout ll_SvSem;
    LinearLayout llconfirmRoll;
    String rollno;
    String rollNO;
    int sem;
    float gpa;

    String noOfSubjects;
    int numberOfSubjects, saveToSem;
    List<List<Subject>> allSemesters;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calculator, container, false);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        rollNO = sharedPref.getString("roll_no", null);
        sem = sharedPref.getInt("current_sem", 1);

        FirebaseFirestore.setLoggingEnabled(true);

        if (getActivity() != null) {
            // Accessing TextViews and Views in the activity layout
            TextView tvActivityProfile = getActivity().findViewById(R.id.tv_profile);
            TextView tvCgpa = getActivity().findViewById(R.id.tv_cgpa);
            TextView tvGraph = getActivity().findViewById(R.id.tv_graph);
            View vProfile = getActivity().findViewById(R.id.v_profile);
            View vCgpa = getActivity().findViewById(R.id.v_cgpa);
            View vGraph = getActivity().findViewById(R.id.v_graph);

            // Change the text color of the TextViews in the activity
            if (tvActivityProfile != null) {
                tvActivityProfile.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_600));
            }
            if (tvCgpa != null) {
                tvCgpa.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
            }
            if (tvGraph != null) {
                tvGraph.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_600));
            }

            // Set visibility for specific views
            if (vProfile != null) {
                vProfile.setVisibility(View.GONE); // Show profile view
            }
            if (vCgpa != null) {
                vCgpa.setVisibility(View.VISIBLE); // Hide CGPA view
            }
            if (vGraph != null) {
                vGraph.setVisibility(View.GONE); // Hide graph view
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
        llconfirmRoll = view.findViewById(R.id.ll_confirm_roll);
        llconfirmRoll.setVisibility(View.GONE);
        etConfirmRoll = view.findViewById(R.id.et_confirmRoll);
        btnConfirmRoll = view.findViewById(R.id.btn_confirmRoll);
        tvGpa = view.findViewById(R.id.tv_gpa);

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
            } else if (rollno.length() < 7 || rollno.length() > 9) {
                etConfirmRoll.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                etConfirmRoll.requestFocus();
            } else {
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                // Check if a document with the roll number exists in the 'Users' collection
                db.collection("Users")
                        .whereEqualTo("Roll No", rollno.toUpperCase())
                        .get()
                        .addOnCompleteListener(rollNoTask -> {
                            if (rollNoTask.isSuccessful() && rollNoTask.getResult() != null) {
                                if (!rollNoTask.getResult().isEmpty()) {
                                    // Document exists, now check the Roll No field
                                    DocumentSnapshot documentSnapshot = rollNoTask.getResult().getDocuments().get(0);
                                    String firestoreRollNo = documentSnapshot.getString("Roll No");
                                    if (firestoreRollNo != null && firestoreRollNo.equals(rollno.toUpperCase())) {
                                        // Hide the roll input and show the semester save layout
                                        llconfirmRoll.setVisibility(View.GONE);
                                        tvGpa.setText("CGPA RESULT (" + rollno.toUpperCase() + ")");
                                        ll_SvSem.setVisibility(View.VISIBLE);
                                    } else {
                                        // If the 'Roll No' doesn't match
                                        etConfirmRoll.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                                        Toast.makeText(requireContext(), "Roll No does not match", Toast.LENGTH_SHORT).show();

                                    }
                                } else {
                                    // If no document with that Roll No exists
                                    etConfirmRoll.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                                    etConfirmRoll.setError("Authendication Failed, Enter your Roll No to Proceed.");
                                }
                            } else {
                                // Task was not successful
                                Toast.makeText(requireContext(), "Error Confirming Roll No", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Handle any failures (e.g., network error)
                            Toast.makeText(requireContext(), "Error Confirming Roll No", Toast.LENGTH_SHORT).show();
                        });
            }
        });


        btnsvToSem.setOnClickListener((View v) -> {
            hideKeyboard(v);
            String semesterInput = etsvToSem.getText().toString().trim();
            if (TextUtils.isEmpty(semesterInput)) {
                etsvToSem.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                etsvToSem.requestFocus();
            } else {

                saveToSem = Integer.parseInt(semesterInput);

                if (saveToSem > 0 && saveToSem <= sem) {
                    etsvToSem.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edittext_backgrouond));
                    saveGpa(saveToSem, gpa, rollno);
                    saveAllSubjects(saveToSem);
                    tv_gpa_result.setText("  Your GPA is : " + String.format("%.2f", gpa) + " for Sem " + saveToSem + " saved successfully.");
                } else {
                    etsvToSem.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                    etsvToSem.requestFocus();
                    Toast.makeText(requireContext(), "Not eligible to set GPA in SEM-" + saveToSem, Toast.LENGTH_SHORT).show();
                }

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

        // GP EditText
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

        // Add EditTexts to the LinearLayout
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

    private int convertDpToPx(int dp) {
        return Math.round(dp * (requireContext().getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
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

                        // Check if the input is empty
                        if (TextUtils.isEmpty(editText.getText().toString().trim())) {
                            editText.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                            editText.requestFocus();
                            return false; // Return false if any EditText is empty
                        } else {
                            editText.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.cell_background));
                        }

                        // Validate GP (should be 10 or less)
                        if (editText.getTag().toString().startsWith("gp")) {
                            try {
                                float gpValue = Float.parseFloat(editText.getText().toString().trim());
                                if (gpValue > 10 || gpValue < 1) {
                                    Toast.makeText(requireContext(), "GP should be 10 or less", Toast.LENGTH_SHORT).show();
                                    editText.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                                    editText.requestFocus();
                                    return false; // Return false if GP is more than 10
                                }
                            } catch (NumberFormatException e) {
                                Toast.makeText(requireContext(), "Please enter a valid number for GP", Toast.LENGTH_SHORT).show();
                                editText.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                                editText.requestFocus();
                                return false; // Return false if GP input is not a number
                            }
                        }

                        // Validate CR (should be 10 or less)
                        if (editText.getTag().toString().startsWith("cr")) {
                            try {
                                int crValue = Integer.parseInt(editText.getText().toString().trim());
                                if (crValue > 10 || crValue < 1) {
                                    Toast.makeText(requireContext(), "CR should be 10 or less", Toast.LENGTH_SHORT).show();
                                    editText.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                                    editText.requestFocus();
                                    return false;
                                }
                            } catch (NumberFormatException e) {
                                Toast.makeText(requireContext(), "Please enter a valid number for CR", Toast.LENGTH_SHORT).show();
                                editText.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                                editText.requestFocus();
                                return false; // Return false if CR input is not a number
                            }
                        }
                    }
                }
            }
        }
        return true; // All inputs are valid
    }


    private void calculate() {
        // Variables to hold the subject names, credit hours, and grade points

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
                            subjectNames[i] = editText.getText().toString().trim(); // Store subject name
                        } else if (j == 1) {
                            creditHours[i] = Integer.parseInt(editText.getText().toString().trim()); // Store CR
                        } else if (j == 2) {
                            gradePoints[i] = Float.parseFloat(editText.getText().toString().trim()); // Store GP
                        }
                    }
                }
            }
        }

        System.out.println(subjectNames);
        System.out.println(creditHours);
        System.out.println(gradePoints);

        ll_subjects_container.setVisibility(View.GONE);
        ll_results.setVisibility(View.VISIBLE);
        gpa = calculateCGPA(creditHours, gradePoints);
        gpa = Float.parseFloat(String.format("%.2f", gpa));
        tv_gpa_result.setText("  Your GPA is : " + String.format("%.2f", gpa));

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
        ll_SvSem.setVisibility(View.GONE);  // Hide the layout
        rollnoInput = rollnoInput.toUpperCase();
        String sem = String.valueOf(intsem); // Normalize roll number input

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the user document using the roll number input
        DocumentReference userRef = db.collection("Users").document(rollnoInput);

        Log.d("DEBUG", "User Input Roll No: " + rollnoInput);

        String finalRollnoInput = rollnoInput;
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String rollNoFromDb = documentSnapshot.getString("Roll No");

                // Validation: Check if the input roll number matches the current user's roll number
                if (!finalRollnoInput.equals(rollNoFromDb)) {
                    Toast.makeText(requireContext(), "You can only save GPA for your own roll number.", Toast.LENGTH_SHORT).show();
                    return;  // Exit if the roll numbers do not match
                }

                // Proceed with saving GPA if validation passes
                Map<String, Object> userData = new HashMap<>();
                userData.put("Sem " + sem, gpa);  // Store GPA for the specified semester

                // Reference to the GPA document using the entered roll number
                DocumentReference docRef = db.collection("GPA").document(finalRollnoInput);

                // Fetch the document to check if the semester field exists
                docRef.get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Check if the semester already exists in the document
                            if (document.contains("Sem " + sem)) {
                                // Update the existing GPA value
                                docRef.update(userData)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(requireContext(), "GPA updated successfully", Toast.LENGTH_SHORT).show();
                                            // Navigate to ProfileFragment after successful update
                                            navigateToProfileFragment();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(requireContext(), "Failed to update GPA", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                // Add a new field for the semester
                                docRef.set(userData, SetOptions.merge())  // Safer way to add new fields
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(requireContext(), "New semester GPA added successfully", Toast.LENGTH_SHORT).show();
                                            // Navigate to ProfileFragment after successful addition
                                            navigateToProfileFragment();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(requireContext(), "Failed to add new semester GPA", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        } else {
                            docRef.set(userData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(requireContext(), "GPA saved successfully for new document", Toast.LENGTH_SHORT).show();
                                        // Navigate to ProfileFragment after successful creation
                                        navigateToProfileFragment();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(requireContext(), "Failed to save GPA", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        Toast.makeText(requireContext(), "Failed to check GPA document", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Error fetching GPA document", Toast.LENGTH_SHORT).show();
                    Log.e("ERROR", "Error fetching GPA document", e);  // Log the exception
                });
            } else {
                Toast.makeText(requireContext(), "Error fetching user data: Document does not exist", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(requireContext(), "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("ERROR", "Error fetching user data", e);  // Log the exception
        });
    }

    // Method to navigate to ProfileFragment
    private void navigateToProfileFragment() {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new ProfileFragment()); // Replace the current fragment with ProfileFragment
        transaction.addToBackStack(null); // Optional: add this transaction to the back stack so users can navigate back
        transaction.commit();
    }


    private void showExitDialogFromFragment() {
        if (getActivity() instanceof BaseActivity) {
            BaseActivity baseActivity = (BaseActivity) getActivity();
            baseActivity.showExitConfirmationDialog();  // Call the method from BaseActivity
        }
    }

    public void saveAllSubjects(int saveToSem) {
        List<Subject> subjectList = collectSubject();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Define the target semester document path based on saveToSem
        String semesterDocumentPath = "GPA/" + rollNO + "/Semester/SEM - " + saveToSem;

        db.document(semesterDocumentPath).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        db.document(semesterDocumentPath)
                                .update("subjects", subjectList)
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Updated Semester " + saveToSem + " data successfully!"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Failed to update semester data", e));
                    } else {
                        // Create new document for the semester if it doesn't exist
                        db.document(semesterDocumentPath)
                                .set(Collections.singletonMap("subjects", subjectList))
                                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Created Semester " + saveToSem + " data successfully!"))
                                .addOnFailureListener(e -> Log.e("Firestore", "Failed to create semester data", e));
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to check for semester document", e));
    }

}






