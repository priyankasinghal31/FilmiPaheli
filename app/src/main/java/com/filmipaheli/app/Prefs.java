package com.filmipaheli.app;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/** Saved coins, solved puzzles, play days and the ad counter. */
public final class Prefs {

    private static final int START_COINS = 100;

    private final SharedPreferences sp;

    public static Prefs get(Context ctx) {
        return new Prefs(ctx.getApplicationContext());
    }

    private Prefs(Context ctx) {
        sp = ctx.getSharedPreferences("filmi", Context.MODE_PRIVATE);
    }

    // ---- coins ----
    public int coins() {
        return sp.getInt("coins", START_COINS);
    }

    public void addCoins(int n) {
        sp.edit().putInt("coins", Math.max(0, coins() + n)).apply();
    }

    public boolean spendCoins(int n) {
        if (coins() < n) return false;
        addCoins(-n);
        return true;
    }

    // ---- solved puzzles ----
    private Set<String> set(String key) {
        return new HashSet<>(sp.getStringSet(key, new HashSet<String>()));
    }

    public boolean isSolved(String puzzleId) {
        return set("solved").contains(puzzleId);
    }

    public int solvedCount(Iterable<Puzzle> puzzles) {
        int n = 0;
        for (Puzzle p : puzzles) if (isSolved(p.id)) n++;
        return n;
    }

    public void markSolved(String puzzleId) {
        Set<String> s = set("solved");
        s.add(puzzleId);
        sp.edit().putStringSet("solved", s).apply();
    }

    // ---- days played and streak ----
    public static String dateKey(long millis) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(millis);
    }

    public static String today() {
        return dateKey(System.currentTimeMillis());
    }

    public boolean hasPlayed(String dateKey) {
        return set("days").contains(dateKey);
    }

    public void markPlayedToday() {
        Set<String> s = set("days");
        s.add(today());
        sp.edit().putStringSet("days", s).apply();
    }

    /** Consecutive days played, ending today (or yesterday if today is still open). */
    public int streak() {
        Set<String> days = set("days");
        Calendar c = Calendar.getInstance();
        if (!days.contains(dateKey(c.getTimeInMillis()))) c.add(Calendar.DAY_OF_YEAR, -1);
        int n = 0;
        while (days.contains(dateKey(c.getTimeInMillis()))) {
            n++;
            c.add(Calendar.DAY_OF_YEAR, -1);
        }
        return n;
    }

    // ---- daily puzzle bonus ----
    public boolean dailyDone() {
        return today().equals(sp.getString("dailyDone", ""));
    }

    public void markDailyDone() {
        sp.edit().putString("dailyDone", today()).apply();
    }
}
