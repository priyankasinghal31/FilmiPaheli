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

    // ---- unlocked packs ----
    public boolean isUnlocked(Pack pack) {
        return pack.cost == 0 || set("unlocked").contains(pack.id);
    }

    /** Spends the pack's cost and unlocks it. Returns false if the player cannot afford it. */
    public boolean unlock(Pack pack) {
        if (!spendCoins(pack.cost)) return false;
        Set<String> s = set("unlocked");
        s.add(pack.id);
        sp.edit().putStringSet("unlocked", s).apply();
        return true;
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

    // ---- a lost puzzle waits for an ad before it can be played again ----
    public void setPendingLoss(String puzzleId, String usedLetters) {
        sp.edit().putString("lossId", puzzleId).putString("lossUsed", usedLetters).apply();
    }

    public String pendingLossId() {
        return sp.getString("lossId", null);
    }

    public String pendingLossUsed() {
        return sp.getString("lossUsed", "");
    }

    public void clearPendingLoss() {
        sp.edit().remove("lossId").remove("lossUsed").apply();
    }

    // ---- sound, vibration, reminder, language ----
    public boolean soundOn() {
        return sp.getBoolean("sound", true);
    }

    public void setSoundOn(boolean on) {
        sp.edit().putBoolean("sound", on).apply();
    }

    public boolean vibrateOn() {
        return sp.getBoolean("vibrate", true);
    }

    public void setVibrateOn(boolean on) {
        sp.edit().putBoolean("vibrate", on).apply();
    }

    public boolean reminderOn() {
        return sp.getBoolean("reminder", true);
    }

    public void setReminderOn(boolean on) {
        sp.edit().putBoolean("reminder", on).apply();
    }

    public boolean reminderAsked() {
        return sp.getBoolean("reminderAsked", false);
    }

    public void setReminderAsked() {
        sp.edit().putBoolean("reminderAsked", true).apply();
    }

    /** "en" or "hi". Defaults to the phone's language. */
    public String language() {
        String saved = sp.getString("language", null);
        if (saved != null) return saved;
        return "hi".equals(java.util.Locale.getDefault().getLanguage()) ? "hi" : "en";
    }

    public void setLanguage(String lang) {
        sp.edit().putString("language", lang).apply();
    }

    // ---- theme: "system", "light" or "dark" ----
    public String theme() {
        return sp.getString("theme", "system");
    }

    public void setTheme(String theme) {
        sp.edit().putString("theme", theme).apply();
    }

    /** Applies the saved theme. Call once at app start and again after it changes. */
    public void applyTheme() {
        int mode;
        switch (theme()) {
            case "light":
                mode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case "dark":
                mode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
                break;
            default:
                mode = androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(mode);
    }

    // ---- daily puzzle: played today (win or lose) and bonus ----
    public boolean dailyPlayed() {
        return today().equals(sp.getString("dailyPlayed", ""));
    }

    public void markDailyPlayed() {
        sp.edit().putString("dailyPlayed", today()).apply();
    }

    // ---- daily puzzle bonus ----
    public boolean dailyDone() {
        return today().equals(sp.getString("dailyDone", ""));
    }

    public void markDailyDone() {
        sp.edit().putString("dailyDone", today()).apply();
    }

    // ---- film download throttle ----
    public long lastContentCheck() {
        return sp.getLong("contentCheck", 0L);
    }

    public void setLastContentCheck(long millis) {
        sp.edit().putLong("contentCheck", millis).apply();
    }

    // ---- counters used by achievements ----
    public int stat(String key) {
        return sp.getInt("stat_" + key, 0);
    }

    public void addStat(String key) {
        sp.edit().putInt("stat_" + key, stat(key) + 1).apply();
    }

    public int solvedTotal() {
        return set("solved").size();
    }

    // ---- achievements ----
    public boolean hasAchievement(String id) {
        return set("ach").contains(id);
    }

    public int achievementCount() {
        return set("ach").size();
    }

    public void unlockAchievement(String id) {
        Set<String> s = set("ach");
        s.add(id);
        sp.edit().putStringSet("ach", s).apply();
    }

    // ---- daily login bonus ----
    /** What the player gets for opening the app today. */
    public static final class Bonus {
        public final int coins;
        public final int milestone;
        public final int streak;

        Bonus(int coins, int milestone, int streak) {
            this.coins = coins;
            this.milestone = milestone;
            this.streak = streak;
        }
    }

    /** Returns today's bonus once per day, or null if it was already given. */
    public Bonus claimLoginBonus() {
        String today = today();
        String last = sp.getString("loginLast", "");
        if (today.equals(last)) return null;

        Calendar y = Calendar.getInstance();
        y.add(Calendar.DAY_OF_YEAR, -1);
        int streak = dateKey(y.getTimeInMillis()).equals(last) ? sp.getInt("loginStreak", 0) + 1 : 1;

        int base = 10;
        int milestone = streak % 30 == 0 ? 500 : streak % 7 == 0 ? 150 : streak == 3 ? 50 : 0;
        sp.edit().putString("loginLast", today).putInt("loginStreak", streak).apply();
        addCoins(base + milestone);
        return new Bonus(base, milestone, streak);
    }

    // ---- spin the wheel ----
    public boolean freeSpinReady() {
        return !today().equals(sp.getString("spinFree", ""));
    }

    public void useFreeSpin() {
        sp.edit().putString("spinFree", today()).apply();
    }

    /** Extra spins per day that need a rewarded ad. */
    public int adSpinsLeft() {
        int used = today().equals(sp.getString("spinAdDate", "")) ? sp.getInt("spinAdCount", 0) : 0;
        return Math.max(0, 3 - used);
    }

    public void useAdSpin() {
        int used = today().equals(sp.getString("spinAdDate", "")) ? sp.getInt("spinAdCount", 0) : 0;
        sp.edit().putString("spinAdDate", today()).putInt("spinAdCount", used + 1).apply();
    }

    // ---- rating prompt: asked at 5, 25 and 75 solved films, never again after "Rate now" ----
    public boolean shouldAskRating() {
        if (sp.getBoolean("rateDone", false)) return false;
        int solved = solvedTotal();
        int lastMark = sp.getInt("rateMark", 0);
        for (int mark : new int[]{5, 25, 75}) {
            if (mark > lastMark && solved >= mark) {
                sp.edit().putInt("rateMark", mark).apply();
                return true;
            }
        }
        return false;
    }

    public void setRateDone() {
        sp.edit().putBoolean("rateDone", true).apply();
    }
}
