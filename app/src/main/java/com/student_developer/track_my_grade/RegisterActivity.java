
//RegisterActivity.java
package com.student_developer.track_my_grade;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Pair;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegisterActivity extends BaseActivity {

    // Constants
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*]).+$";
    private static final String ROLLNO_REGEX = "^[A-Za-z0-9]{7,8}$";

    private EditText etRollNo, etEmail, etPassword, etConfirmPassword;
    private ProgressBar progressBar;
    private Button btnRegisterSubmit, btnMoveToLogin;
    private TextView passwordStrengthIndicator, tvPrompt;
    private ImageView ivTogglePassword;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        EdgeToEdge.enable(this);

        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        showToast("You Can Register Now");
        initUI();
        etRollNo.requestFocus();
        setupListeners();
    }

    private void initUI() {
        etRollNo = findViewById(R.id.etRollNo);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegisterSubmit = findViewById(R.id.btnRegisterSubmit);
        progressBar = findViewById(R.id.progressBar);
        btnMoveToLogin = findViewById(R.id.btnLogin);
        passwordStrengthIndicator = findViewById(R.id.passwordStrength_Indicator);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        tvPrompt = findViewById(R.id.loginPrompt);

        setupRealTimeValidation();
    }


    private void setupListeners() {
        View.OnClickListener loginClickListener = v -> {hideKeyboard(btnRegisterSubmit);startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        overridePendingTransition(0, 0);
        finish();
        };
        
        btnMoveToLogin.setOnClickListener(loginClickListener);
        tvPrompt.setOnClickListener(loginClickListener);

        // Submit Registration
        btnRegisterSubmit.setOnClickListener(v -> {hideKeyboard(btnRegisterSubmit);validateAndCheckUser();});

        // Toggle Password Visibility
        ivTogglePassword.setOnClickListener(v -> {hideKeyboard(btnRegisterSubmit);togglePasswordVisibility();});
    }

    private void setupRealTimeValidation() {
        TextWatcher realTimeTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (etPassword.hasFocus()) {
                    updatePasswordStrengthIndicator(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etPassword.addTextChangedListener(realTimeTextWatcher);
    }

    private void updatePasswordStrengthIndicator(String password) {
        passwordStrengthIndicator.setVisibility(View.VISIBLE);

        if (password.length() < MIN_PASSWORD_LENGTH) {
            passwordStrengthIndicator.setText("Weak: At least 8 characters");
            passwordStrengthIndicator.setTextColor(ContextCompat.getColor(this, R.color.red));
        } else if (password.matches(PASSWORD_REGEX)) {
            passwordStrengthIndicator.setText("Strong: Great job!");
            passwordStrengthIndicator.setTextColor(ContextCompat.getColor(this, R.color.green));
        } else {
            passwordStrengthIndicator.setText("Medium: Consider adding numbers or symbols");
            passwordStrengthIndicator.setTextColor(ContextCompat.getColor(this, R.color.orange));
        }
    }

    private void validateAndCheckUser() {
        String rollNo = getTrimmedText(etRollNo).toUpperCase();
        String email = getTrimmedText(etEmail);
        String password = getTrimmedText(etPassword);
        String confirmPassword = getTrimmedText(etConfirmPassword);

        if (isInputValid(rollNo, email, password, confirmPassword)) {
            if (!Utils.isNetworkConnected(this)) {
                Snackbar.make(findViewById(android.R.id.content), "No Internet, Please try again later to Register.", Snackbar.LENGTH_LONG).show();
                btnRegisterSubmit.setEnabled(true);
                return;
            }

            showProgressBar(true);

            checkIfUserExistsInFirestore(rollNo, email, () -> {
                mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getSignInMethods().isEmpty()) {
                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(this, authTask -> {
                                    showProgressBar(false);
                                    if (authTask.isSuccessful()) {
                                        saveUserData(rollNo, email, password);
                                        sendVerificationEmail(authTask.getResult().getUser());
                                    } else {
                                        handleError(authTask.getException());
                                        clearInputFields();
                                    }
                                });
                    } else {
                        showProgressBar(false);
                        showToast("Email Already Exists");
                        etEmail.getText().clear();
                    }
                });
            });
        }
    }


    private void checkIfUserExistsInFirestore(String rollNo, String email, Runnable onSuccess) {
        firestore.collection("Users").whereEqualTo("Email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().isEmpty()) {
                        firestore.collection("Users").whereEqualTo("Roll No", rollNo.toUpperCase())
                                .get()
                                .addOnCompleteListener(rollNoTask -> {
                                    if (rollNoTask.isSuccessful() && rollNoTask.getResult() != null && rollNoTask.getResult().isEmpty()) {
                                        // Neither email nor roll number exist in Firestore, proceed with Firebase Authentication check
                                        onSuccess.run();
                                    } else {
                                        showProgressBar(false);
                                        showToast("Roll Number Already Exists");
                                        etRollNo.getText().clear();
                                        etRollNo.requestFocus();
                                        etRollNo.setBackground(ContextCompat.getDrawable(this, R.drawable.edit_text_round_corner));
                                    }
                                });
                    } else {
                        showProgressBar(false);
                        showToast("Email Already Exists");
                        etEmail.getText().clear();
                        etEmail.requestFocus();
                        etEmail.setBackground(ContextCompat.getDrawable(this, R.drawable.edit_text_round_corner));
                    }
                });
    }

    private void saveUserData(String rollNo, String email, String password) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> userData = new HashMap<>();
        userData.put("Roll No", rollNo.toUpperCase());
        userData.put("Email", email);
        userData.put("Password", password);

        db.collection("Users").document(rollNo.toUpperCase())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    showToast("Registration successful");
                    clearInputFields();
                    startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                    overridePendingTransition(0, 0);

                })
                .addOnFailureListener(e -> showToast("Registration failed"));
    }
   private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private String getTrimmedText(EditText editText) {
        return editText.getText().toString().trim();
    }

    private boolean isInputValid(String rollNo, String email, String password, String confirmPassword) {
        List<Pair<EditText, String>> validations = new ArrayList<>();
        validations.add(new Pair<>(etRollNo, isRollNoValid(rollNo) ? null : getString(R.string.error_roll_no_invalid)));
        validations.add(new Pair<>(etEmail, isEmailValid(email) ? null : getString(R.string.error_email_invalid)));
        validations.add(new Pair<>(etPassword, isPasswordValid(password) ? null : getString(R.string.error_password_weak)));
        validations.add(new Pair<>(etConfirmPassword, doPasswordsMatch(password, confirmPassword) ? null : getString(R.string.error_password_mismatch)));

        boolean allValid = true;
        for (Pair<EditText, String> validation : validations) {
            if (validation.second != null) {
                validation.first.setError(validation.second);
                validation.first.setBackground(ContextCompat.getDrawable(this, R.drawable.edit_text_round_corner)); // Set red border when error
                validation.first.requestFocus();
                allValid = false;
            } else {
                validation.first.setBackground(ContextCompat.getDrawable(this, R.drawable.edittext_backgrouond)); // Reset border when valid
            }
        }

        return allValid;
    }


    private boolean isRollNoValid(String rollNo) {
        return !TextUtils.isEmpty(rollNo) && rollNo.length() >= 7 && rollNo.length() <= 8 && rollNo.matches(ROLLNO_REGEX);
    }

    private boolean isEmailValid(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= MIN_PASSWORD_LENGTH && password.matches(PASSWORD_REGEX);
    }

    private boolean doPasswordsMatch(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    private void sendVerificationEmail(FirebaseUser user) {
        showProgressBar(true);
        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    showProgressBar(false);
                    if (task.isSuccessful()) {
                        showToast("Verification email sent");
                    } else {
                        showToast("Failed to send verification email");
                    }
                });
    }

    private void showProgressBar(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegisterSubmit.setEnabled(!show);
    }


    private void clearInputFields() {
        etRollNo.getText().clear();
        etEmail.getText().clear();
        etPassword.getText().clear();
        etConfirmPassword.getText().clear();
    }

    private void togglePasswordVisibility() {
        boolean isPasswordVisible = etPassword.getTransformationMethod() instanceof PasswordTransformationMethod;
        etPassword.setTransformationMethod(isPasswordVisible ? null : new PasswordTransformationMethod());
        ivTogglePassword.setImageResource(isPasswordVisible ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
        ivTogglePassword.setContentDescription(isPasswordVisible ? "Hide password" : "Show password"); // Add accessibility support
        etPassword.setSelection(etPassword.length());
        if (!TextUtils.isEmpty(etPassword.getText())) {
            showToast(isPasswordVisible ? "Password visible" : "Password hidden");
        }
    }

    private void showToast(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show(); // Change this to Toast if you prefer
    }

    private void setError(EditText editText, String errorMessage) {
        editText.setError(errorMessage);
        editText.setBackground(ContextCompat.getDrawable(this, R.drawable.edit_text_round_corner));
        editText.requestFocus();
    }


    private void handleError(Exception exception) {
        if (exception instanceof FirebaseAuthWeakPasswordException) {
            setError(etPassword, getString(R.string.error_password_weak));
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            setError(etEmail, getString(R.string.error_email_invalid));
        } else if (exception instanceof FirebaseAuthUserCollisionException) {
            setError(etEmail, getString(R.string.error_email_collision));
        } else if (exception instanceof FirebaseNetworkException) {
            showSnackbar(getString(R.string.error_network));
        } else if (exception instanceof FirebaseAuthException) {
            showSnackbar(getString(R.string.error_authentication_failed));
        } else {
            showSnackbar(getString(R.string.error_registration) + exception.getMessage());
        }
    }


    private void showSnackbar(String string) {
        Snackbar.make(findViewById(android.R.id.content), string, Snackbar.LENGTH_SHORT).show();
    }

    // Override the onBackPressed method to show the exit dialog
    @Override
    public void onBackPressed() {
        showExitConfirmationDialog(); // Call the method to show the dialog
    }
}


