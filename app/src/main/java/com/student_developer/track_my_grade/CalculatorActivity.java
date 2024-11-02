package com.student_developer.track_my_grade;

import static android.app.PendingIntent.getActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class CalculatorActivity extends BaseActivity {

    private boolean isProfileLoading = false;
    private ImageView btnProfile,btnCalculator,btnGraph;
    private ImageView ivNeedHelp;
    private TextView tvNeedHelp;
    private FloatingActionButton fab_menu, fab_opt1, fab_opt2, fab_opt3;
    private RelativeLayout rlFAB;
    private TextView tv_opt1, tv_opt2 ,tv_opt3;
    private boolean isMenuOpen = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        loadFragment(new ProfileFragment());

        btnProfile = findViewById(R.id.btn_profile);
        btnCalculator = findViewById(R.id.btn_calculator);
        btnGraph = findViewById(R.id.btn_graph);
        rlFAB = findViewById(R.id.RlFab);
        fab_menu = findViewById(R.id.fab_menu);
        fab_opt1 = findViewById(R.id.fab_option1);
        fab_opt2 = findViewById(R.id.fab_option2);
        fab_opt3 = findViewById(R.id.fab_option3);
        tv_opt1 = findViewById(R.id.tv_option1);
        tv_opt2 = findViewById(R.id.tv_option2);
        tv_opt3 = findViewById(R.id.tv_option3);
        ivNeedHelp = findViewById(R.id.iv_need_help);
        tvNeedHelp = findViewById(R.id.tv_need_help);

        btnProfile.setEnabled(!isProfileLoading);
        btnCalculator.setEnabled(!isProfileLoading);
        btnGraph.setEnabled(!isProfileLoading);

        ivNeedHelp.setOnClickListener(v -> {
          Utils.intend(this, NeedHelpActivity.class);
        });

        tvNeedHelp.setOnClickListener(v -> {
            Utils.intend(this, NeedHelpActivity.class);
        });

        fab_menu.setOnClickListener(v -> {
            if (isMenuOpen) {
                closeMenu();
            } else {
                openMenu();
            }
        });

        fab_opt3.setOnClickListener(v -> {
                    FirebaseAuth.getInstance().signOut();


                    Intent intent = new Intent(CalculatorActivity.this, LoginActivity.class);

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    startActivity(intent);

                    CalculatorActivity.this.overridePendingTransition(0, 0);


                    if (CalculatorActivity.this != null) {
                        CalculatorActivity.this.finish();
                    }
                });

        btnProfile.setOnClickListener(v -> {
            if (!isProfileLoading) {
                loadFragment(new ProfileFragment());
                rlFAB.setVisibility(View.GONE);
            }
        });

        btnCalculator.setOnClickListener(v -> {
            if (!isProfileLoading) {
                loadFragment(new CalculatorFragment());
                rlFAB.setVisibility(View.VISIBLE);
                fab_menu.setVisibility(View.VISIBLE);
                fab_opt1.setVisibility(View.GONE);
                fab_opt2.setVisibility(View.GONE);
                fab_opt3.setVisibility(View.GONE);
                tv_opt1.setVisibility(View.GONE);
                tv_opt2.setVisibility(View.GONE);
                tv_opt3.setVisibility(View.GONE);
            }

        });

        btnGraph.setOnClickListener(v -> {
            if (!isProfileLoading) {
                loadFragment(new GraphFragment());
                rlFAB.setVisibility(View.GONE);
            }
        });

    }

    public void setFabVisibility(int visibility) {
        rlFAB.setVisibility(visibility);
    }
    private void openMenu() {
        fab_opt1.setVisibility(View.VISIBLE);
        tv_opt1.setVisibility(View.VISIBLE);
        fab_opt2.setVisibility(View.VISIBLE);
        tv_opt2.setVisibility(View.VISIBLE);
        fab_opt3.setVisibility(View.VISIBLE);
        tv_opt3.setVisibility(View.VISIBLE);

        fab_opt1.animate().translationY(-440f).alpha(1f).start();
        fab_opt2.animate().translationY(-320f).alpha(1f).start();
        fab_opt3.animate().translationY(-200f).alpha(1f).start();

        tv_opt1.animate().translationY(-475f).alpha(1f).start();
        tv_opt2.animate().translationY(-355f).alpha(1f).start();
        tv_opt3.animate().translationY(-235f).alpha(1f).start();
        isMenuOpen = true;
    }

    private void closeMenu() {
        fab_opt1.animate().translationY(0).alpha(0f).withEndAction(() -> fab_opt1.setVisibility(View.GONE)).start();
        fab_opt2.animate().translationY(0).alpha(0f).withEndAction(() -> fab_opt2.setVisibility(View.GONE)).start();
        fab_opt3.animate().translationY(0).alpha(0f).withEndAction(() -> fab_opt3.setVisibility(View.GONE)).start();

        tv_opt1.animate().translationY(0).alpha(0f).withEndAction(() -> tv_opt1.setVisibility(View.GONE)).start();
        tv_opt2.animate().translationY(0).alpha(0f).withEndAction(() -> tv_opt2.setVisibility(View.GONE)).start();
        tv_opt3.animate().translationY(0).alpha(0f).withEndAction(() -> tv_opt3.setVisibility(View.GONE)).start();


        isMenuOpen = false;
    }
    private void loadFragment(Fragment fragment) {
        if (fragment != null) {

            FragmentManager fragmentManager = getSupportFragmentManager();
             FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
             fragmentTransaction.replace(R.id.fragment_container, fragment);
             fragmentTransaction.commit();
        }
    }

    public void setProfileLoading(boolean loading) {
        isProfileLoading = loading;
        ImageView btnProfile = findViewById(R.id.btn_profile);
        ImageView btnCalculator = findViewById(R.id.btn_calculator);
        ImageView btnGraph = findViewById(R.id.btn_graph);

        btnProfile.setEnabled(!loading);
        btnCalculator.setEnabled(!loading);
        btnGraph.setEnabled(!loading);
    }

    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }
}

