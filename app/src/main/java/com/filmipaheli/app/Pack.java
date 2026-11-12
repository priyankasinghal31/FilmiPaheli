package com.filmipaheli.app;

import java.util.Arrays;
import java.util.List;

/** A themed group of puzzles shown as a card on the home screen. */
public class Pack {
    public final String id;
    public final int nameRes;
    public final String glyph;
    public final int background;
    /** Coins needed to unlock. 0 means the pack is open from the start. */
    public final int cost;

    public Pack(String id, int nameRes, String glyph, int background, int cost) {
        this.id = id;
        this.nameRes = nameRes;
        this.glyph = glyph;
        this.background = background;
        this.cost = cost;
    }

    private static final int A = R.drawable.bg_pack_1;
    private static final int B = R.drawable.bg_pack_2;
    private static final int C = R.drawable.bg_pack_3;
    private static final int D = R.drawable.bg_pack_4;

    /** Pack ids must match the 'pack' field in assets/puzzles.json. */
    public static final List<Pack> ALL = Arrays.asList(
            new Pack("classics", R.string.pack_classics, "क्लासिक", D, 0),
            new Pack("retro", R.string.pack_retro, "७०", A, 0),
            new Pack("90s", R.string.pack_90s, "९०", B, 0),
            new Pack("2000s", R.string.pack_2000s, "२०००", C, 0),
            new Pack("2010s", R.string.pack_2010s, "२०१०", D, 0),
            new Pack("new", R.string.pack_new_hits, "नई", A, 0),
            new Pack("srk", R.string.pack_srk, "SRK", B, 0),
            new Pack("aamir", R.string.pack_aamir, "आमिर", C, 0),
            new Pack("salman", R.string.pack_salman, "भाई", D, 0),
            new Pack("bigb", R.string.pack_bigb, "बिग B", A, 0),
            new Pack("akshay", R.string.pack_akshay, "खिलाड़ी", B, 0),
            new Pack("heroines", R.string.pack_heroines, "नायिका", C, 0),
            new Pack("emoji", R.string.pack_emoji, "🎬", D, 0),
            new Pack("dialogues", R.string.pack_dialogues, "संवाद", R.drawable.bg_pack_lock, 300),
            new Pack("directors", R.string.pack_directors, "निर्देशक", R.drawable.bg_pack_lock, 300)
    );

    public static Pack byId(String id) {
        for (Pack p : ALL) if (p.id.equals(id)) return p;
        return ALL.get(0);
    }
}
