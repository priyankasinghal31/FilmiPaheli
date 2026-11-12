package com.filmipaheli.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/** Shown after a round: stars and coins on a win, or an extra-life offer on a loss. */
public class ResultActivity extends AppCompatActivity {

    public static final String EX_WIN = "win";
    public static final String EX_STARS = "stars";
    public static final String EX_GAIN = "gain";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Prefs prefs = Prefs.get(this);
        PuzzleRepository repo = PuzzleRepository.get(this);

        Intent in = getIntent();
        boolean win = in.getBooleanExtra(EX_WIN, false);
        int stars = in.getIntExtra(EX_STARS, 0);
        int gain = in.getIntExtra(EX_GAIN, 0);
        String packId = in.getStringExtra(GameActivity.EX_PACK);
        String puzzleId = in.getStringExtra(GameActivity.EX_PUZZLE);
        boolean daily = in.getBooleanExtra(GameActivity.EX_DAILY, false);
        String used = in.getStringExtra(GameActivity.EX_USED);
        Puzzle puzzle = repo.byId(puzzleId);

        TextView title = findViewById(R.id.rTitle);
        TextView small = findViewById(R.id.rSmall);
        Button next = findViewById(R.id.btnNext);
        Button retry = findViewById(R.id.btnRetry);
        View reward = findViewById(R.id.rReward);

        ((TextView) findViewById(R.id.rAnswer)).setText(puzzle.answer);
        ((TextView) findViewById(R.id.rInfo)).setText(puzzle.category + ", " + puzzle.year);

        if (win) {
            title.setText(stars == 3 ? "Blockbuster!" : stars == 2 ? "Hit film!" : "Just made it");
            small.setText("The film was");
            next.setText(daily ? "Back to home" : "Next puzzle");
            reward.setVisibility(gain > 0 ? View.VISIBLE : View.GONE);
            ((TextView) findViewById(R.id.rGain)).setText("+" + gain);
        } else {
            title.setText("Flop show");
            title.setTextColor(getColor(R.color.rani));
            small.setText("It was");
            next.setText("Try the next film");
            reward.setVisibility(View.GONE);
            retry.setVisibility(View.VISIBLE);
        }
        drawStars(stars);

        // Extra life: watch a rewarded ad, then return to the same puzzle with 1 heart and the same letters.
        retry.setOnClickListener(v -> AdManager.get(this).showRewarded(this, () -> {
            Intent i = new Intent(this, GameActivity.class);
            i.putExtra(GameActivity.EX_PACK, packId);
            i.putExtra(GameActivity.EX_PUZZLE, puzzleId);
            i.putExtra(GameActivity.EX_DAILY, daily);
            i.putExtra(GameActivity.EX_LIVES, 1);
            i.putExtra(GameActivity.EX_USED, used);
            startActivity(i);
            finish();
        }));

        next.setOnClickListener(v -> {
            if (daily && win) {
                goHome();
                return;
            }
            Puzzle n = repo.next(packId, puzzleId, prefs);
            Runnable go = () -> {
                Intent i = new Intent(this, GameActivity.class);
                i.putExtra(GameActivity.EX_PACK, n.pack);
                i.putExtra(GameActivity.EX_PUZZLE, n.id);
                startActivity(i);
                finish();
            };
            // short ad break after every 4th finished puzzle
            if (prefs.countPuzzleAndCheckAdBreak()) {
                AdManager.get(this).showInterstitial(this, go);
            } else {
                go.run();
            }
        });

        findViewById(R.id.btnHome).setOnClickListener(v -> goHome());
    }

    private void drawStars(int earned) {
        LinearLayout box = findViewById(R.id.rStars);
        box.removeAllViews();
        int size = Math.round(40 * getResources().getDisplayMetrics().density);
        int gap = Math.round(4 * getResources().getDisplayMetrics().density);
        for (int i = 0; i < 3; i++) {
            ImageView s = new ImageView(this);
            s.setImageResource(R.drawable.ic_star);
            s.setColorFilter(getColor(i < earned ? R.color.marigold : R.color.line));
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

    @Override
    public void onBackPressed() {
        goHome();
    }
}
