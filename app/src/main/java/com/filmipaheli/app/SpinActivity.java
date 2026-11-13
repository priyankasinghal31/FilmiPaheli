package com.filmipaheli.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** One free spin a day, and up to three more for a rewarded ad each. */
public class SpinActivity extends AppCompatActivity {

    private Prefs prefs;
    private WheelView wheel;
    private TextView coinText, result;
    private Button btnFree, btnAd;
    private boolean spinning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spin);
        prefs = Prefs.get(this);

        wheel = findViewById(R.id.wheel);
        coinText = findViewById(R.id.coinText);
        result = findViewById(R.id.spinResult);
        btnFree = findViewById(R.id.btnSpinFree);
        btnAd = findViewById(R.id.btnSpinAd);

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            Feedback.get(this).tap();
            finish();
        });

        btnFree.setOnClickListener(v -> {
            if (spinning || !prefs.freeSpinReady()) return;
            prefs.useFreeSpin();
            spin();
        });
        btnAd.setOnClickListener(v -> {
            if (spinning || prefs.adSpinsLeft() == 0) return;
            AdManager.get(this).showRewarded(this, () -> {
                prefs.useAdSpin();
                spin();
            });
        });
        refresh();
    }

    private void spin() {
        spinning = true;
        result.setText(R.string.spin_prompt);
        refresh();
        int index = wheel.pickIndex();
        wheel.spinTo(index, () -> {
            int won = WheelView.PRIZES[index];
            prefs.addCoins(won);
            prefs.addStat("spin");
            spinning = false;
            Feedback.get(this).win();
            result.setText(getString(R.string.spin_won, won));
            refresh();
            Achievements.checkAndAnnounce(this);
            coinText.setText(String.valueOf(prefs.coins()));
        });
    }

    private void refresh() {
        coinText.setText(String.valueOf(prefs.coins()));
        btnFree.setEnabled(!spinning && prefs.freeSpinReady());
        btnFree.setAlpha(btnFree.isEnabled() ? 1f : 0.45f);
        int left = prefs.adSpinsLeft();
        btnAd.setText(getString(R.string.spin_ad_btn, left));
        btnAd.setEnabled(!spinning && left > 0);
        btnAd.setAlpha(btnAd.isEnabled() ? 1f : 0.45f);
    }
}
