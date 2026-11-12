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
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

/**
 * Loads and shows AdMob rewarded ads. Ads only appear when the player taps a button to watch one.
 * The ad unit id comes from BuildConfig: Google's test id in debug builds, your real id in release.
 */
public final class AdManager {

    private static AdManager instance;

    private final Context app;
    private RewardedAd rewarded;
    private boolean loading;

    public static synchronized AdManager get(Context ctx) {
        if (instance == null) instance = new AdManager(ctx.getApplicationContext());
        return instance;
    }

    private AdManager(Context app) {
        this.app = app;
    }

    public void init() {
        MobileAds.initialize(app, status -> loadRewarded());
    }

    private void loadRewarded() {
        if (loading || rewarded != null) return;
        loading = true;
        RewardedAd.load(app, BuildConfig.REWARDED_ID, new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                rewarded = ad;
                loading = false;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                rewarded = null;
                loading = false;
            }
        });
    }

    /** Shows a rewarded ad. 'onReward' runs after the ad closes, only if the viewer earned the reward. */
    public void showRewarded(Activity activity, Runnable onReward) {
        if (rewarded == null) {
            Toast.makeText(activity, app.getString(R.string.ad_not_ready), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(activity, app.getString(R.string.ad_failed), Toast.LENGTH_SHORT).show();
            }
        });
        ad.show(activity, item -> earned[0] = true);
    }
}
