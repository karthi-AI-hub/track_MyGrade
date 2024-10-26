package com.student_developer.track_my_grade;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.Objects;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestoreException;




public class ProfileFragment extends Fragment {

    TextView tvpro1,tvpro2,tvpro3,tvpro4,tvpro5,tvpro6,tvpro7,tvpro8,tvCGPATotal;
    Button btnLogOut,mvTOAdmin;
    private FirebaseFirestore db;
    public static ProgressBar progressBar;
    String rollNO;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment (fragment_profile.xml)
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


        SharedPreferences sharedPref = getActivity().getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        rollNO = sharedPref.getString("roll_no", null);


        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        // Notify activity that profile is loading
        if (getActivity() instanceof CalculatorActivity) {
            ((CalculatorActivity) getActivity()).setProfileLoading(true);
        }


        btnLogOut =view.findViewById(R.id.btn_logOut);
        db = FirebaseFirestore.getInstance();
        String rollNo = rollNO;
        DocumentReference docRef = db.collection("GPA").document(rollNo);
        mvTOAdmin = view.findViewById(R.id.mvToAdmin);
        if(rollNO.equalsIgnoreCase("22AD045")){
            mvTOAdmin.setVisibility(View.VISIBLE);
        }
        if(rollNO.equalsIgnoreCase("22AD049")){
            mvTOAdmin.setVisibility(View.VISIBLE);
        }
        mvTOAdmin.setOnClickListener(view1 -> startActivity(new Intent(getActivity(),MainActivity.class)));



        tvpro1 = view.findViewById((R.id.tv_pro1));
        tvpro2 = view.findViewById((R.id.tv_pro2));
        tvpro3 = view.findViewById((R.id.tv_pro3));
        tvpro4 = view.findViewById((R.id.tv_pro4));
        tvpro5 = view.findViewById((R.id.tv_pro5));
        tvpro6 = view.findViewById((R.id.tv_pro6));
        tvpro7 = view.findViewById((R.id.tv_pro7));
        tvpro8 = view.findViewById((R.id.tv_pro8));
        tvCGPATotal = view.findViewById((R.id.tvCgpaTotal));


        TextView[] textViews = {tvpro1, tvpro2, tvpro3, tvpro4, tvpro5, tvpro6, tvpro7, tvpro8};

        setButtonsEnabled(false);

        for (TextView textView : textViews) {
            textView.setOnClickListener(view1 -> navigateToCalculatorFragment());
        }
        btnLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();


                Intent intent = new Intent(getActivity(), LoginActivity.class);

                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                startActivity(intent);

                getActivity().overridePendingTransition(0, 0);


                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });



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

                // Calculate and display the CGPA
                Double meanGPA = (count > 0) ? (sum / count) : null;
                if (meanGPA != null) {
                    tvCGPATotal.setText("Your CGPA for " + count + " Semester(s): " + String.format("%.2f", meanGPA));
                } else {
                    tvCGPATotal.setText("Your CGPA for all Semester      : 0.00");
                }

                progressBar.setVisibility(View.GONE);
                tvCGPATotal.setVisibility(View.VISIBLE);
                btnLogOut.setVisibility(View.VISIBLE);
                setButtonsEnabled(true);
                if (getActivity() instanceof CalculatorActivity) {
                    ((CalculatorActivity) getActivity()).setProfileLoading(false);
                }
            } else {
                progressBar.setVisibility(View.GONE);
                tvCGPATotal.setVisibility(View.VISIBLE);
                btnLogOut.setVisibility(View.VISIBLE);
                setButtonsEnabled(true);
                if (getActivity() != null && getActivity() instanceof CalculatorActivity) {
                    ((CalculatorActivity) getActivity()).setProfileLoading(false);
                }
            }
        });

        return view;
    }



    private Float getGpaFromDocument(DocumentSnapshot documentSnapshot, String key) {
        Object gpaValue = documentSnapshot.get(key);
        if (gpaValue instanceof Double) {
            return ((Double) gpaValue).floatValue();
        } else if (gpaValue instanceof String && "N/A".equals(gpaValue)) {
            return null;
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
        // Create an instance of the CalculatorFragment
        CalculatorFragment calculatorFragment = new CalculatorFragment();

        // Use the FragmentManager to replace the current fragment with the CalculatorFragment
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, calculatorFragment); // Change fragment_container to your actual container ID
        transaction.addToBackStack(null); // Optional: Add to back stack if you want to navigate back
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
        btnLogOut.setEnabled(enabled);

        // Ensure activity is attached before calling setProfileLoading
        if (getActivity() instanceof CalculatorActivity) {
            ((CalculatorActivity) getActivity()).setProfileLoading(false);
        }
    }

    private void showExitDialogFromFragment() {
        // Ensure the fragment is attached to an activity that extends BaseActivity
        if (getActivity() instanceof BaseActivity) {
            BaseActivity baseActivity = (BaseActivity) getActivity();
            baseActivity.showExitConfirmationDialog();  // Call the method from BaseActivity
        }
    }
}
