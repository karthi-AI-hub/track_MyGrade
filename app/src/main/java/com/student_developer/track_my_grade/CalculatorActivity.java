package com.student_developer.track_my_grade;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;


public class CalculatorActivity extends BaseActivity {

    private boolean isProfileLoading = false;
    private boolean isTransactionInProgress = false;
    private ImageView btnProfile,btnCalculator,btnGraph;
    private ImageView ivNeedHelp;
    private TextView tvNeedHelp;
    private FloatingActionButton fab_menu, fab_opt1, fab_opt2, fab_opt3;
    private RelativeLayout rlFAB;
    private TextView tv_opt1, tv_opt2 ,tv_opt3;
    private boolean isMenuOpen = false;
    private String DBcollegeName;
    private String DBdepartmentName;
    private InterstitialAd mInterstitialAd;
    private static final String TAG = "CalculatorActivity";
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        MobileAds.initialize(this, initializationStatus -> {
            Log.d(TAG, "AdMob initialized.");
        });

        MobileAds.initialize(this);
        loadAd();
        loadBannerAd();

        DBcollegeName = getIntent().getStringExtra("collegeName");
        System.out.println("collegeName: " + DBcollegeName);
        DBdepartmentName = getIntent().getStringExtra("departmentName");

        loadFragment(new ProfileFragment());

        btnProfile = findViewById(R.id.btn_profile);
        btnCalculator = findViewById(R.id.btn_calculator);
        btnGraph = findViewById(R.id.btn_graph);
        rlFAB = findViewById(R.id.RlFab);
        rlFAB.setVisibility(View.GONE);
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
          finish();
        });

        tvNeedHelp.setOnClickListener(v -> {
            Utils.intend(this, NeedHelpActivity.class);
            finish();
        });

        fab_menu.setOnClickListener(v -> {tranferFAB();});

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

        fab_opt2.setOnClickListener(v -> {
            Intent intent = new Intent(CalculatorActivity.this, UploadDocumentActivity.class);
            startActivity(intent);

            CalculatorActivity.this.overridePendingTransition(0, 0);


            if (CalculatorActivity.this != null) {
                CalculatorActivity.this.finish();
            }
        });


        btnProfile.setOnClickListener(v -> {

            btnProfile.setEnabled(false);
            if (!isProfileLoading && !isTransactionInProgress) {
                loadFragment(new ProfileFragment());
                btnProfile.setEnabled(isProfileLoading);
                if (isMenuOpen) {
                    closeMenu();
                }
                setFabVisibility(View.GONE);
                btnCalculator.setEnabled(!isProfileLoading);
                btnGraph.setEnabled(!isProfileLoading);
            }
        });

        btnCalculator.setOnClickListener(v -> {
            if (!isProfileLoading && !isTransactionInProgress) {
                loadFragment(new CalculatorFragment());
                if (isMenuOpen) {
                    closeMenu();
                }
                setFabVisibility(View.VISIBLE);
                btnProfile.setEnabled(!isProfileLoading);
                btnCalculator.setEnabled(true);
                btnGraph.setEnabled(!isProfileLoading);
            }

        });

        btnGraph.setOnClickListener(v -> {
            if (!isProfileLoading && !isTransactionInProgress) {
                loadFragment(new GraphFragment());
                if (isMenuOpen) {
                    closeMenu();
                }
                setFabVisibility(View.GONE);
                btnProfile.setEnabled(!isProfileLoading);
                btnCalculator.setEnabled(!isProfileLoading);
                btnGraph.setEnabled(false);

            }
        });

    }
    private void tranferFAB(){
        float startRotation = isMenuOpen ? 135f : 0f;
        float endRotation = isMenuOpen ? 0f : 135f;
        ObjectAnimator rotation = ObjectAnimator.ofFloat(fab_menu, "rotation", startRotation, endRotation);
        rotation.setDuration(300);
        rotation.start();

        if (isMenuOpen) {
            closeMenu();
        } else {
            openMenu();
        }
    }

    public void setFabVisibility(int visibility) {
        rlFAB.setVisibility(visibility);
    }

    private void openMenu() {
        fab_opt2.setVisibility(View.VISIBLE);
        tv_opt2.setVisibility(View.VISIBLE);
        fab_opt3.setVisibility(View.VISIBLE);
        tv_opt3.setVisibility(View.VISIBLE);

        String collegeName = getIntent().getStringExtra("collegeName");

        if ("Excel".equalsIgnoreCase(collegeName)) {
            fab_opt1.setVisibility(View.VISIBLE);
            tv_opt1.setVisibility(View.VISIBLE);

            fab_opt1.animate().translationY(-500).alpha(1f).start();
            tv_opt1.animate().translationY(-530).alpha(1f).start();
        } else {
            fab_opt1.setVisibility(View.GONE);
            tv_opt1.setVisibility(View.GONE);
        }

        fab_opt2.animate().translationY(-350).alpha(1f).start();
        tv_opt2.animate().translationY(-380).alpha(1f).start();
        fab_opt3.animate().translationY(-200).alpha(1f).start();
        tv_opt3.animate().translationY(-230).alpha(1f).start();

        isMenuOpen = true;
    }


    private void closeMenu() {

        fab_opt1.animate().translationY(0).alpha(0f).start();
        tv_opt1.animate().translationY(0).alpha(0f).start();

        fab_opt2.animate().translationY(0).alpha(0f).start();
        tv_opt2.animate().translationY(0).alpha(0f).start();

        fab_opt3.animate().translationY(0).alpha(0f).start();
        tv_opt3.animate().translationY(0).alpha(0f).start();


        isMenuOpen = false;
    }
    private void loadFragment(Fragment fragment) {
        if (fragment == null || isTransactionInProgress) return;
        isTransactionInProgress = true;

        if (fragment instanceof ProfileFragment) {
            Bundle bundle = new Bundle();
            bundle.putString("collegeName", DBcollegeName);
            bundle.putString("departmentName", DBdepartmentName);
            fragment.setArguments(bundle);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment currentFragment = fragmentManager.findFragmentById(R.id.fragment_container);

        if (currentFragment != null) {
            fragmentManager.beginTransaction().hide(currentFragment).commitNow();
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
        isTransactionInProgress = false;
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
    private void loadAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this, "ca-app-pub-9796820425295040/4351001136", adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");

                        mInterstitialAd.show(CalculatorActivity.this);

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                Log.d(TAG, "Ad was dismissed.");
                                loadAd();
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                Log.e(TAG, "Failed to show ad: " + adError.getMessage());
                                mInterstitialAd = null;
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
                                Log.d(TAG, "Ad is showing.");
                                mInterstitialAd = null; }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.e(TAG, "Ad failed to load: " + loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });
    }
    private void loadBannerAd(){
        adView = new AdView(this);
        adView.setAdUnitId("ca-app-pub-9796820425295040/2726900028");
        adView.setAdSize(AdSize.SMART_BANNER);

        LinearLayout layout = findViewById(R.id.bannerAdLayout);

        AdRequest adRequest = new AdRequest.Builder().build();
        layout.removeAllViews();
        layout.addView(adView);
        adView.loadAd(adRequest);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }

            @Override
            public void onAdFailedToLoad(LoadAdError adError) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdImpression() {
                // Code to be executed when an impression is recorded
                // for an ad.
            }

            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }
        });

    }

    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }

}

