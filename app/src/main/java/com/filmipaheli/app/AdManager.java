package com.filmipaheli.app;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

/**
 * Loads and shows AdMob rewarded and interstitial ads.
 * The unit ids below are Google's public TEST ids. Swap them for your own before release.
 */
public final class AdManager {

    private static final String REWARDED_ID = "ca-app-pub-3940256099942544/5224354917";
    private static final String INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712";

    private static AdManager instance;

    private final Context app;
    private RewardedAd rewarded;
    private InterstitialAd interstitial;
    private boolean loadingRewarded;
    private boolean loadingInterstitial;

    public static synchronized AdManager get(Context ctx) {
        if (instance == null) instance = new AdManager(ctx.getApplicationContext());
        return instance;
    }

    private AdManager(Context app) {
        this.app = app;
    }

    public void init() {
        MobileAds.initialize(app, status -> {
            loadRewarded();
            loadInterstitial();
        });
    }

    private void loadRewarded() {
        if (loadingRewarded || rewarded != null) return;
        loadingRewarded = true;
        RewardedAd.load(app, REWARDED_ID, new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                rewarded = ad;
                loadingRewarded = false;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                rewarded = null;
                loadingRewarded = false;
            }
        });
    }

    private void loadInterstitial() {
        if (loadingInterstitial || interstitial != null) return;
        loadingInterstitial = true;
        InterstitialAd.load(app, INTERSTITIAL_ID, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd ad) {
                interstitial = ad;
                loadingInterstitial = false;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                interstitial = null;
                loadingInterstitial = false;
            }
        });
    }

    /** Shows a rewarded ad. 'onReward' runs after the ad closes, only if the viewer earned the reward. */
    public void showRewarded(Activity activity, Runnable onReward) {
        if (rewarded == null) {
            Toast.makeText(activity, "Ad is not ready yet. Try again in a moment.", Toast.LENGTH_SHORT).show();
            loadRewarded();
            return;
        }
        final RewardedAd ad = rewarded;
        rewarded = null;
        final boolean[] earned = {false};
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                loadRewarded();
                if (earned[0]) onReward.run();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError error) {
                loadRewarded();
                Toast.makeText(activity, "Could not show the ad. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
        ad.show(activity, item -> earned[0] = true);
    }

    /** Shows an interstitial if one is ready. 'onDone' always runs, so the game never gets stuck. */
    public void showInterstitial(Activity activity, Runnable onDone) {
        if (interstitial == null) {
            loadInterstitial();
            onDone.run();
            return;
        }
        final InterstitialAd ad = interstitial;
        interstitial = null;
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                loadInterstitial();
                onDone.run();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError error) {
                loadInterstitial();
                onDone.run();
            }
        });
        ad.show(activity);
    }
}
