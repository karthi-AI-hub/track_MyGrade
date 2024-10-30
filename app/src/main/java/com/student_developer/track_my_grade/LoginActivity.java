package com.student_developer.track_my_grade;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.MeshSpecification;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.student_developer.track_my_grade.*;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class  LoginActivity extends BaseActivity {

    private static final int MIN_PASSWORD_LENGTH = 8;

    // Error messages as constants
    private static final String ERROR_NO_INTERNET = "No Internet Connection.Make sure you are connected to Internet .";
    private static final String ERROR_EMAIL_REQUIRED = "Email is required.";
    private static final String ERROR_INVALID_EMAIL = "Enter a valid email address.";
    private static final String ERROR_PASSWORD_REQUIRED = "Password is required.";
    private static final String ERROR_PASSWORD_MIN_LENGTH = "Password must be a minimum of 8 characters.";
    private static final String ERROR_LOGIN_FAILED = "Login failed. Please try again.";
    private static final String ERROR_USER_NOT_REGISTERED = "This email address is not registered.";
    private static final String ERROR_INVALID_PASSWORD = "Invalid email Id or Password. Please try again.";
    private static String rollNO;
    private EditText etEmail, etPassword;
    private ProgressBar progressBar;
    private Button btnSubmitLogin;
    private Button btnMvToLogin;
    private EditText et_PwdReset;
    private FirebaseAuth authLogin;
    private FirebaseFirestore db;
    private ImageView ivTogglePassword;
    private TextView forgetPassword;
    LinearLayout ll_Container,ll_Pwreset;
    String ReEmail;
    TextView BackToLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        showToast("You Can Login Now");
        initializeUIElements();
        setOnClickListeners();
        etEmail.requestFocus();
    }

    private void initializeUIElements() {

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        progressBar = findViewById(R.id.progressBar);
        btnSubmitLogin = findViewById(R.id.btnLoginSubmit);
        btnMvToLogin = findViewById(R.id.btnSignUp);
        forgetPassword = findViewById(R.id.forgetpassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        forgetPassword = findViewById(R.id.forgetpassword);
        et_PwdReset = findViewById(R.id.et_pwreset_email);
        BackToLogin = findViewById(R.id.backToLogin);
        ll_Container = findViewById(R.id.ll_container);
        ll_Pwreset = findViewById(R.id.ll_pwreset);
        authLogin = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (!isNetworkConnected()) {
            showErrorMessage(ERROR_NO_INTERNET);
            btnSubmitLogin.setEnabled(true);
            return;
        }


    }


    private void setOnClickListeners() {

        BackToLogin.setOnClickListener(v->{hideKeyboard(BackToLogin);
            ll_Container.setVisibility(View.VISIBLE);
            ll_Pwreset.setVisibility(View.GONE);});

        forgetPassword.setOnClickListener(v ->{
            hideKeyboard(forgetPassword);
            ll_Container.setVisibility(View.GONE);
            ll_Pwreset.setVisibility(View.VISIBLE);
            validateEmailAndSendReset();});

        btnMvToLogin.setOnClickListener(v -> {hideKeyboard(btnSubmitLogin);navigateTo(RegisterActivity.class);});
        btnSubmitLogin.setOnClickListener(v -> {
            hideKeyboard(btnSubmitLogin);
            btnSubmitLogin.setEnabled(false);
            handleLogin();
            loginUser();
        });
        ivTogglePassword.setOnClickListener(v -> {hideKeyboard(btnSubmitLogin);togglePasswordVisibility();});
        findViewById(R.id.signUpPrompt).setOnClickListener(v ->{hideKeyboard(btnSubmitLogin); navigateTo(RegisterActivity.class);});
    }

    private void handleLogin() {
        String adminemail = etEmail.getText().toString().trim();
        String adminpassword = etPassword.getText().toString().trim();

        if (!validateInputs(adminemail, adminpassword)) {
            btnSubmitLogin.setEnabled(true);
            return;
        }

        showProgressBar(true);

        authLogin.signInWithEmailAndPassword(adminemail, adminpassword)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkIfAdmin(authLogin.getCurrentUser().getUid());
                    }
                });
    }

    private void checkIfAdmin(String userId) {
        DocumentReference docRef = db.collection("Admins").document(userId);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String role = documentSnapshot.getString("Role");
                if ("admin".equals(role)) {
                    navigateTo(MainActivity.class);
                } else {
                    showToast("You have no acces");
                     }
            }
        }).addOnFailureListener(e -> {showErrorMessage("Login failed as Admin");
              });
    }



    // Method to validate the email and check Firestore
    private void validateEmailAndSendReset() {
        findViewById(R.id.btn_PwReset).setOnClickListener(view -> {
            ReEmail = et_PwdReset.getText().toString().trim();

            // Validate email format
            if (TextUtils.isEmpty(ReEmail)) {
                setError(et_PwdReset,ERROR_EMAIL_REQUIRED);
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(ReEmail).matches()) {
                setError(et_PwdReset, ERROR_INVALID_EMAIL);
                return;
            }
            db.collection("Users").whereEqualTo("Email", ReEmail).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && !task.getResult().isEmpty()) {
                    showResetEmailConfirmationDialog(ReEmail);
                } else {
                   setError(et_PwdReset, "Email not found in records.");
                     }
            });
        });
    }

    private void showResetEmailConfirmationDialog(String ReEmail) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        builder.setMessage("Do you want to send a password reset link to " + ReEmail + "?");

        builder.setPositiveButton("Yes", (dialog, which) -> sendResetPasswordEmail(ReEmail));

        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void sendResetPasswordEmail(String email) {
        authLogin.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                       showToast("Reset link sent to " + email);
                        Intent emailIntent = new Intent(Intent.ACTION_MAIN);
                        emailIntent.addCategory(Intent.CATEGORY_APP_EMAIL);
                        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(Intent.createChooser(emailIntent, "Open email app"));
                    } else {
                        String errorMessage = task.getException().getMessage();
                        showErrorMessage("Error : "+errorMessage);
                          }
                });

        ll_Container.setVisibility(View.VISIBLE);
        ll_Pwreset.setVisibility(View.GONE);
    }

    private void navigateTo(Class<?> targetActivity) {
        startActivity(new Intent(LoginActivity.this, targetActivity));
        overridePendingTransition(0, 0);
        finish();
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInputs(email, password)) {
            btnSubmitLogin.setEnabled(true);
            return;
        }


        showProgressBar(true);
        authLogin.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, this::handleLoginResponse);
    }

    private void handleLoginResponse(Task<AuthResult> task) {
        showProgressBar(false);
        if (task.isSuccessful()) {
            FirebaseUser firebaseUser = authLogin.getCurrentUser();

            if (firebaseUser.isEmailVerified()) {
                String email = firebaseUser.getEmail();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("Users")
                        .whereEqualTo("Email", email)
                        .get()
                        .addOnCompleteListener(queryTask -> {
                            if (queryTask.isSuccessful() && !queryTask.getResult().isEmpty()) {
                                DocumentSnapshot document = queryTask.getResult().getDocuments().get(0);
                                String rollNo = document.getString("Roll No");

                                if (rollNo != null) {
                                    // Save Roll No to SharedPreferences
                                    SharedPreferences sharedPref = getSharedPreferences("UserPref", Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString("roll_no", rollNo);
                                    editor.apply();


                                    navigateTo(UserInputActivity.class);
                                } else {
                                    showToast("Roll No not found.");
                                }
                            } else {
                                showToast("Failed to fetch Roll No.");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("LoginActivity", "Error fetching Roll No: ", e);
                            showToast("Error fetching Roll No.");
                        });
            } else {
                firebaseUser.sendEmailVerification();
                authLogin.signOut();
                showAlertDialog();
            }
        } else {
            handleLoginError(task);
        }
    }



    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Email Not Verified");
        builder.setMessage("Verify your Email now. You can't login without verification");

        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void handleLoginError(Task<AuthResult> task) {
        Exception exception = task.getException();
        String errorMessage = ERROR_LOGIN_FAILED;

        if (exception instanceof FirebaseAuthInvalidUserException) {
            clearEmailField();
            clearPasswordField();
            errorMessage = ERROR_USER_NOT_REGISTERED;
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            clearPasswordField();
            errorMessage = ERROR_INVALID_PASSWORD;
        }

        showErrorMessage(errorMessage);
        Log.e("LoginActivity", errorMessage, exception);
    }

    private void clearInputFields() {
        etEmail.setText("");
        etPassword.setText("");
    }

    private void clearEmailField() {
        clearField(etEmail);
    }

    private void clearPasswordField() {
        clearField(etPassword);
    }

    private void clearField(EditText editText) {
        editText.setText("");
        editText.setBackground(ContextCompat.getDrawable(this, R.drawable.edit_text_round_corner));
        editText.requestFocus();
    }

    private boolean validateInputs(String email, String password) {
        boolean isValid = true;

        if (TextUtils.isEmpty(email)) {
            setError(etEmail, ERROR_EMAIL_REQUIRED);
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            setError(etEmail, ERROR_INVALID_EMAIL);
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            setError(etPassword, ERROR_PASSWORD_REQUIRED);
            isValid = false;
        } else if (password.length() < MIN_PASSWORD_LENGTH) {
            setError(etPassword, ERROR_PASSWORD_MIN_LENGTH);
            isValid = false;
        }

        else{
            resetBackgrounds();
        }

        return isValid;
    }

    private void setError(EditText editText, String message) {
        editText.setBackground(ContextCompat.getDrawable(this, R.drawable.edit_text_round_corner));
        editText.setError(message);
        editText.requestFocus();
    }

    private void resetBackgrounds() {
        etEmail.setBackground(ContextCompat.getDrawable(this, R.drawable.edittext_backgrouond));
        etPassword.setBackground(ContextCompat.getDrawable(this, R.drawable.edittext_backgrouond));
    }

    private void showProgressBar(boolean isVisible) {
        progressBar.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        btnSubmitLogin.setEnabled(!isVisible);
        btnSubmitLogin.setText(isVisible ? "Logging in..." : "Login");
    }

    private void showErrorMessage(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private void showToast(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show(); // Change to Toast if preferred
    }

    private void togglePasswordVisibility() {
        boolean isPasswordVisible = etPassword.getTransformationMethod() instanceof PasswordTransformationMethod;
        etPassword.setTransformationMethod(isPasswordVisible ? null : new PasswordTransformationMethod());
        ivTogglePassword.setImageResource(isPasswordVisible ? R.drawable.ic_visibility : R.drawable.ic_visibility_off);
        etPassword.setSelection(etPassword.length());

        // Show Snackbar if the password field is not empty
        if (!TextUtils.isEmpty(etPassword.getText())) {
            showToast(isPasswordVisible ? "Password visible" : "Password hidden");
        }
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }




//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (authLogin.getCurrentUser() != null) {
//            SharedPreferences sharedPref = getSharedPreferences("UserPref", Context.MODE_PRIVATE);
//            String rollNo = sharedPref.getString("roll_no", null);
//
//            if (rollNo != null) {
//                Snackbar.make(LoginActivity.this.getCurrentFocus(),"Already Logged In! Roll No: " + rollNo, Snackbar.LENGTH_SHORT).show();
//                LoginActivity.rollNO = rollNo; // Save it to the static variable for future use
//
//            } else {
//                Snackbar.make(LoginActivity.this.getCurrentFocus(),"Roll No not found!", Snackbar.LENGTH_SHORT).show();
//            }
//        } else {
//            Snackbar.make(LoginActivity.this.getCurrentFocus(),"You Can Login Now!", Snackbar.LENGTH_SHORT).show();
//        }
//    }

    @Override
    public void onBackPressed() {
        showExitConfirmationDialog(); // Call the method to show the dialog
    }
}
