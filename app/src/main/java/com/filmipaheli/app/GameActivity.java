package com.filmipaheli.app;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** The puzzle screen: clue ticket, blanks, hints and the on-screen keyboard. */
public class GameActivity extends AppCompatActivity {

    public static final String EX_PACK = "pack";
    public static final String EX_PUZZLE = "puzzle";
    public static final String EX_DAILY = "daily";
    public static final String EX_LIVES = "lives";
    public static final String EX_USED = "used";

    private static final int MAX_LIVES = 3;
    private static final int HINT_COST = 20;
    private static final String[] ROWS = {"QWERTYUIOP", "ASDFGHJKL", "ZXCVBNM"};

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Map<Character, Button> keys = new HashMap<>();
    private final Set<Character> used = new HashSet<>();     // every letter tapped
    private final Set<Character> hinted = new HashSet<>();   // letters revealed by a hint

    private Prefs prefs;
    private PuzzleRepository repo;
    private Puzzle puzzle;
    private boolean daily;
    private boolean locked;
    private int lives = MAX_LIVES;

    private LinearLayout livesBox, answerBox, keyboard;
    private TextView coinText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        prefs = Prefs.get(this);
        repo = PuzzleRepository.get(this);

        Intent in = getIntent();
        String packId = in.getStringExtra(EX_PACK);
        String puzzleId = in.getStringExtra(EX_PUZZLE);
        daily = in.getBooleanExtra(EX_DAILY, false);
        puzzle = puzzleId != null ? repo.byId(puzzleId) : repo.firstToPlay(packId, prefs);
        lives = in.getIntExtra(EX_LIVES, MAX_LIVES);

        // This film was lost earlier and the ad was not watched yet. Back to the ad screen.
        if (puzzle.id.equals(prefs.pendingLossId()) && in.getStringExtra(EX_USED) == null) {
            Intent r = new Intent(this, ResultActivity.class);
            r.putExtra(ResultActivity.EX_WIN, false);
            r.putExtra(EX_PACK, puzzle.pack);
            r.putExtra(EX_PUZZLE, puzzle.id);
            r.putExtra(EX_DAILY, daily);
            r.putExtra(EX_USED, prefs.pendingLossUsed());
            startActivity(r);
            finish();
            return;
        }

        // restore letters from a previous attempt (used after "watch ad for an extra life")
        String saved = in.getStringExtra(EX_USED);
        if (saved != null) for (char c : saved.toCharArray()) used.add(c);

        livesBox = findViewById(R.id.lives);
        answerBox = findViewById(R.id.answerBox);
        keyboard = findViewById(R.id.keyboard);
        coinText = findViewById(R.id.coinText);

        ((TextView) findViewById(R.id.packTitle)).setText(daily ? getString(R.string.daily_puzzle) : getString(Pack.byId(puzzle.pack).nameRes));
        List<Puzzle> inPack = repo.inPack(puzzle.pack);
        int index = 0;
        for (int i = 0; i < inPack.size(); i++) if (inPack.get(i).id.equals(puzzle.id)) index = i;
        ((TextView) findViewById(R.id.countText)).setText(
                daily ? getString(R.string.daily_puzzle) : getString(R.string.puzzle_count, index + 1, inPack.size()));
        ((TextView) findViewById(R.id.emojiText)).setText(puzzle.emoji);
        boolean hindi = "hi".equals(getResources().getConfiguration().getLocales().get(0).getLanguage());
        ((TextView) findViewById(R.id.clueText)).setText(puzzle.clueFor(hindi));
        ((TextView) findViewById(R.id.chipCat)).setText(puzzle.category);
        ((TextView) findViewById(R.id.chipYear)).setText(puzzle.year);

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            Feedback.get(this).tap();
            finish();
        });
        findViewById(R.id.hintCoin).setOnClickListener(v -> onCoinHint());
        findViewById(R.id.hintAd).setOnClickListener(v -> onAdHint());

        buildKeyboard();
        refreshAll();
        if (isSolved() || lives <= 0) finishRound(); // cannot happen normally, but stay safe
    }

    // ------------------------------------------------------------ game rules

    private boolean isSolved() {
        for (char c : puzzle.letters().toCharArray()) if (!used.contains(c)) return false;
        return true;
    }

    private void guess(char c, boolean fromHint) {
        if (locked || used.contains(c)) return;
        used.add(c);
        boolean hit = puzzle.answer.indexOf(c) >= 0;
        if (hit) {
            if (fromHint) hinted.add(c);
            Feedback.get(this).correct();
            renderAnswer(c);
        } else {
            lives--;
            Feedback.get(this).wrong();
            renderLives();
            shake(livesBox);
        }
        updateKey(c);
        if (isSolved() || lives <= 0) {
            locked = true;
            handler.postDelayed(this::finishRound, 750);
        }
    }

    private void onCoinHint() {
        if (locked) return;
        if (!prefs.spendCoins(HINT_COST)) {
            Toast.makeText(this, R.string.toast_no_coins, Toast.LENGTH_SHORT).show();
            shake(coinText);
            return;
        }
        coinText.setText(String.valueOf(prefs.coins()));
        revealRandomLetter();
    }

    private void onAdHint() {
        if (locked) return;
        AdManager.get(this).showRewarded(this, this::revealRandomLetter);
    }

    private void revealRandomLetter() {
        List<Character> left = new ArrayList<>();
        for (char c : puzzle.letters().toCharArray()) if (!used.contains(c) && !left.contains(c)) left.add(c);
        if (left.isEmpty()) return;
        guess(left.get((int) (Math.random() * left.size())), true);
    }

    /** Works out stars and coins, saves progress, then opens the result screen. */
    private void finishRound() {
        if (isFinishing()) return;
        boolean win = isSolved();
        int stars = 0, gain = 0;
        if (win) {
            stars = lives >= 3 ? 3 : lives == 2 ? 2 : 1;
            boolean firstTime = !prefs.isSolved(puzzle.id);
            if (firstTime) gain += 10 * stars;
            if (daily) prefs.markDailyPlayed();
        if (win && daily && !prefs.dailyDone()) {
                gain += 50;
                prefs.markDailyDone();
            }
            prefs.markSolved(puzzle.id);
            prefs.markPlayedToday();
            prefs.addCoins(gain);
        }
        StringBuilder usedLetters = new StringBuilder();
        for (char c : used) usedLetters.append(c);

        if (win) prefs.clearPendingLoss();
        else prefs.setPendingLoss(puzzle.id, usedLetters.toString());

        Intent i = new Intent(this, ResultActivity.class);
        i.putExtra(ResultActivity.EX_WIN, win);
        i.putExtra(ResultActivity.EX_STARS, stars);
        i.putExtra(ResultActivity.EX_GAIN, gain);
        i.putExtra(EX_PACK, puzzle.pack);
        i.putExtra(EX_PUZZLE, puzzle.id);
        i.putExtra(EX_DAILY, daily);
        i.putExtra(EX_USED, usedLetters.toString());
        startActivity(i);
        finish();
    }

    // ------------------------------------------------------------- rendering

    private void refreshAll() {
        coinText.setText(String.valueOf(prefs.coins()));
        renderLives();
        renderAnswer(null);
        for (char c : keys.keySet()) updateKey(c);
    }

    private void renderLives() {
        livesBox.removeAllViews();
        for (int i = 0; i < MAX_LIVES; i++) {
            ImageView h = new ImageView(this);
            h.setImageResource(R.drawable.ic_heart);
            h.setColorFilter(getColor(i < lives ? R.color.rani : R.color.outline));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(22), dp(22));
            lp.setMargins(dp(3), 0, dp(3), 0);
            livesBox.addView(h, lp);
        }
    }

    /** Lays the answer out as tiles, packing whole words into rows that fit the screen. */
    private void renderAnswer(Character pop) {
        answerBox.removeAllViews();
        String[] words = puzzle.answer.split(" ");
        int avail = getResources().getDisplayMetrics().widthPixels - dp(28);
        int gap = dp(6), wordGap = dp(18);

        int longest = 1;
        for (String w : words) longest = Math.max(longest, w.length());
        int tile = Math.min(dp(38), (avail - (longest - 1) * gap) / longest);
        int tileH = Math.round(tile * 1.2f);

        LinearLayout row = newRow();
        int rowWidth = 0;
        for (String w : words) {
            int wWidth = w.length() * tile + (w.length() - 1) * gap;
            boolean empty = row.getChildCount() == 0;
            if (!empty && rowWidth + wordGap + wWidth > avail) {
                answerBox.addView(row);
                row = newRow();
                rowWidth = 0;
                empty = true;
            }
            LinearLayout word = new LinearLayout(this);
            word.setOrientation(LinearLayout.HORIZONTAL);
            for (char c : w.toCharArray()) {
                TextView t = new TextView(this);
                t.setGravity(Gravity.CENTER);
                t.setTypeface(Typeface.DEFAULT_BOLD);
                t.setTextSize(tile >= dp(34) ? 22 : 18);
                t.setTextColor(getColor(R.color.ink));
                boolean shown = used.contains(c);
                if (shown) {
                    t.setText(String.valueOf(c));
                    t.setBackgroundResource(hinted.contains(c) ? R.drawable.tile_hint : R.drawable.tile_on);
                    if (pop != null && pop == c) popIn(t);
                } else {
                    t.setBackgroundResource(R.drawable.tile_empty);
                }
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(tile, tileH);
                lp.setMarginEnd(gap);
                word.addView(t, lp);
            }
            LinearLayout.LayoutParams wl = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            if (!empty) wl.setMarginStart(wordGap - gap);
            row.addView(word, wl);
            rowWidth += (empty ? 0 : wordGap) + wWidth;
        }
        answerBox.addView(row);
    }

    private LinearLayout newRow() {
        LinearLayout r = new LinearLayout(this);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER);
        r.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        r.setPadding(0, 0, 0, dp(6));
        return r;
    }

    private void buildKeyboard() {
        float[] pad = {0f, 0.5f, 1.5f}; // spacer weight at each end, so shorter rows stay centred
        for (int r = 0; r < ROWS.length; r++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams rl = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(46));
            rl.topMargin = r == 0 ? 0 : dp(7);
            if (pad[r] > 0) addSpacer(row, pad[r]);
            for (char c : ROWS[r].toCharArray()) {
                Button b = new Button(this);
                b.setText(String.valueOf(c));
                b.setAllCaps(false);
                b.setTextSize(17);
                b.setTypeface(Typeface.DEFAULT_BOLD);
                b.setTextColor(getColor(R.color.ink));
                b.setBackgroundResource(R.drawable.key_bg);
                b.setStateListAnimator(null);
                b.setPadding(0, 0, 0, dp(3));
                b.setMinWidth(0);
                b.setMinHeight(0);
                b.setOnClickListener(v -> guess(c, false));
                LinearLayout.LayoutParams kl = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                kl.setMargins(dp(2), 0, dp(2), 0);
                row.addView(b, kl);
                keys.put(c, b);
            }
            if (pad[r] > 0) addSpacer(row, pad[r]);
            keyboard.addView(row, rl);
        }
    }

    private void addSpacer(LinearLayout row, float weight) {
        View s = new View(this);
        row.addView(s, new LinearLayout.LayoutParams(0, 1, weight));
    }

    private void updateKey(char c) {
        Button b = keys.get(c);
        if (b == null) return;
        if (!used.contains(c)) {
            b.setEnabled(true);
            return;
        }
        boolean hit = puzzle.answer.indexOf(c) >= 0;
        b.setEnabled(false);
        b.setBackgroundResource(hit ? R.drawable.key_ok : R.drawable.key_no);
        b.setTextColor(getColor(hit ? R.color.ink : R.color.outline));
    }

    // -------------------------------------------------------------- helpers

    private void popIn(View v) {
        v.setScaleX(0.5f);
        v.setScaleY(0.5f);
        v.animate().scaleX(1f).scaleY(1f).setDuration(300)
                .setInterpolator(new android.view.animation.OvershootInterpolator(3f)).start();
    }

    private void shake(View v) {
        ObjectAnimator.ofFloat(v, "translationX", 0, -dp(6), dp(6), -dp(4), dp(4), 0)
                .setDuration(350).start();
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
