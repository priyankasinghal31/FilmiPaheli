package com.filmipaheli.app;

import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Badges the player earns, each with a small coin reward. */
public final class Achievements {

    interface Rule {
        boolean met(Prefs prefs, PuzzleRepository repo);
    }

    public static final class Item {
        public final String id;
        public final int titleRes;
        public final int descRes;
        public final int reward;
        final Rule rule;

        Item(String id, int titleRes, int descRes, int reward, Rule rule) {
            this.id = id;
            this.titleRes = titleRes;
            this.descRes = descRes;
            this.reward = reward;
            this.rule = rule;
        }
    }

    public static final List<Item> ALL = Arrays.asList(
            new Item("first_film", R.string.ach_first_film_t, R.string.ach_first_film_d, 20,
                    (p, r) -> p.solvedTotal() >= 1),
            new Item("films_10", R.string.ach_films_10_t, R.string.ach_films_10_d, 50,
                    (p, r) -> p.solvedTotal() >= 10),
            new Item("films_50", R.string.ach_films_50_t, R.string.ach_films_50_d, 150,
                    (p, r) -> p.solvedTotal() >= 50),
            new Item("films_100", R.string.ach_films_100_t, R.string.ach_films_100_d, 300,
                    (p, r) -> p.solvedTotal() >= 100),
            new Item("films_250", R.string.ach_films_250_t, R.string.ach_films_250_d, 600,
                    (p, r) -> p.solvedTotal() >= 250),
            new Item("films_500", R.string.ach_films_500_t, R.string.ach_films_500_d, 1000,
                    (p, r) -> p.solvedTotal() >= 500),
            new Item("pack_done", R.string.ach_pack_done_t, R.string.ach_pack_done_d, 100,
                    (p, r) -> {
                        for (Pack pack : Pack.ALL) {
                            if (!p.isUnlocked(pack)) continue;
                            List<Puzzle> list = r.inPack(pack.id);
                            if (!list.isEmpty() && p.solvedCount(list) == list.size()) return true;
                        }
                        return false;
                    }),
            new Item("three_star_10", R.string.ach_three_star_10_t, R.string.ach_three_star_10_d, 100,
                    (p, r) -> p.stat("three") >= 10),
            new Item("streak_7", R.string.ach_streak_7_t, R.string.ach_streak_7_d, 100,
                    (p, r) -> p.streak() >= 7),
            new Item("streak_30", R.string.ach_streak_30_t, R.string.ach_streak_30_d, 500,
                    (p, r) -> p.streak() >= 30),
            new Item("daily_7", R.string.ach_daily_7_t, R.string.ach_daily_7_d, 100,
                    (p, r) -> p.stat("daily") >= 7),
            new Item("rich", R.string.ach_rich_t, R.string.ach_rich_d, 50,
                    (p, r) -> p.coins() >= 1000),
            new Item("spinner", R.string.ach_spinner_t, R.string.ach_spinner_d, 50,
                    (p, r) -> p.stat("spin") >= 10),
            new Item("sharer", R.string.ach_sharer_t, R.string.ach_sharer_d, 30,
                    (p, r) -> p.stat("share") >= 1 || p.stat("ask") >= 1)
    );

    private Achievements() {
    }

    /** Unlocks any newly earned badges, pays the coins and tells the player with a toast. */
    public static List<Item> checkAndAnnounce(Context ctx) {
        Prefs prefs = Prefs.get(ctx);
        PuzzleRepository repo = PuzzleRepository.get(ctx);
        List<Item> fresh = new ArrayList<>();
        for (Item item : ALL) {
            if (prefs.hasAchievement(item.id) || !item.rule.met(prefs, repo)) continue;
            prefs.unlockAchievement(item.id);
            prefs.addCoins(item.reward);
            fresh.add(item);
            Toast.makeText(ctx, ctx.getString(R.string.ach_unlocked_toast,
                    ctx.getString(item.titleRes), item.reward), Toast.LENGTH_LONG).show();
        }
        return fresh;
    }
}
