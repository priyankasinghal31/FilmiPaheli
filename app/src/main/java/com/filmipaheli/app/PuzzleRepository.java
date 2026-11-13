package com.filmipaheli.app;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/** Reads puzzles.json once and answers questions about it. */
public final class PuzzleRepository {

    private static PuzzleRepository instance;
    private final List<Puzzle> all = new ArrayList<>();

    public static synchronized PuzzleRepository get(Context ctx) {
        if (instance == null) instance = new PuzzleRepository(ctx.getApplicationContext());
        return instance;
    }

    private PuzzleRepository(Context ctx) {
        // Built-in films first, then films downloaded later. Downloaded ones can add or replace by id.
        java.util.LinkedHashMap<String, Puzzle> byId = new java.util.LinkedHashMap<>();
        try (InputStream in = ctx.getAssets().open("puzzles.json")) {
            read(new String(readAll(in), "UTF-8"), byId);
        } catch (Exception e) {
            throw new IllegalStateException("Could not read puzzles.json", e);
        }
        java.io.File remote = RemoteContent.file(ctx);
        if (remote.exists()) {
            try (InputStream in = new java.io.FileInputStream(remote)) {
                read(new String(readAll(in), "UTF-8"), byId);
            } catch (Exception ignored) {
                // A bad download is ignored. The built-in films still work.
            }
        }
        all.addAll(byId.values());
    }

    private static byte[] readAll(InputStream in) throws java.io.IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        return out.toByteArray();
    }

    private static void read(String json, java.util.Map<String, Puzzle> into) throws org.json.JSONException {
        JSONArray arr = new JSONArray(json);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            Puzzle p = new Puzzle(
                    o.getString("id"), o.getString("pack"), o.getString("answer"),
                    o.getString("emoji"), o.getString("clue"), o.optString("clueHi", null),
                    o.getString("category"), o.getString("year"));
            into.put(p.id, p);
        }
    }

    public List<Puzzle> all() {
        return all;
    }

    public List<Puzzle> inPack(String packId) {
        List<Puzzle> list = new ArrayList<>();
        for (Puzzle p : all) if (p.pack.equals(packId)) list.add(p);
        return list;
    }

    public Puzzle byId(String id) {
        for (Puzzle p : all) if (p.id.equals(id)) return p;
        return all.get(0);
    }

    /** Same film for everyone on a given calendar day. */
    public Puzzle daily() {
        Calendar c = Calendar.getInstance();
        int seed = c.get(Calendar.YEAR) * 366 + c.get(Calendar.DAY_OF_YEAR);
        return all.get(seed % all.size());
    }

    /** First puzzle in the pack that is not solved yet, or the first one if all are solved. */
    public Puzzle firstToPlay(String packId, Prefs prefs) {
        List<Puzzle> list = inPack(packId);
        for (Puzzle p : list) if (!prefs.isSolved(p.id)) return p;
        return list.get(0);
    }

    /** The puzzle to show after 'current': next unsolved one, else the next one in order. */
    public Puzzle next(String packId, String currentId, Prefs prefs) {
        List<Puzzle> list = inPack(packId);
        for (Puzzle p : list) if (!p.id.equals(currentId) && !prefs.isSolved(p.id)) return p;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id.equals(currentId)) return list.get((i + 1) % list.size());
        }
        return list.get(0);
    }
}
