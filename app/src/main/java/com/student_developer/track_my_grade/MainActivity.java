package com.student_developer.track_my_grade;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {
    private ListView listView;
    private CustomAdapter adapter;
    private List<String> gpaDataList;
    private FirebaseFirestore db;
    private Button btn_logOut;
    private BottomNavigationView bottomNavigationView;
   //private RewardedAd[] rewardedAds = new RewardedAd[1];
   private RewardedAd rewardedAd1, rewardedAd2;
    private static final String TAG = "AdActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        MobileAds.initialize(this, initializationStatus -> Log.d(TAG, "AdMob initialized."));

        loadAds();

        setContentView(R.layout.activity_main);
        btn_logOut = findViewById(R.id.btn_logout);
        listView = findViewById(R.id.list_view);
        gpaDataList = new ArrayList<>();
        adapter = new CustomAdapter(this, gpaDataList);
        listView.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        loadHome();

        btn_logOut.setOnClickListener(v->{
            startActivity(new Intent(this, CalculatorActivity.class));
            finish();
        });

        bottomNavigationView.findViewById(R.id.nav_user_data).setOnClickListener(v -> loadUSERData());

        bottomNavigationView.findViewById(R.id.nav_second_button).setOnClickListener(v -> {
            loadHome();
            showRewardedNewAd1();
            showRewardedNewAd2();
        });

        bottomNavigationView.findViewById(R.id.nav_collection_gpa).setOnClickListener(v -> loadCollectionGPA());
    }

    private void loadHome() {

        bottomNavigationView.setSelectedItemId(R.id.nav_second_button);
        gpaDataList.clear();
        adapter.notifyDataSetChanged();
//        for (int i = 0; i < rewardedAds.length; i++) {
//            final int index = i;
//            showAd(rewardedAds, index);
////            new Handler().postDelayed(() -> showAd(rewardedAds, index), (long) (i * 500));
//        }

    }

    private void loadCollectionGPA() {
        bottomNavigationView.setSelectedItemId(R.id.nav_collection_gpa);
        db.collection("GPA").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                gpaDataList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String rollNo = document.getId();

                    StringBuilder gpaInfo = new StringBuilder("Roll No : " + rollNo + "\n");

                    for (int i = 1; i <= 8; i++) {
                        Double semGPA = document.getDouble("Sem " + i);

                        if (semGPA != null) {
                            double availableGPA = semGPA;
                            gpaInfo.append("SEM ").append(i)
                                    .append("   :    ")
                                    .append(String.format("%.2f", availableGPA))
                                    .append("\n");

                        }
                    }

                    gpaDataList.add(gpaInfo.toString());
                }

                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Error getting GPA data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadUSERData() {
        bottomNavigationView.setSelectedItemId(R.id.nav_user_data);

        db.collection("Users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                gpaDataList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String Email = document.getString("Email");
                    String RollNo = document.getString("Roll No");

                    if (Email != null && RollNo != null) {
                        String Users = "Email: " + Email + "\nRoll No: " + RollNo;
                        gpaDataList.add(Users);
                    }
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Error getting documents: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void loadAndShowAd(String adUnitId, RewardedAd[] rewardedAdArray, int adIndex) {
//        AdRequest adRequest = new AdRequest.Builder().build();
//        RewardedAd.load(this, adUnitId, adRequest, new RewardedAdLoadCallback() {
//            @Override
//            public void onAdLoaded(@NonNull RewardedAd loadedAd) {
//                rewardedAdArray[adIndex] = loadedAd;
//                Log.d(TAG, "Rewarded Ad " + (adIndex + 1) + " loaded.");
//                rewardedAdArray[adIndex].setFullScreenContentCallback(new FullScreenContentCallback() {
//                    @Override
//                    public void onAdDismissedFullScreenContent() {
//                        Log.d(TAG, "Rewarded Ad " + (adIndex + 1) + " dismissed.");
//                        rewardedAdArray[adIndex] = null; // Re-load the ad after it is dismissed.
//                        loadAndShowAd(adUnitId, rewardedAdArray, adIndex);
//                    }
//
//                    public void onAdFailedToShowFullScreenContent(LoadAdError adError) {
//                        Log.e(TAG, "Failed to show Rewarded Ad " + (adIndex + 1) + ": " + adError.getMessage());
//                        rewardedAdArray[adIndex] = null;
//                    }
//                });
//            }
//
//            @Override
//            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                Log.e(TAG, "Rewarded Ad " + (adIndex + 1) + " failed to load: " + loadAdError.getMessage());
//                Log.e(TAG, "Error code: " + loadAdError.getCode() + ", Domain: " + loadAdError.getDomain());
//                rewardedAdArray[adIndex] = null;
//                new Handler(Looper.getMainLooper()).postDelayed(() ->  loadAndShowAd(adUnitId, rewardedAdArray, adIndex), 10000);
//            }
//        });
//    }
//
//
//    private void showAd(RewardedAd[] rewardedAdArray, int adIndex) {
//        if (rewardedAdArray[adIndex] != null) {
//            Log.d(TAG, "Showing Rewarded Ad " + (adIndex + 1));
//            rewardedAdArray[adIndex].show(this, rewardItem -> {
//                // Handle the reward item here.
//            });
//        } else {
//            Log.d(TAG, "Rewarded Ad " + (adIndex + 1) + " not ready.");
//            loadAndShowAd("ca-app-pub-3940256099942544/5224354917", rewardedAdArray, adIndex); // Reload if ad not ready
//        }
//    }
//
//
    private void loadAds() {
        new Handler().postDelayed(() -> loadRewardNewAd1(), 0);
        new Handler().postDelayed(() -> loadRewardNewAd2(), 500);
//        new Handler().postDelayed(() -> loadAndShowAd("ca-app-pub-3940256099942544/5224354917", rewardedAds, 2), 1000);
//        new Handler().postDelayed(() -> loadAndShowAd("ca-app-pub-3940256099942544/5224354917", rewardedAds, 3), 1500);
//        new Handler().postDelayed(() -> loadAndShowAd("ca-app-pub-3940256099942544/5224354917", rewardedAds, 4), 2000);
   }

    private void loadRewardNewAd1() {
        AdRequest adRequest = new AdRequest.Builder().build();
        Log.d(TAG, "Attempting to load RewardedNew Ad 1...");
        RewardedAd.load(this, "ca-app-pub-9796820425295040/9981555588", adRequest, new RewardedAdLoadCallback() {
            public void onAdLoaded(@NonNull RewardedAd rewardednewAd) {
                rewardedAd1 = rewardednewAd;
                Log.d(TAG, "RewardedNew Ad 1 successfully loaded.");
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

    private void showRewardedNewAd1() {
        if (rewardedAd1 != null) {
            rewardedAd1.show(this, rewardItem -> {

            });
        } else {
            Log.d(TAG, "Rewarded Ad1 not ready . Reloading...");
            loadRewardNewAd1();
        }
    }

    private void loadRewardNewAd2() {
        AdRequest adRequest = new AdRequest.Builder().build();
        Log.d(TAG, "Attempting to load RewardedNew Ad 2...");
        RewardedAd.load(this, "ca-app-pub-9796820425295040/8425793738", adRequest, new RewardedAdLoadCallback() {
            public void onAdLoaded(@NonNull RewardedAd rewardednewAd) {
                rewardedAd2 = rewardednewAd;
                Log.d(TAG, "RewardedNew Ad 2 successfully loaded.");
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
                new Handler(Looper.getMainLooper()).postDelayed(() -> loadRewardNewAd2(), 20000); // Retry after 10 seconds
            }
        });
    }

    private void showRewardedNewAd2() {
        if (rewardedAd2 != null) {
            rewardedAd2.show(this, rewardItem -> {

            });
        } else {
            Log.d(TAG, "Rewarded Ad2 not ready . Reloading...");
            loadRewardNewAd2();
        }
    }






    @Override
    public void onBackPressed() {
        showRewardedNewAd1();
        showRewardedNewAd2();
        showExitConfirmationDialog();
    }
}
