package com.student_developer.track_my_grade;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
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
    int sem;

    float gpa;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calculator, container, false);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        String rollNO = sharedPref.getString("roll_no", null);
        sem = sharedPref.getInt("sem",1);


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


        // Initialize UI components
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

            if (TextUtils.isEmpty(rollno )) {
                etConfirmRoll.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                etConfirmRoll.requestFocus();
            }else if(!rollno.equals(rollNO)){
                etConfirmRoll.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                etConfirmRoll.setError("Roll No does not match");
                etConfirmRoll.requestFocus();
            }
            else if(rollno.length()<7 ||rollno.length()>9 ){
                etConfirmRoll.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                etConfirmRoll.requestFocus();
            }else {
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
                // Safely parse the input to an integer
                int saveToSem = Integer.parseInt(semesterInput);

                // Check if the semester number is within a valid range
                if (saveToSem > 0 && saveToSem <= sem) {
                    etsvToSem.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edittext_backgrouond));
                    saveGpa(saveToSem, gpa, rollno);
                    tv_gpa_result.setText("  Your GPA is : " + String.format("%.2f", gpa) + " for Sem " + saveToSem + " saved successfully.");
                } else {
                    etsvToSem.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                    etsvToSem.requestFocus();
                    Toast.makeText(requireContext(), "Not eligible to set GPA in SEM-"+sem, Toast.LENGTH_SHORT).show();
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
            String noOfSubjects = etNoOfSubs.getText().toString().trim();

            if (TextUtils.isEmpty(noOfSubjects)) {
                etNoOfSubs.setError("Please enter the number of subjects");
                etNoOfSubs.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                return;
            }

            try {
                int numberOfSubjects = Integer.parseInt(noOfSubjects);
                if (numberOfSubjects <= 0 || numberOfSubjects > 11) {
                    etNoOfSubs.setError("Your input is not valid");
                    etNoOfSubs.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                    return;
                }

                // Remove any previously generated views
                ll_subjects_container.removeAllViews();
                ll_no_of_sub.setVisibility(View.GONE);
                ll_results.setVisibility(View.GONE);
                ll_subjects_container.setVisibility(View.VISIBLE);
                sv_containers.setVisibility(View.VISIBLE);

                // Add a row with the labels Subject Name, CR, GP
                addSubjectLabels();

                // Dynamically create 'n' subject details
                for (int i = 0; i < numberOfSubjects; i++) {
                    createSubjectDetailView(i + 1);
                }

                // Add a button at the bottom to process the inputs
                addCalculateButton();

            } catch (NumberFormatException e) {
                etNoOfSubs.setError("Please enter a valid number");
                etNoOfSubs.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
            }
        });

        return view;
    }

    private void createSubjectDetailView(int subjectNumber) {
        // Create a new LinearLayout for subject details
        LinearLayout llSubjectDetail = new LinearLayout(requireContext());
        llSubjectDetail.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, WRAP_CONTENT));
        llSubjectDetail.setOrientation(LinearLayout.HORIZONTAL);

        // Subject Name EditText
        EditText etSubjectName = new EditText(requireContext());


        etSubjectName.setId(View.generateViewId());
        etSubjectName.setTag("sub" + subjectNumber);

        LinearLayout.LayoutParams subjectNameParams = new LinearLayout.LayoutParams(
                WRAP_CONTENT, WRAP_CONTENT);
        subjectNameParams.setMargins(0, convertDpToPx(10), convertDpToPx(30), 0); // marginTop 10dp and marginEnd 30dp
        etSubjectName.setLayoutParams(subjectNameParams);
        etSubjectName.setHint("Subject " + subjectNumber);
        etSubjectName.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
        etSubjectName.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edittext_backgrouond));
        etSubjectName.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        etSubjectName.setPadding(convertDpToPx(10), convertDpToPx(10), convertDpToPx(10), convertDpToPx(10)); // Padding 10dp
        etSubjectName.setTextAlignment(View.TEXT_ALIGNMENT_CENTER); // Text alignment center

        // CR EditText
        EditText etCr = new EditText(requireContext());


        etCr.setId(View.generateViewId());
        etCr.setTag("cr" + subjectNumber);

        LinearLayout.LayoutParams crParams = new LinearLayout.LayoutParams(
                WRAP_CONTENT, WRAP_CONTENT);
        crParams.setMargins(0, convertDpToPx(10), 0, 0); // marginTop 10dp
        etCr.setLayoutParams(crParams);
        etCr.setHint("Enter CR");
        etCr.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
        etCr.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edittext_backgrouond));
        etCr.setInputType(InputType.TYPE_CLASS_NUMBER);
        etCr.setPadding(convertDpToPx(10), convertDpToPx(10), convertDpToPx(10), convertDpToPx(10)); // Padding 10dp
        etCr.setTextAlignment(View.TEXT_ALIGNMENT_CENTER); // Text alignment center

        // GP EditText
        EditText etGp = new EditText(requireContext());


        etGp.setId(View.generateViewId());
        etGp.setTag("gp" + subjectNumber);

        LinearLayout.LayoutParams gpParams = new LinearLayout.LayoutParams(
                WRAP_CONTENT, WRAP_CONTENT);
        gpParams.setMargins(convertDpToPx(30), convertDpToPx(10), 0, 0); // marginTop 10dp and marginStart 30dp
        etGp.setLayoutParams(gpParams);
        etGp.setHint("Enter GP");
        etGp.setTextColor(ContextCompat.getColor(requireContext(), R.color.green));
        etGp.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edittext_backgrouond));
        etGp.setInputType(InputType.TYPE_CLASS_NUMBER);
        etGp.setPadding(convertDpToPx(10), convertDpToPx(10), convertDpToPx(10), convertDpToPx(10)); // Padding 10dp
        etGp.setTextAlignment(View.TEXT_ALIGNMENT_CENTER); // Text alignment center

        // Add EditTexts to the LinearLayout
        llSubjectDetail.addView(etSubjectName);
        llSubjectDetail.addView(etCr);
        llSubjectDetail.addView(etGp);

        // Add the subject detail layout to the container
        ll_subjects_container.addView(llSubjectDetail);
    }

    private void addSubjectLabels() {
        LinearLayout llLabels = new LinearLayout(requireContext());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(0, 0, 0, 20); // Set margin bottom to 20dp
        llLabels.setLayoutParams(layoutParams);
        llLabels.setOrientation(LinearLayout.HORIZONTAL);
        llLabels.setGravity(Gravity.CENTER); // Center the content horizontally


        // Subject Name Label
        TextView tvSubjectLabel = new TextView(requireContext());
        LinearLayout.LayoutParams subjectLabelParams = new LinearLayout.LayoutParams(
                0, WRAP_CONTENT, 1f);
        subjectLabelParams.setMargins(convertDpToPx(15), convertDpToPx(16), convertDpToPx(20), convertDpToPx(16));
        tvSubjectLabel.setLayoutParams(subjectLabelParams);
        tvSubjectLabel.setText("Subject Name");
        tvSubjectLabel.setTextSize(18);
        tvSubjectLabel.setTypeface(null, Typeface.BOLD);  // Set text style to bold
        tvSubjectLabel.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));  // Set text color to black

        // CR Label
        TextView tvCrLabel = new TextView(requireContext());
        LinearLayout.LayoutParams crLabelParams = new LinearLayout.LayoutParams(
                0, WRAP_CONTENT, 1f);
        crLabelParams.setMargins(convertDpToPx(40), convertDpToPx(16), convertDpToPx(0), convertDpToPx(16));
        tvCrLabel.setLayoutParams(crLabelParams);
        tvCrLabel.setText("CR");
        tvCrLabel.setTextSize(18);
        tvCrLabel.setTypeface(null, Typeface.BOLD);
        tvCrLabel.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));

        // GP Label
        TextView tvGpLabel = new TextView(requireContext());
        LinearLayout.LayoutParams gpLabelParams = new LinearLayout.LayoutParams(
                0, WRAP_CONTENT, 1f);
        gpLabelParams.setMargins(convertDpToPx(30), convertDpToPx(16), convertDpToPx(0), convertDpToPx(16));
        tvGpLabel.setLayoutParams(gpLabelParams);
        tvGpLabel.setText("GP");
        tvGpLabel.setTextSize(18);
        tvGpLabel.setTypeface(null, Typeface.BOLD);
        tvGpLabel.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black));

        // Add Labels to the LinearLayout
        llLabels.addView(tvSubjectLabel);
        llLabels.addView(tvCrLabel);
        llLabels.addView(tvGpLabel);

        // Add the labels layout to the container
        ll_subjects_container.addView(llLabels);
    }

    private void addCalculateButton() {
        Button btnCalculate = new Button(requireContext());
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, WRAP_CONTENT);
        buttonLayoutParams.setMargins(0, convertDpToPx(20), 0, 0); // Set margin top as 20dp
        btnCalculate.setLayoutParams(buttonLayoutParams);
        btnCalculate.setText("Calculate GPA");
        btnCalculate.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.blue_600));
        btnCalculate.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
        btnCalculate.setPadding(20, 20, 20, 20);
        btnCalculate.setGravity(Gravity.CENTER); // Center the text in the button


        // Add a click listener for the button to process the inputs
        btnCalculate.setOnClickListener(v -> {
            if (isInputValid()) {// Check if all inputs are valid
                hideKeyboard(v);
                calculate();
                // If valid, perform the calculation
            }
        });


        // Add the button to the container
        ll_subjects_container.addView(btnCalculate);
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
                            editText.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edittext_backgrouond));
                        }

                        // Validate GP (should be 10 or less)
                        if (editText.getTag().toString().startsWith("gp")) {
                            try {
                                float gpValue = Float.parseFloat(editText.getText().toString().trim());
                                if (gpValue > 10) {
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
                                if (crValue > 10) {
                                    Toast.makeText(requireContext(), "CR should be 10 or less", Toast.LENGTH_SHORT).show();
                                    editText.setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.edit_text_round_corner));
                                    editText.requestFocus();
                                    return false; // Return false if CR is more than 10
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

        // Now you can use subjectNames, creditHours, and gradePoints for your CGPA calculation logic
        // For example:
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
        return totalCreditHours > 0 ? totalGradePoints / totalCreditHours : 0; // Avoid division by zero
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
                            // Document doesn't exist, create a new document with the semester and GPA
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
        // Ensure the fragment is attached to an activity that extends BaseActivity
        if (getActivity() instanceof BaseActivity) {
            BaseActivity baseActivity = (BaseActivity) getActivity();
            baseActivity.showExitConfirmationDialog();  // Call the method from BaseActivity
        }
    }
}



