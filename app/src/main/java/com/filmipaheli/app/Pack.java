package com.filmipaheli.app;

import java.util.Arrays;
import java.util.List;

/** A themed group of puzzles shown as a card on the home screen. */
public class Pack {
    public final String id;
    public final String name;
    public final String glyph;
    public final int background;
    /** Coins needed to unlock. 0 means the pack is open. Locked packs have no puzzles yet. */
    public final int cost;

    public Pack(String id, String name, String glyph, int background, int cost) {
        this.id = id;
        this.name = name;
        this.glyph = glyph;
        this.background = background;
        this.cost = cost;
    }

    public boolean isLocked() {
        return cost > 0;
    }

    public static final List<Pack> ALL = Arrays.asList(
            new Pack("90s", "90s Hits", "९०", R.drawable.bg_pack_1, 0),
            new Pack("srk", "Shah Rukh Khan", "SRK", R.drawable.bg_pack_2, 0),
            new Pack("emoji", "Emoji Round", "🎬", R.drawable.bg_pack_3, 0),
            new Pack("classics", "Black & White Classics", "क्लासिक", R.drawable.bg_pack_4, 0),
            new Pack("dialogues", "Dialogues", "", R.drawable.bg_pack_lock, 300),
            new Pack("directors", "Directors", "", R.drawable.bg_pack_lock, 300)
    );

    public static Pack byId(String id) {
        for (Pack p : ALL) if (p.id.equals(id)) return p;
        return ALL.get(0);
    }
}
