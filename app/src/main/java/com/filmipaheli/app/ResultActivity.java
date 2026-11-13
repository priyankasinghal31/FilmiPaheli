package com.filmipaheli.app;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/** Shown after a film is solved: stars, coins, the film name, share and next puzzle. */
public class ResultActivity extends AppCompatActivity {

    public static final String EX_STARS = "stars";
    public static final String EX_GAIN = "gain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        Edge.apply(this);
        // Back goes home. This replaces onBackPressed(), which Android 16 no longer calls.
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                goHome();
            }
        });

        Prefs prefs = Prefs.get(this);
        PuzzleRepository repo = PuzzleRepository.get(this);

        Intent in = getIntent();
        int stars = in.getIntExtra(EX_STARS, 1);
        int gain = in.getIntExtra(EX_GAIN, 0);
        String packId = in.getStringExtra(GameActivity.EX_PACK);
        String puzzleId = in.getStringExtra(GameActivity.EX_PUZZLE);
        boolean daily = in.getBooleanExtra(GameActivity.EX_DAILY, false);
        Puzzle puzzle = repo.byId(puzzleId);

        ((TextView) findViewById(R.id.rTitle)).setText(
                stars == 3 ? R.string.result_win3 : stars == 2 ? R.string.result_win2 : R.string.result_win1);
        ((TextView) findViewById(R.id.rSmall)).setText(R.string.result_was_win);
        ((TextView) findViewById(R.id.rAnswer)).setText(puzzle.answer);
        ((TextView) findViewById(R.id.rInfo)).setText(puzzle.category + ", " + puzzle.year);

        View reward = findViewById(R.id.rReward);
        reward.setVisibility(gain > 0 ? View.VISIBLE : View.GONE);
        ((TextView) findViewById(R.id.rGain)).setText("+" + gain);

        drawStars(stars);
        Feedback.get(this).win();
        Achievements.checkAndAnnounce(this);

        // Happy moment, so ask for a rating (at 5, 25 and 75 solved films).
        if (stars >= 2 && prefs.shouldAskRating()) {
            new Handler(Looper.getMainLooper()).postDelayed(this::askForRating, 1600);
        }

        Button next = findViewById(R.id.btnNext);
        next.setText(daily ? R.string.back_home : R.string.next_puzzle);
        next.setOnClickListener(v -> {
            Feedback.get(this).tap();
            if (daily) {
                goHome();
                return;
            }
            Puzzle n = repo.next(packId, puzzleId, prefs);
            Intent i = new Intent(this, GameActivity.class);
            i.putExtra(GameActivity.EX_PACK, n.pack);
            i.putExtra(GameActivity.EX_PUZZLE, n.id);
            startActivity(i);
            finish();
        });

        findViewById(R.id.btnShare).setOnClickListener(v -> {
            Feedback.get(this).tap();
            prefs.addStat("share");
            Achievements.checkAndAnnounce(this);
            shareResult(stars, daily, packId);
        });

        findViewById(R.id.btnHome).setOnClickListener(v -> {
            Feedback.get(this).tap();
            goHome();
        });
    }

    private void askForRating() {
        if (isFinishing() || isDestroyed()) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.rate_title)
                .setMessage(R.string.rate_message)
                .setPositiveButton(R.string.rate_now, (d, w) -> {
                    Prefs.get(this).setRateDone();
                    openStorePage();
                })
                .setNegativeButton(R.string.rate_later, null)
                .show();
    }

    private void openStorePage() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    /** Shares a message that does not reveal the answer. Hearts left equals stars earned. */
    private void shareResult(int hearts, boolean daily, String packId) {
        StringBuilder h = new StringBuilder();
        for (int i = 0; i < 3; i++) h.append(i < hearts ? "❤️" : "🖤");
        String link = "https://play.google.com/store/apps/details?id=" + getPackageName();
        String text = daily
                ? getString(R.string.share_daily, h, link)
                : getString(R.string.share_pack, getString(Pack.byId(packId).nameRes), h, link);
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(Intent.createChooser(send, getString(R.string.share_chooser)));
    }

    private void drawStars(int earned) {
        LinearLayout box = findViewById(R.id.rStars);
        box.removeAllViews();
        int size = Math.round(40 * getResources().getDisplayMetrics().density);
        int gap = Math.round(4 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < 3; i++) {
            ImageView s = new ImageView(this);
            s.setImageResource(R.drawable.ic_star);
            s.setColorFilter(getColor(i < earned ? R.color.marigold : R.color.outline));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
            lp.setMargins(gap, 0, gap, 0);
            box.addView(s, lp);
            s.setScaleX(0f);
            s.setScaleY(0f);
            s.animate().scaleX(1f).scaleY(1f).setStartDelay(150L * i).setDuration(350)
                    .setInterpolator(new android.view.animation.OvershootInterpolator(3f)).start();
        }
    }

    private void goHome() {
        Intent i = new Intent(this, HomeActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
        finish();
    }
}
