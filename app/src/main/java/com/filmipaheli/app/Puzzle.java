package com.filmipaheli.app;

/** One film to guess. Loaded from assets/puzzles.json. */
public class Puzzle {
    public final String id;
    public final String pack;
    public final String answer;   // upper case letters and single spaces only
    public final String emoji;
    public final String clue;
    /** Hindi version of the clue, or null if there is none. */
    public final String clueHi;
    public final String category;
    public final String year;

    public Puzzle(String id, String pack, String answer, String emoji,
                  String clue, String clueHi, String category, String year) {
        this.id = id;
        this.pack = pack;
        this.answer = answer.toUpperCase();
        this.emoji = emoji;
        this.clue = clue;
        this.clueHi = clueHi;
        this.category = category;
        this.year = year;
    }

    /** The clue in the player's language. Falls back to English. */
    public String clueFor(boolean hindi) {
        return hindi && clueHi != null && !clueHi.isEmpty() ? clueHi : clue;
    }

    /** Answer without spaces, for counting letters. */
    public String letters() {
        return answer.replace(" ", "");
    }
}
