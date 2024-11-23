package com.student_developer.track_my_grade;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.android.gms.ads.MobileAds;

import java.util.Calendar;


public class CalculatorActivity extends BaseActivity implements OnUserEarnedRewardListener {

    private boolean isProfileLoading = false;
    private boolean isTransactionInProgress = false;
    private ImageView btnProfile,btnCalculator,btnGraph;
    private ImageView ivNeedHelp;
    private TextView tvNeedHelp;
    private FloatingActionButton fab_menu, fab_opt1, fab_opt2, fab_opt3;
    private RelativeLayout rlFAB;
    private TextView tv_opt1, tv_opt2 ,tv_opt3;
    private boolean isMenuOpen = false;
    private SharedPreferences sharedPref;
    private AdView mBannerAd;
    private InterstitialAd mInterstitialAd;
    private RewardedInterstitialAd mRewardedAd;
    private RewardedAd rewardedAd1, rewardedAd2, rewardedAd3;
    private long lastAdTime = 0;
    private static long AD_COOLDOWN = 3 * 60 * 1000;
    private static final String TAG = "CalculatorActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calculator);

        MobileAds.initialize(this, initializationStatus -> Log.d(TAG, "AdMob initialized."));
       loadAds();


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
        ivNeedHelp.setOnClickListener(v -> openNeedHelpActivity());
        tvNeedHelp.setOnClickListener(v -> openNeedHelpActivity());


        fab_menu.setOnClickListener(v -> {tranferFAB();});

        fab_opt3.setOnClickListener(v -> {
                    FirebaseAuth.getInstance().signOut();
            sharedPref = getSharedPreferences("UserPref", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove("roll_no").apply();
            Intent intent = new Intent(CalculatorActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    CalculatorActivity.this.overridePendingTransition(0, 0);
                    if (CalculatorActivity.this != null) {
                        CalculatorActivity.this.finish();
                    }
                });

        fab_opt2.setOnClickListener(v -> {
            Intent intent = new Intent(CalculatorActivity.this, UploadDocumentActivity.class);
            startActivity(intent);
        });

        fab_opt1.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            if (hour >= 10 && hour < 17) {
                startActivity(new Intent(this, GetResultActivity.class));
            } else {
                Toast.makeText(this, "Results can only be accessed between 10 AM and 5 PM.", Toast.LENGTH_SHORT).show();
            }
        });


        btnProfile.setOnClickListener(v -> {
            btnProfile.setEnabled(false);
                if (!isProfileLoading && !isTransactionInProgress) {
                    loadFragment(new ProfileFragment());

                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastAdTime >= AD_COOLDOWN) {
                        showInterstitialAd();
                        showRewardedAd();
                        showRewardedNewAd1();
                        showRewardedNewAd2();
                        showRewardedNewAd3();

                        lastAdTime = currentTime;
                    } else {
                        Log.d(TAG, "Ad cooldown not completed. Ads will not be shown.");
                    }

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

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastAdTime >= AD_COOLDOWN) {
                    showInterstitialAd();
                    showRewardedAd();
                    showRewardedNewAd1();
                    showRewardedNewAd2();
                    showRewardedNewAd3();

                    lastAdTime = currentTime;
                } else {
                    Log.d(TAG, "Ad cooldown not completed. Ads will not be shown.");
                }

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

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastAdTime >= AD_COOLDOWN) {
                    showInterstitialAd();
                    showRewardedAd();
                    showRewardedNewAd1();
                    showRewardedNewAd2();
                    showRewardedNewAd3();

                    lastAdTime = currentTime;
                } else {
                    Log.d(TAG, "Ad cooldown not completed. Ads will not be shown.");
                }

                btnProfile.setEnabled(!isProfileLoading);
                btnCalculator.setEnabled(!isProfileLoading);
                btnGraph.setEnabled(false);
            }
        });

    }


    private void openNeedHelpActivity() {
        Utils.intend(this, NeedHelpActivity.class);
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

        SharedPreferences sharedPref = getSharedPreferences("UserPref", Context.MODE_PRIVATE);
        String collegeName = sharedPref.getString("collegeName", "");
        if ("Excel".equalsIgnoreCase(collegeName)) {
            fab_opt1.setVisibility(View.VISIBLE);
            tv_opt1.setVisibility(View.VISIBLE);

            fab_opt1.animate().translationY(-400).alpha(1f).start();
            tv_opt1.animate().translationY(-430).alpha(1f).start();
        } else {
            fab_opt1.setVisibility(View.GONE);
            tv_opt1.setVisibility(View.GONE);
        }

        fab_opt2.animate().translationY(-250).alpha(1f).start();
        tv_opt2.animate().translationY(-280).alpha(1f).start();
        fab_opt3.animate().translationY(-100).alpha(1f).start();
        tv_opt3.animate().translationY(-130).alpha(1f).start();

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

        if (isMenuOpen) closeMenu();

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

    private void loadInterstitialAd() {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(this, "ca-app-pub-9796820425295040/4351001136", adRequest, // Test Ad Unit ID
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
                        Log.d(TAG, "Interstitial Ad loaded.");

                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                Log.d(TAG, "Interstitial Ad dismissed.");
                                mInterstitialAd = null;
                                loadInterstitialAd();
                            }

                            public void onAdFailedToShowFullScreenContent(LoadAdError adError) {
                                Log.e(TAG, "Failed to show Interstitial Ad.");
                                mInterstitialAd = null;
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.e(TAG, "Interstitial Ad failed to load: " + loadAdError.getMessage());
                        mInterstitialAd = null;
                        new Handler(Looper.getMainLooper()).postDelayed(() -> loadInterstitialAd(), 10000); // Retry after 10 seconds

                    }
                });
    }
    private void showInterstitialAd() {
            if(mInterstitialAd != null) {
                mInterstitialAd.show(this);
            }else {
            loadInterstitialAd();
            Log.d(TAG, "Interstitial cooldown not passed.");
        }
    }

    private void loadRewardNewAd1() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-9796820425295040/9846918121", adRequest, new RewardedAdLoadCallback() {
            public void onAdLoaded(@NonNull RewardedAd rewardednewAd) {
                rewardedAd1 = rewardednewAd;
                Log.d(TAG, "RewardedNew Ad 1 loaded.");
                rewardedAd1.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d(TAG, "RewardedNew Ad 1 dismissed.");
                        rewardedAd1 = null;
                        loadRewardNewAd1();
                    }

                    public void onAdFailedToShowFullScreenContent(LoadAdError adError) {
                        Log.e(TAG, "Failed to show RewardedNew Ad1: " + adError.getMessage());
                        rewardedAd1 = null;
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.e(TAG, "RewardedNew Ad 1 failed to load: " + loadAdError.getMessage());
                Log.e(TAG, "Error code: " + loadAdError.getCode() + ", Domain: " + loadAdError.getDomain());
                Log.e(TAG, "Response Info: " + loadAdError.getResponseInfo());
                rewardedAd1 = null;
                new Handler(Looper.getMainLooper()).postDelayed(() -> loadRewardNewAd1(), 10000); // Retry after 10 seconds
            }
        });
    }

    private void loadRewardNewAd2() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-9796820425295040/6899262523", adRequest, new RewardedAdLoadCallback() {
            public void onAdLoaded(@NonNull RewardedAd rewardednewAd) {
                rewardedAd2 = rewardednewAd;
                Log.d(TAG, "RewardedNew Ad 2 loaded.");
                rewardedAd2.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d(TAG, "RewardedNew Ad 2 dismissed.");
                        rewardedAd2 = null;
                        loadRewardNewAd2();
                    }

                    public void onAdFailedToShowFullScreenContent(LoadAdError adError) {
                        Log.e(TAG, "Failed to show RewardedNew Ad2: " + adError.getMessage());
                        rewardedAd2 = null;
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.e(TAG, "RewardedNew Ad 2 failed to load: " + loadAdError.getMessage());
                Log.e(TAG, "Error code: " + loadAdError.getCode() + ", Domain: " + loadAdError.getDomain());
                Log.e(TAG, "Response Info: " + loadAdError.getResponseInfo());
                rewardedAd2 = null;
                new Handler(Looper.getMainLooper()).postDelayed(() -> loadRewardNewAd2(), 10000); // Retry after 10 seconds


            }
        });
    }
    private void loadRewardNewAd3() {
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-9796820425295040/8663770339", adRequest, new RewardedAdLoadCallback() {
            public void onAdLoaded(@NonNull RewardedAd rewardednewAd) {
                rewardedAd3 = rewardednewAd;
                Log.d(TAG, "RewardedNew Ad 3 loaded.");
                rewardedAd3.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d(TAG, "RewardedNew Ad 3 dismissed.");
                        rewardedAd3 = null;
                        loadRewardNewAd3();
                    }

                    public void onAdFailedToShowFullScreenContent(LoadAdError adError) {
                        Log.e(TAG, "Failed to show RewardedNew Ad 3: " + adError.getMessage());
                        rewardedAd3 = null;
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.e(TAG, "RewardedNew Ad 3 failed to load: " + loadAdError.getMessage());
                Log.e(TAG, "Error code: " + loadAdError.getCode() + ", Domain: " + loadAdError.getDomain());
                rewardedAd3 = null;
                new Handler(Looper.getMainLooper()).postDelayed(() -> loadRewardNewAd3(), 10000);

            }
        });
    }

    private void showRewardedNewAd1() {
        if (rewardedAd1 != null) {
            rewardedAd1.show(this, rewardItem -> {

            });
        } else {
            Log.d(TAG, "Rewarded Ad1 not ready or cooldown not passed.");
            loadRewardNewAd1();
        }
    }

    private void showRewardedNewAd2() {
        if (rewardedAd2 != null) {
            rewardedAd2.show(this, rewardItem -> {

            });
        } else {
            Log.d(TAG, "Rewarded Ad2 not ready or cooldown not passed.");
            loadRewardNewAd2();
        }
    }
    private void showRewardedNewAd3() {
        if (rewardedAd3 != null) {
            rewardedAd3.show(this, rewardItem -> {

            });
        } else {
            Log.d(TAG, "Rewarded Ad3 not ready or cooldown not passed.");
            loadRewardNewAd3();
        }
    }


    private void loadRewardedAd() {
        RewardedInterstitialAd.load(CalculatorActivity.this, "ca-app-pub-9796820425295040/9791502834",
                new AdRequest.Builder().build(),  new RewardedInterstitialAdLoadCallback() {
                    public void onAdLoaded(@NonNull RewardedInterstitialAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.d(TAG, "Rewarded Ad loaded.");

                        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                Log.d(TAG, "Rewarded Ad dismissed.");
                                mRewardedAd = null;
                                loadRewardedAd();
                            }


                            public void onAdFailedToShowFullScreenContent(LoadAdError adError) {
                                Log.e(TAG, "Failed to show Rewarded Ad.");
                                mRewardedAd = null;

                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.e(TAG, "Rewarded Ad failed to load: " + loadAdError.getMessage());
                        mRewardedAd = null;
                        new Handler(Looper.getMainLooper()).postDelayed(() -> loadRewardedAd(), 10000);
                    }
                });
    }
    private void showRewardedAd() {
        if (mRewardedAd != null) {
            mRewardedAd.show(this, rewardItem -> {
                // Grant reward to the user
            });
        } else {
            Log.d(TAG, "Rewarded Ad not ready or cooldown not passed.");
            loadRewardedAd();
        }
    }

    private void loadBannerAd() {
        mBannerAd = new AdView(this);
        mBannerAd.setAdUnitId("ca-app-pub-9796820425295040/2726900028");
        mBannerAd.setAdSize(AdSize.BANNER);

        if (mBannerAd != null && mBannerAd.getParent() == null) {
            LinearLayout adContainer = findViewById(R.id.bannerAdLayout);
            adContainer.addView(mBannerAd);
        }



        AdRequest adRequest = new AdRequest.Builder().build();
        mBannerAd.loadAd(adRequest);

        mBannerAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d(TAG, "Banner Ad loaded.");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                Log.e(TAG, "Banner Ad failed to load: " + adError.getMessage());
                Log.e(TAG, "Ad failed to load - Error code: " + adError.getCode() + ", Domain: " + adError.getDomain());
                new Handler(Looper.getMainLooper()).postDelayed(() ->  mBannerAd.loadAd(adRequest), 30000);
            }

            @Override
            public void onAdClicked() {
                Log.d(TAG, "Banner Ad clicked.");
            }
        });
    }

    private void loadAds() {
        loadBannerAd();

        new Handler().postDelayed(this::loadRewardNewAd3, 500);

        new Handler().postDelayed(this::loadRewardNewAd2, 1000);

        new Handler().postDelayed(this::loadRewardNewAd1, 1500);

        new Handler().postDelayed(this::loadRewardedAd, 2000);

        new Handler().postDelayed(this::loadInterstitialAd, 2500);
    }


    @Override
    public void onBackPressed() {
        if (isMenuOpen) {
            closeMenu();
        } else {
            if (System.currentTimeMillis() - lastAdTime >= AD_COOLDOWN) {
                showInterstitialAd();
                showRewardedAd();
                showRewardedNewAd2();
                showRewardedNewAd3();
                lastAdTime = System.currentTimeMillis();
            } else {
                showExitConfirmationDialog();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (mBannerAd != null) {
            mBannerAd.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {

    }
}

