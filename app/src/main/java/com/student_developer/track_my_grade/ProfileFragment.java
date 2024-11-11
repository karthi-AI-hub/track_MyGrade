package com.student_developer.track_my_grade;



import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;



public class ProfileFragment extends Fragment {

    TextView tvpro1, tvpro2, tvpro3, tvpro4, tvpro5, tvpro6, tvpro7, tvpro8, tvCGPATotal;
    TextView pro_name, pro_roll, pro_reg, pro_dob, pro_clg, pro_dept, pro_phno, pro_email, pro_cgpa;
    ShapeableImageView pro_photo;
    Button mvTOAdmin;
    private FirebaseFirestore db;
    DocumentReference docRef;
    FirebaseDatabase database;
    DatabaseReference myRef;
    FirebaseUser firebaseUser;
    public static ProgressBar progressBar;
    String rollNO;
    String email;
    Double meanGPA;
    int currentSemester;
    LinearLayout[] semesterLayouts;
    SharedPreferences sharedPref;
    private Map<String, String> departmentNames;
    private ConnectivityManager.NetworkCallback networkCallback;
    private ConnectivityManager connectivityManager;
    String rollNo, DBofClg, DBofDept;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        getActivity().getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);


        if (getActivity() != null) {
            View decorView = getActivity().getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
        initializeDepartmentNames();

        semesterLayouts = new LinearLayout[]{
                view.findViewById(R.id.llS1),
                view.findViewById(R.id.llS2),
                view.findViewById(R.id.llS3),
                view.findViewById(R.id.llS4),
                view.findViewById(R.id.llS5),
                view.findViewById(R.id.llS7),
                view.findViewById(R.id.llS6),
                view.findViewById(R.id.llS8)
        };

        sharedPref = getActivity().getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        rollNO = sharedPref.getString("roll_no", null).toUpperCase();

        DBofClg = sharedPref.getString("collegeName", null);
        DBofDept = sharedPref.getString("departmentName", null);
        System.out.println("RollNo : "+ rollNO);
        System.out.println("collegeName : "+ DBofClg);
        System.out.println("departmentName : "+ DBofDept);

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        if (getActivity() instanceof CalculatorActivity) {
            ((CalculatorActivity) getActivity()).setProfileLoading(true);
        }

        db = FirebaseFirestore.getInstance();

        rollNo = rollNO.toUpperCase();
        docRef = db.collection("GPA").document(rollNo);

        database = FirebaseDatabase.getInstance("https://app1-ec550-default-rtdb.asia-southeast1.firebasedatabase.app/");
        myRef = database.getReference(DBofClg).child(DBofDept).child(rollNo);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        email = firebaseUser.getEmail();

        fetchDataFromFirebase();
        mvTOAdmin = view.findViewById(R.id.mvToAdmin);

        if (rollNO.equalsIgnoreCase("22AD045")) {
            mvTOAdmin.setVisibility(View.VISIBLE);
        }
        if (rollNO.equalsIgnoreCase("22AD049")) {
            mvTOAdmin.setVisibility(View.VISIBLE);
        }
        mvTOAdmin.setOnClickListener(view1 -> startActivity(new Intent(getActivity(), MainActivity.class)));


        tvpro1 = view.findViewById((R.id.tv_pro1));
        tvpro2 = view.findViewById((R.id.tv_pro2));
        tvpro3 = view.findViewById((R.id.tv_pro3));
        tvpro4 = view.findViewById((R.id.tv_pro4));
        tvpro5 = view.findViewById((R.id.tv_pro5));
        tvpro6 = view.findViewById((R.id.tv_pro6));
        tvpro7 = view.findViewById((R.id.tv_pro7));
        tvpro8 = view.findViewById((R.id.tv_pro8));
        tvCGPATotal = view.findViewById((R.id.tvCgpaTotal));

        pro_photo = view.findViewById(R.id.pro_photo);
        pro_name = view.findViewById((R.id.pro_name));
        pro_roll = view.findViewById((R.id.pro_roll));
        pro_reg = view.findViewById((R.id.pro_reg));
        pro_dob = view.findViewById((R.id.pro_dob));
        pro_email = view.findViewById((R.id.pro_email));
        pro_cgpa = view.findViewById((R.id.pro_cgpa));
        pro_clg = view.findViewById((R.id.pro_clg));
        pro_phno = view.findViewById((R.id.pro_phno));
        pro_dept = view.findViewById((R.id.pro_dept));

        setProText(pro_roll, rollNO);
        setProText(pro_email, email);

        setButtonsEnabled(false);
        TextView[] textViews = {tvpro1, tvpro2, tvpro3, tvpro4, tvpro5, tvpro6, tvpro7, tvpro8};
        SharedPreferences sharedPref = getContext().getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        for (int i = 0; i < textViews.length; i++) {
            int index = i + 1;
            TextView textView = textViews[i];
            textView.setOnClickListener(view1 -> {
                String text = textView.getText().toString();
                if (!text.equals("N/A")) {
                    navigateToGraphFragment();
                } else {
                    editor.putInt("TextViewIndex", index);
                    editor.apply();
                    navigateToCalculatorFragment();
                }
            });
        }


        if (getActivity() != null) {
            TextView tvActivityProfile = getActivity().findViewById(R.id.tv_profile);
            TextView tvCgpa = getActivity().findViewById(R.id.tv_cgpa);
            TextView tvGraph = getActivity().findViewById(R.id.tv_graph);
            View vProfile = getActivity().findViewById(R.id.v_profile);
            View vCgpa = getActivity().findViewById(R.id.v_cgpa);
            View vGraph = getActivity().findViewById(R.id.v_graph);

            if (tvActivityProfile != null) {
                tvActivityProfile.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
            }
            if (tvCgpa != null) {
                tvCgpa.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_600));
            }
            if (tvGraph != null) {
                tvGraph.setTextColor(ContextCompat.getColor(requireContext(), R.color.blue_600));
            }

            if (vProfile != null) {
                vProfile.setVisibility(View.VISIBLE);
            }
            if (vCgpa != null) {
                vCgpa.setVisibility(View.GONE);
            }
            if (vGraph != null) {
                vGraph.setVisibility(View.GONE);
            }
        }

        if (Utils.isNetworkAvailable(requireContext())) {
            loadData();
        } else {
            Utils.Snackbar(requireActivity().findViewById(android.R.id.content), "No Internet . Waiting for connection . . .", "long");
            connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override
                public void onAvailable(@NonNull Network network) {
                    requireActivity().runOnUiThread(() -> {
                        try {
                            Utils.Snackbar(view, "Network connected. Retrieving your data . . .", "long");
                            loadData();
                        } finally {
                            progressBar.setVisibility(View.GONE);
                            setButtonsEnabled(true);
                        }
                    });
                    connectivityManager.unregisterNetworkCallback(this);
                }

                @Override
                public void onLost(@NonNull Network network) {
                    requireActivity().runOnUiThread(() -> {
                        Utils.Snackbar(view, "Network lost...", "long");
                        progressBar.setVisibility(View.GONE);
                        setButtonsEnabled(true);
                    });
                }
            };

            if (connectivityManager != null) {
                connectivityManager.registerDefaultNetworkCallback(networkCallback);
            }
        }

        pro_photo.setOnClickListener(v -> {
            Dialog dialog = new Dialog(getContext());
            dialog.setContentView(R.layout.full_image_view);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            ImageView fullImageView = dialog.findViewById(R.id.full_image_View);
            TextView tvClose = dialog.findViewById(R.id.tv_close);
            fullImageView.setImageDrawable(pro_photo.getDrawable());
            tvClose.setOnClickListener(v2 -> dialog.dismiss());

            dialog.show();
        });

        return view;
    }
    private void loadData(){
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists() && getContext() != null) {
                Float gpa1 = getGpaFromDocument(documentSnapshot, "Sem 1");
                Float gpa2 = getGpaFromDocument(documentSnapshot, "Sem 2");
                Float gpa3 = getGpaFromDocument(documentSnapshot, "Sem 3");
                Float gpa4 = getGpaFromDocument(documentSnapshot, "Sem 4");
                Float gpa5 = getGpaFromDocument(documentSnapshot, "Sem 5");
                Float gpa6 = getGpaFromDocument(documentSnapshot, "Sem 6");
                Float gpa7 = getGpaFromDocument(documentSnapshot, "Sem 7");
                Float gpa8 = getGpaFromDocument(documentSnapshot, "Sem 8");

                setGPAColorAndText(tvpro1, gpa1);
                setGPAColorAndText(tvpro2, gpa2);
                setGPAColorAndText(tvpro3, gpa3);
                setGPAColorAndText(tvpro4, gpa4);
                setGPAColorAndText(tvpro5, gpa5);
                setGPAColorAndText(tvpro6, gpa6);
                setGPAColorAndText(tvpro7, gpa7);
                setGPAColorAndText(tvpro8, gpa8);

                Float[] gpas = {gpa1, gpa2, gpa3, gpa4, gpa5, gpa6, gpa7, gpa8};
                double sum = 0.0;
                int count = 0;
                for (Float gpa : gpas) {
                    if (gpa != null && gpa > 0.0 && gpa <= 10.0) {
                        sum += gpa;
                        count++;
                    }
                }

                meanGPA = (count > 0) ? (sum / count) : null;
                meanGPA = (count > 0) ? (sum / count) : null;
                if (meanGPA != null) {
                    meanGPA = Double.valueOf(String.format("%.2f", meanGPA));
                }


                Map<String, Object> cgpaData = new HashMap<>();
                cgpaData.put("CGPA", meanGPA.toString().toUpperCase());
                db.collection("Users").document(rollNo.toUpperCase())
                        .update(cgpaData)
                        .addOnSuccessListener(avoid -> {
                            setProText(pro_cgpa, String.format("%.2f", meanGPA));
                        });

                if (meanGPA != null) {
                    tvCGPATotal.setText(String.format("Your CGPA for %d Semester(s): %.2f", count, meanGPA));
                } else {
                    tvCGPATotal.setText("Your CGPA for all Semester      : 0.00");
                }

                progressBar.setVisibility(View.GONE);
                tvCGPATotal.setVisibility(View.VISIBLE);
                 setButtonsEnabled(true);
                if (getActivity() instanceof CalculatorActivity) {
                    ((CalculatorActivity) getActivity()).setProfileLoading(false);
                }
            } else {
                progressBar.setVisibility(View.GONE);
                tvCGPATotal.setVisibility(View.VISIBLE);
                setButtonsEnabled(true);
                if (getActivity() != null && getActivity() instanceof CalculatorActivity) {
                    ((CalculatorActivity) getActivity()).setProfileLoading(false);
                }
            }
        });

    }
    private void fetchDataFromFirebase() {

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("Name").getValue(String.class);
                    String regNo = dataSnapshot.child("RegNo").getValue(String.class);
                    String clg = dataSnapshot.child("Clg").getValue(String.class);
                    String dept = dataSnapshot.child("Dept").getValue(String.class);
                    String sem = dataSnapshot.child("SEM").getValue(String.class);
                    String dob = dataSnapshot.child("DOB").getValue(String.class);
                    String phno = dataSnapshot.child("PhNo").getValue(String.class);
                    String profileUrl = dataSnapshot.child("Profile").getValue(String.class);

                    name = name != null ? name.toUpperCase() : "UNKNOWN";
                    regNo = regNo != null ? regNo.toUpperCase() : "UNKNOWN";
                    clg = clg != null ? clg.toUpperCase() : "UNKNOWN";
                    dept = dept != null ? dept : "UNKNOWN";
                    sem = sem != null ? sem : "UNKNOWN";
                    dob = dob != null ? dob.toUpperCase() : "UNKNOWN";
                    phno = phno != null ? phno : "UNKNOWN";

                    String fullDept = departmentNames.getOrDefault(dept, dept);

                    if (profileUrl != null) {
                        new LoadImageFromURL(pro_photo).execute(profileUrl);
                    }

                    currentSemester = !sem.equals("UNKNOWN") ? Integer.parseInt(sem) : 1;
                    removeExtraSemester(currentSemester);
                    setProText(pro_name, name.toUpperCase());
                    setProText(pro_reg, regNo.toUpperCase());
                    setProText(pro_clg, clg.toUpperCase());
                    setProText(pro_dob, dob.toUpperCase());
                    setProText(pro_phno, phno);
                    pro_dept.setText(fullDept + ", SEM-" + sem);

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt("current_sem", currentSemester);
                    editor.putString("stuRegNo", regNo);
                    editor.apply();


                } else {
                    Toast.makeText(getActivity(), "No Data Found.", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Error in Retriving Data.", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private static class LoadImageFromURL extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

        public LoadImageFromURL(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            Bitmap bitmap = null;
            try {
                URL url = new URL(urlDisplay);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (result != null) {
                imageView.setImageBitmap(result);
            } else {
                imageView.setImageResource(R.drawable.profile);
            }
        }
    }

    private void removeExtraSemester(int currentSemester) {
        for (int i = 0; i < semesterLayouts.length; i++) {
            if (i < currentSemester) {
                semesterLayouts[i].setVisibility(View.VISIBLE);
            } else {
                semesterLayouts[i].setVisibility(View.GONE);
            }
        }
    }

    private void setProText(TextView tvname, String tvVal) {
        tvname.setText(tvVal);
    }


    private Float getGpaFromDocument(DocumentSnapshot documentSnapshot, String key) {
        if (documentSnapshot.contains(key)) {
            try {
                return documentSnapshot.getDouble(key).floatValue();
            } catch (Exception e) {
                Log.e("ProfileFragment", "Error parsing GPA for " + key + ": " + e.getMessage());
            }
        }
        return null;
    }


    private void setGPAColorAndText(TextView textView, Float gpa) {
        if (gpa == null) {
            textView.setText("N/A");
            textView.setTextColor(getResources().getColor(R.color.gray));
            return;
        }

        textView.setText(String.valueOf(gpa));
        if (gpa >= 7.5) {
            textView.setTextColor(getResources().getColor(R.color.green)); // Good GPA
        } else if (gpa >= 5.0) {
            textView.setTextColor(getResources().getColor(R.color.orange)); // Average GPA
        } else {
            textView.setTextColor(getResources().getColor(R.color.red)); // Poor GPA
        }

    }


    private void navigateToCalculatorFragment() {
        CalculatorFragment calculatorFragment = new CalculatorFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, calculatorFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void navigateToGraphFragment() {
        GraphFragment profileFragment = new GraphFragment();
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, profileFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void clearTextViews() {
        tvpro1.setText("");
        tvpro2.setText("");
        tvpro3.setText("");
        tvpro4.setText("");
        tvpro5.setText("");
        tvpro6.setText("");
        tvpro7.setText("");
        tvpro8.setText("");
    }

    private void setButtonsEnabled(boolean enabled) {
        tvpro1.setEnabled(enabled);
        tvpro2.setEnabled(enabled);
        tvpro3.setEnabled(enabled);
        tvpro4.setEnabled(enabled);
        tvpro5.setEnabled(enabled);
        tvpro6.setEnabled(enabled);
        tvpro7.setEnabled(enabled);
        tvpro8.setEnabled(enabled);

        if (getActivity() instanceof CalculatorActivity) {
            ((CalculatorActivity) getActivity()).setProfileLoading(false);
        }
    }


    private void initializeDepartmentNames() {
        departmentNames = new HashMap<>();
        departmentNames.put("AIDS", "Artificial Intelligence & Data Science");
        departmentNames.put("AERO", "Aerospace Engineering");
        departmentNames.put("AGRI", "Agricultural Engineering");
        departmentNames.put("BME", "Bio Medical Engineering");
        departmentNames.put("CSE", "Computer Science and Engineering");
        departmentNames.put("CIVIL", "Civil Engineering");
        departmentNames.put("ECE", "Electronics and Communication Engineering");
        departmentNames.put("EEE", "Electrical and Electronics Engineering");
        departmentNames.put("IT", "Information Technology");
        departmentNames.put("FT", "Food Technology");
        departmentNames.put("MECH", "Mechanical Engineering");
        departmentNames.put("PCT", "Petroleum Chemical Technology");
        departmentNames.put("SF", "Safety & Fire");
    }

    public void onDestroyView() {
        super.onDestroyView();

        if (connectivityManager != null && networkCallback != null) {
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }

    }
}