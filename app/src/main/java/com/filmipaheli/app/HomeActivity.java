package com.filmipaheli.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private Prefs prefs;
    private PuzzleRepository repo;
    private TextView coinText, streakText, dailyStatus, streakTitle, dailyBadge;
    private Button btnDaily;
    private LinearLayout dailyTiles, weekRow, packList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Edge.apply(this);
        prefs = Prefs.get(this);
        repo = PuzzleRepository.get(this);

        coinText = findViewById(R.id.coinText);
        streakText = findViewById(R.id.streakText);
        streakTitle = findViewById(R.id.streakTitle);
        dailyStatus = findViewById(R.id.dailyStatus);
        dailyBadge = findViewById(R.id.dailyBadge);
        btnDaily = findViewById(R.id.btnDaily);
        dailyTiles = findViewById(R.id.dailyTiles);
        weekRow = findViewById(R.id.weekRow);
        packList = findViewById(R.id.packList);

        askNotificationPermissionOnce();

        findViewById(R.id.cardSpin).setOnClickListener(v -> {
            Feedback.get(this).tap();
            startActivity(new Intent(this, SpinActivity.class));
        });
        findViewById(R.id.cardAch).setOnClickListener(v -> {
            Feedback.get(this).tap();
            startActivity(new Intent(this, AchievementsActivity.class));
        });

        findViewById(R.id.btnSettings).setOnClickListener(v -> {
            Feedback.get(this).tap();
            startActivity(new Intent(this, SettingsActivity.class));
        });

        btnDaily.setOnClickListener(v -> {
            Feedback.get(this).tap();
            Puzzle d = repo.daily();
            Intent i = new Intent(this, GameActivity.class);
            i.putExtra(GameActivity.EX_PACK, d.pack);
            i.putExtra(GameActivity.EX_PUZZLE, d.id);
            i.putExtra(GameActivity.EX_DAILY, true);
            startActivity(i);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Prefs.Bonus bonus = prefs.claimLoginBonus();
        Achievements.checkAndAnnounce(this);
        refresh();
        if (bonus != null) showLoginBonus(bonus);
    }

    private void showLoginBonus(Prefs.Bonus b) {
        String msg = getString(R.string.bonus_msg, b.coins, b.streak);
        if (b.milestone > 0) msg += "\n" + getString(R.string.bonus_milestone, b.milestone);
        Feedback.get(this).win();
        new AlertDialog.Builder(this)
                .setTitle(R.string.bonus_title)
                .setMessage(msg)
                .setPositiveButton(R.string.bonus_ok, null)
                .show();
    }

    private void refresh() {
        coinText.setText(String.valueOf(prefs.coins()));
        int streak = prefs.streak();
        streakText.setText(String.valueOf(streak));
        streakTitle.setText(getResources().getQuantityString(R.plurals.streak_days, streak, streak));
        buildDailyTiles();
        buildWeek();

        boolean played = prefs.dailyPlayed();
        dailyBadge.setVisibility(played ? View.VISIBLE : View.GONE);
        btnDaily.setText(played ? R.string.daily_play_again : R.string.daily_play);
        dailyStatus.setText(getString(R.string.daily_new_in, timeToMidnight()));
        buildPacks();

        ((TextView) findViewById(R.id.spinStatus)).setText(
                prefs.freeSpinReady() ? R.string.spin_free_ready : R.string.spin_tomorrow);
        ((TextView) findViewById(R.id.achStatus)).setText(
                getString(R.string.ach_progress, prefs.achievementCount(), Achievements.ALL.size()));
    }

    private String timeToMidnight() {
        Calendar now = Calendar.getInstance();
        Calendar next = (Calendar) now.clone();
        next.add(Calendar.DAY_OF_YEAR, 1);
        next.set(Calendar.HOUR_OF_DAY, 0);
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        long mins = (next.getTimeInMillis() - now.getTimeInMillis()) / 60000;
        return String.format(Locale.US, "%02d:%02d", mins / 60, mins % 60);
    }

    /** Empty tiles that match the length of today's film, so the card hints at the puzzle. */
    private void buildDailyTiles() {
        dailyTiles.removeAllViews();
        String answer = repo.daily().answer;
        int max = 14; // keep the row on screen for long titles
        int shown = 0;
        for (char c : answer.toCharArray()) {
            if (shown++ >= max) break;
            View v = new View(this);
            if (c == ' ') {
                dailyTiles.addView(v, new LinearLayout.LayoutParams(dp(12), dp(36)));
            } else {
                v.setBackgroundResource(R.drawable.tile_preview);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(24), dp(36));
                lp.setMarginEnd(dp(4));
                dailyTiles.addView(v, lp);
            }
        }
    }

    private void buildWeek() {
        weekRow.removeAllViews();
        String[] names = getResources().getStringArray(R.array.day_letters);
        Calendar c = Calendar.getInstance();
        int mondayBased = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;
        String today = Prefs.today();
        c.add(Calendar.DAY_OF_YEAR, -mondayBased);
        for (int i = 0; i < 7; i++) {
            String key = Prefs.dateKey(c.getTimeInMillis());
            View item = LayoutInflater.from(this).inflate(R.layout.item_day, weekRow, false);
            TextView circle = item.findViewById(R.id.dayCircle);
            TextView label = item.findViewById(R.id.dayLabel);
            label.setText(names[i]);
            if (prefs.hasPlayed(key)) {
                circle.setBackgroundResource(R.drawable.bg_day_done);
                circle.setText("✓");
            } else if (key.equals(today)) {
                circle.setBackgroundResource(R.drawable.bg_day_today);
                circle.setTextColor(getColor(R.color.accent));
                circle.setText("?");
                label.setTextColor(getColor(R.color.accent));
            } else {
                circle.setBackgroundResource(R.drawable.bg_day);
            }
            weekRow.addView(item);
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    /** Shows every pack, two per row. Built by hand so none can be cut off. */
    private void buildPacks() {
        packList.removeAllViews();
        List<Pack> all = Pack.ALL;
        for (int i = 0; i < all.size(); i += 2) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int j = i; j < i + 2; j++) {
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(124), 1f);
                lp.setMargins(dp(6), dp(6), dp(6), dp(6));
                if (j < all.size()) {
                    row.addView(makePackCard(all.get(j), row), lp);
                } else {
                    row.addView(new View(this), lp); // keeps the last card the same width
                }
            }
            packList.addView(row, new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        }
    }

    private View makePackCard(Pack pack, LinearLayout parent) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_pack, parent, false);
        TextView name = card.findViewById(R.id.packName);
        TextView glyph = card.findViewById(R.id.packGlyph);
        TextView meta = card.findViewById(R.id.packMeta);
        ProgressBar bar = card.findViewById(R.id.packBar);
        View lock = card.findViewById(R.id.packLock);

        card.setBackgroundResource(pack.background);
        name.setText(pack.nameRes);
        glyph.setText(pack.glyph);

        if (!prefs.isUnlocked(pack)) {
            card.setBackgroundResource(R.drawable.bg_pack_lock);
            lock.setVisibility(View.VISIBLE);
            bar.setVisibility(View.GONE);
            meta.setText(getString(R.string.pack_unlock, pack.cost));
            name.setTextColor(getColor(R.color.dim));
            meta.setTextColor(getColor(R.color.dim));
            card.setOnClickListener(v -> {
                if (prefs.unlock(pack)) {
                    Toast.makeText(this, getString(R.string.pack_unlocked_toast, getString(pack.nameRes)),
                            Toast.LENGTH_SHORT).show();
                    refresh();
                } else {
                    Toast.makeText(this, getString(R.string.pack_need_coins, pack.cost),
                            Toast.LENGTH_SHORT).show();
                }
            });
            return card;
        }

        List<Puzzle> list = repo.inPack(pack.id);
        int solved = prefs.solvedCount(list);
        lock.setVisibility(View.GONE);
        bar.setVisibility(View.VISIBLE);
        bar.setMax(Math.max(1, list.size()));
        bar.setProgress(solved);
        meta.setText(solved == 0
                ? getString(R.string.pack_new, list.size())
                : getString(R.string.pack_progress, solved, list.size()));
        card.setOnClickListener(v -> {
            Feedback.get(this).tap();
            Intent i = new Intent(this, GameActivity.class);
            i.putExtra(GameActivity.EX_PACK, pack.id);
            startActivity(i);
        });
        return card;
    }

    /** On Android 13 and newer, asks once so the daily reminder can show. */
    private void askNotificationPermissionOnce() {
        if (prefs.reminderAsked()) return;
        prefs.setReminderAsked();
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
