package com.filmipaheli.app;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Downloads new films so you can add content without an app update.
 * The list is read from BuildConfig.CONTENT_URL (a JSON file in the same format as puzzles.json).
 * New films appear the next time the app starts. A failed or bad download is ignored.
 */
public final class RemoteContent {

    private static final long MIN_GAP_MS = 6L * 60 * 60 * 1000; // at most one download every 6 hours
    private static final int MAX_BYTES = 5 * 1024 * 1024;
    private static final Pattern ANSWER = Pattern.compile("[A-Z]+( [A-Z]+)*");

    private RemoteContent() {
    }

    static File file(Context ctx) {
        return new File(ctx.getFilesDir(), "puzzles_remote.json");
    }

    public static void refreshInBackground(Context ctx) {
        final Context app = ctx.getApplicationContext();
        Prefs prefs = Prefs.get(app);
        long now = System.currentTimeMillis();
        if (now - prefs.lastContentCheck() < MIN_GAP_MS) return;
        prefs.setLastContentCheck(now);

        new Thread(() -> {
            try {
                String json = download(BuildConfig.CONTENT_URL);
                if (json != null && valid(json)) {
                    File tmp = new File(app.getFilesDir(), "puzzles_remote.tmp");
                    try (FileOutputStream out = new FileOutputStream(tmp)) {
                        out.write(json.getBytes("UTF-8"));
                    }
                    if (!tmp.renameTo(file(app))) tmp.delete();
                }
            } catch (Exception ignored) {
                // Offline or bad link. Try again later.
            }
        }, "content-refresh").start();
    }

    private static String download(String address) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(address).openConnection();
        c.setConnectTimeout(10000);
        c.setReadTimeout(15000);
        if (c.getResponseCode() != 200) return null;
        try (InputStream in = c.getInputStream()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
                if (out.size() > MAX_BYTES) return null;
            }
            return out.toString("UTF-8");
        } finally {
            c.disconnect();
        }
    }

    /** Only accepts a list that is shaped like puzzles.json. */
    private static boolean valid(String json) {
        try {
            JSONArray arr = new JSONArray(json);
            if (arr.length() == 0) return false;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                for (String key : new String[]{"id", "pack", "answer", "emoji", "clue", "category", "year"}) {
                    if (o.optString(key, "").isEmpty()) return false;
                }
                if (!ANSWER.matcher(o.getString("answer")).matches()) return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
