package com.filmipaheli.app;

import java.util.Arrays;
import java.util.List;

/** A themed group of puzzles shown as a card on the home screen. */
public class Pack {
    public final String id;
    public final String name;
    public final String glyph;
    public final int background;
    /** Coins needed to unlock. 0 means the pack is open from the start. */
    public final int cost;

    public Pack(String id, String name, String glyph, int background, int cost) {
        this.id = id;
        this.name = name;
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
            new Pack("classics", "Black & White Classics", "क्लासिक", D, 0),
            new Pack("retro", "70s & 80s", "७०", A, 0),
            new Pack("90s", "90s Hits", "९०", B, 0),
            new Pack("2000s", "2000s", "२०००", C, 0),
            new Pack("2010s", "2010s", "२०१०", D, 0),
            new Pack("new", "Latest Hits", "नई", A, 0),
            new Pack("srk", "Shah Rukh Khan", "SRK", B, 0),
            new Pack("aamir", "Aamir Khan", "आमिर", C, 0),
            new Pack("salman", "Salman Khan", "भाई", D, 0),
            new Pack("bigb", "Amitabh Bachchan", "बिग B", A, 0),
            new Pack("akshay", "Akshay Kumar", "खिलाड़ी", B, 0),
            new Pack("heroines", "Leading Ladies", "नायिका", C, 0),
            new Pack("emoji", "Emoji Round", "🎬", D, 0),
            new Pack("dialogues", "Dialogues", "संवाद", R.drawable.bg_pack_lock, 300),
            new Pack("directors", "Directors", "निर्देशक", R.drawable.bg_pack_lock, 300)
    );

    public static Pack byId(String id) {
        for (Pack p : ALL) if (p.id.equals(id)) return p;
        return ALL.get(0);
    }
}
