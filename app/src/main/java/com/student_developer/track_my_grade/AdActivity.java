package com.student_developer.track_my_grade;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;

public class AdActivity extends AppCompatActivity implements IUnityAdsInitializationListener {

    private String unityGameID = "5742359";
    private boolean testMode = false;
    private String adUnitId = "rew1";
    private Button rewardedButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Optional; for full-screen mode
        setContentView(R.layout.activity_ad);

        UnityAds.initialize(getApplicationContext(), unityGameID, testMode, this);

        Button btn_Exit = findViewById(R.id.btn_exit);
        rewardedButton = findViewById(R.id.btn_ad);

        btn_Exit.setOnClickListener(view -> {
            Intent intent = new Intent(AdActivity.this, CalculatorActivity.class);
            startActivity(intent);
        });

        rewardedButton.setOnClickListener(v -> {
            rewardedButton.setEnabled(false);
            displayRewardedAd();
        });
    }

    @Override
    public void onInitializationComplete() {
        Log.d("UnityAdsExample", "Unity Ads Initialization Complete");
        rewardedButton.setEnabled(true);
    }

    @Override
    public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {
        Log.e("UnityAdsExample", "Unity Ads initialization failed with error: [" + error + "] " + message);
    }

    public void displayRewardedAd() {
        UnityAds.load(adUnitId, new UnityAdsLoadListener());
    }

    private class UnityAdsLoadListener implements IUnityAdsLoadListener {
        @Override
        public void onUnityAdsAdLoaded(String placementId) {
            if (placementId.equals(adUnitId)) {
                UnityAds.show(AdActivity.this, adUnitId, new UnityAdsShowListener());
            }
        }

        @Override
        public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
            Log.e("UnityAdsExample", "Failed to load ad for " + placementId + " with error: [" + error + "] " + message);
            rewardedButton.setEnabled(true); // Re-enable button if loading fails
        }
    }

    private class UnityAdsShowListener implements IUnityAdsShowListener {
        @Override
        public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
            Log.e("UnityAdsExample", "Unity Ads failed to show ad for " + placementId + " with error: [" + error + "] " + message);
            rewardedButton.setEnabled(true); // Re-enable button
        }

        @Override
        public void onUnityAdsShowStart(String placementId) {
            Log.d("UnityAdsExample", "Ad started: " + placementId);
        }

        @Override
        public void onUnityAdsShowClick(String placementId) {
            Log.d("UnityAdsExample", "Ad clicked: " + placementId);
        }

        @Override
        public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
            if (placementId.equals(adUnitId) && state == UnityAds.UnityAdsShowCompletionState.COMPLETED) {
                Log.d("UnityAdsExample", "User should be rewarded!");
            }
            rewardedButton.setEnabled(true);
        }
    }
}
