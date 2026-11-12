package com.filmipaheli.app;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private Prefs prefs;
    private PuzzleRepository repo;
    private TextView coinText, streakText, dailyStatus, streakTitle;
    private Button btnDaily;
    private LinearLayout dailyTiles, weekRow;
    private RecyclerView.Adapter<?> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        prefs = Prefs.get(this);
        repo = PuzzleRepository.get(this);

        coinText = findViewById(R.id.coinText);
        streakText = findViewById(R.id.streakText);
        streakTitle = findViewById(R.id.streakTitle);
        dailyStatus = findViewById(R.id.dailyStatus);
        btnDaily = findViewById(R.id.btnDaily);
        dailyTiles = findViewById(R.id.dailyTiles);
        weekRow = findViewById(R.id.weekRow);

        RecyclerView rv = findViewById(R.id.packList);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        rv.setNestedScrollingEnabled(false);
        adapter = new PackAdapter();
        rv.setAdapter(adapter);

        btnDaily.setOnClickListener(v -> {
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
        refresh();
    }

    private void refresh() {
        coinText.setText(String.valueOf(prefs.coins()));
        int streak = prefs.streak();
        streakText.setText(String.valueOf(streak));
        streakTitle.setText(streak == 1 ? "Your streak: 1 day" : "Your streak: " + streak + " days");
        buildDailyTiles();
        buildWeek();

        boolean done = prefs.dailyDone();
        btnDaily.setText(done ? "Play again" : "Play · +50");
        dailyStatus.setText(done ? "Done today" : "New film in " + timeToMidnight());
        adapter.notifyDataSetChanged();
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
        String[] names = {"M", "T", "W", "T", "F", "S", "S"};
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
                circle.setTextColor(getColor(R.color.marigold));
                circle.setText("?");
                label.setTextColor(getColor(R.color.marigold));
            } else {
                circle.setBackgroundResource(R.drawable.bg_day);
            }
            weekRow.addView(item);
            c.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    // ---------------------------------------------------------------- packs

    private class PackAdapter extends RecyclerView.Adapter<PackAdapter.Holder> {

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pack, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder h, int position) {
            Pack pack = Pack.ALL.get(position);
            h.root.setBackgroundResource(pack.background);
            h.name.setText(pack.name);
            h.glyph.setText(pack.glyph);

            if (!prefs.isUnlocked(pack)) {
                h.root.setBackgroundResource(R.drawable.bg_pack_lock);
                h.lock.setVisibility(View.VISIBLE);
                h.bar.setVisibility(View.GONE);
                h.meta.setText("Unlock for " + pack.cost + " coins");
                h.name.setTextColor(getColor(R.color.dim));
                h.meta.setTextColor(getColor(R.color.dim));
                h.root.setOnClickListener(v -> {
                    if (prefs.unlock(pack)) {
                        Toast.makeText(HomeActivity.this, pack.name + " unlocked!", Toast.LENGTH_SHORT).show();
                        refresh();
                    } else {
                        Toast.makeText(HomeActivity.this,
                                "You need " + pack.cost + " coins. Solve more films to earn them.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }

            List<Puzzle> list = repo.inPack(pack.id);
            int solved = prefs.solvedCount(list);
            h.lock.setVisibility(View.GONE);
            h.bar.setVisibility(View.VISIBLE);
            h.bar.setMax(Math.max(1, list.size()));
            h.bar.setProgress(solved);
            h.name.setTextColor(getColor(R.color.cream));
            h.meta.setTextColor(0xD9FFF0D2);
            h.meta.setText(solved == 0 ? "New · " + list.size() + " films" : solved + " / " + list.size() + " solved");
            h.root.setOnClickListener(v -> {
                Intent i = new Intent(HomeActivity.this, GameActivity.class);
                i.putExtra(GameActivity.EX_PACK, pack.id);
                startActivity(i);
            });
        }

        @Override
        public int getItemCount() {
            return Pack.ALL.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            final View root;
            final TextView name, glyph, meta;
            final ProgressBar bar;
            final View lock;

            Holder(@NonNull View v) {
                super(v);
                root = v.findViewById(R.id.packRoot);
                name = v.findViewById(R.id.packName);
                glyph = v.findViewById(R.id.packGlyph);
                meta = v.findViewById(R.id.packMeta);
                bar = v.findViewById(R.id.packBar);
                lock = v.findViewById(R.id.packLock);
            }
        }
    }
}
