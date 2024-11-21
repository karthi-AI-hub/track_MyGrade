package com.student_developer.track_my_grade;

import android.content.Intent;
import android.os.Bundle;
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
    private RewardedAd rewardedAd1, rewardedAd2, rewardedAd3, rewardedAd4, rewardedAd5;
    private static final String TAG = "AdActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        MobileAds.initialize(this, initializationStatus -> Log.d(TAG, "AdMob initialized."));
        loadAd1();
        loadAd2();
        loadAd3();
        loadAd4();
        loadAd5();

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

        bottomNavigationView.findViewById(R.id.nav_second_button).setOnClickListener(v -> loadHome());

        bottomNavigationView.findViewById(R.id.nav_collection_gpa).setOnClickListener(v -> loadCollectionGPA());
    }

    private void loadHome() {
        bottomNavigationView.setSelectedItemId(R.id.nav_second_button);
        gpaDataList.clear();
        adapter.notifyDataSetChanged();
        showAd1();
        showAd2();
        showAd3();
        showAd4();
        showAd5();
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

    private void loadAd1(){
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", adRequest, new RewardedAdLoadCallback() {
            public void onAdLoaded(@NonNull RewardedAd rewardednewAd) {
                        rewardedAd1 = rewardednewAd;
                        Log.d(TAG, "RewardedNew Ad 1 loaded.");
                        rewardedAd1.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                Log.d(TAG, "RewardedNew Ad 1 dismissed.");
                                loadAd1();
                            }

                            public void onAdFailedToShowFullScreenContent(LoadAdError adError) {
                                Log.e(TAG, "Failed to show RewardedNew Ad 1: " + adError.getMessage());
                                rewardedAd1 = null;
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.e(TAG, "RewardedNew Ad 1 failed to load: " + loadAdError.getMessage());
                        Log.e(TAG, "Error code: " + loadAdError.getCode() + ", Domain: " + loadAdError.getDomain());
                        rewardedAd1 = null;
                    }
                });
            }

            private void showAd1() {
                if (rewardedAd1 != null) {
                    rewardedAd1.show(this, rewardItem -> {

                    });
                } else {
                    Log.d(TAG, "Rewarded Ad 1 not ready.");
                    loadAd1();
                }
            }
    private void loadAd2(){
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", adRequest, new RewardedAdLoadCallback() {
            public void onAdLoaded(@NonNull RewardedAd rewardednewAd) {
                rewardedAd2 = rewardednewAd;
                Log.d(TAG, "RewardedNew Ad 2 loaded.");
                rewardedAd2.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d(TAG, "RewardedNew Ad 2 dismissed.");
                        loadAd2();
                    }

                    public void onAdFailedToShowFullScreenContent(LoadAdError adError) {
                        Log.e(TAG, "Failed to show RewardedNew Ad 2: " + adError.getMessage());
                        rewardedAd2 = null;
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.e(TAG, "RewardedNew Ad 2 failed to load: " + loadAdError.getMessage());
                Log.e(TAG, "Error code: " + loadAdError.getCode() + ", Domain: " + loadAdError.getDomain());
                rewardedAd2 = null;
            }
        });
    }

    private void showAd2() {
        if (rewardedAd2 != null) {
            rewardedAd2.show(this, rewardItem -> {

            });
        } else {
            Log.d(TAG, "Rewarded Ad 2 not ready.");
            loadAd2();
        }
    }
    private void loadAd3(){
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", adRequest, new RewardedAdLoadCallback() {
            public void onAdLoaded(@NonNull RewardedAd rewardednewAd) {
                rewardedAd3 = rewardednewAd;
                Log.d(TAG, "RewardedNew Ad 3 loaded.");
                rewardedAd3.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d(TAG, "RewardedNew Ad 3 dismissed.");
                        loadAd3();
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
            }
        });
    }

    private void showAd3() {
        if (rewardedAd3 != null) {
            rewardedAd3.show(this, rewardItem -> {

            });
        } else {
            Log.d(TAG, "Rewarded Ad 3 not ready.");
            loadAd3();
        }
    }
    private void loadAd4(){
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", adRequest, new RewardedAdLoadCallback() {
            public void onAdLoaded(@NonNull RewardedAd rewardednewAd) {
                rewardedAd4 = rewardednewAd;
                Log.d(TAG, "RewardedNew Ad 4 loaded.");
                rewardedAd4.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d(TAG, "RewardedNew Ad 4 dismissed.");
                        loadAd4();
                    }

                    public void onAdFailedToShowFullScreenContent(LoadAdError adError) {
                        Log.e(TAG, "Failed to show RewardedNew Ad 4: " + adError.getMessage());
                        rewardedAd4 = null;
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.e(TAG, "RewardedNew Ad 4 failed to load: " + loadAdError.getMessage());
                Log.e(TAG, "Error code: " + loadAdError.getCode() + ", Domain: " + loadAdError.getDomain());
                rewardedAd4 = null;
            }
        });
    }

    private void showAd4() {
        if (rewardedAd4 != null) {
            rewardedAd4.show(this, rewardItem -> {

            });
        } else {
            Log.d(TAG, "Rewarded Ad 4 not ready.");
            loadAd4();
        }
    }
    private void loadAd5(){
        AdRequest adRequest = new AdRequest.Builder().build();
        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917", adRequest, new RewardedAdLoadCallback() {
            public void onAdLoaded(@NonNull RewardedAd rewardednewAd) {
                rewardedAd5 = rewardednewAd;
                Log.d(TAG, "RewardedNew Ad 5 loaded.");
                rewardedAd1.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.d(TAG, "RewardedNew Ad 5 dismissed.");
                        loadAd5();
                    }

                    public void onAdFailedToShowFullScreenContent(LoadAdError adError) {
                        Log.e(TAG, "Failed to show RewardedNew Ad 5: " + adError.getMessage());
                        rewardedAd5 = null;
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.e(TAG, "RewardedNew Ad 5 failed to load: " + loadAdError.getMessage());
                Log.e(TAG, "Error code: " + loadAdError.getCode() + ", Domain: " + loadAdError.getDomain());
                rewardedAd5 = null;
            }
        });
    }

    private void showAd5() {
        if (rewardedAd5 != null) {
            rewardedAd5.show(this, rewardItem -> {

            });
        } else {
            Log.d(TAG, "Rewarded Ad 5 not ready.");
            loadAd5();
        }
    }


    @Override
    public void onBackPressed() {
        showExitConfirmationDialog();
    }
}
