package com.filmipaheli.app;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

/** Plays the game sounds and vibrations. Both can be switched off in Settings. */
public final class Feedback {

    private static Feedback instance;

    private final Context app;
    private final SoundPool pool;
    private final Vibrator vibrator;
    private final int tap, correct, wrong, win, lose;

    public static synchronized Feedback get(Context ctx) {
        if (instance == null) instance = new Feedback(ctx.getApplicationContext());
        return instance;
    }

    @SuppressWarnings("deprecation")
    private Feedback(Context app) {
        this.app = app;
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        pool = new SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attrs).build();
        tap = pool.load(app, R.raw.sfx_tap, 1);
        correct = pool.load(app, R.raw.sfx_correct, 1);
        wrong = pool.load(app, R.raw.sfx_wrong, 1);
        win = pool.load(app, R.raw.sfx_win, 1);
        lose = pool.load(app, R.raw.sfx_lose, 1);
        vibrator = (Vibrator) app.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public void tap() {
        play(tap);
    }

    public void correct() {
        play(correct);
    }

    public void wrong() {
        play(wrong);
        buzz(160);
    }

    public void win() {
        play(win);
        buzz(60);
    }

    public void lose() {
        play(lose);
        buzz(300);
    }

    private void play(int id) {
        if (Prefs.get(app).soundOn()) pool.play(id, 1f, 1f, 1, 0, 1f);
    }

    @SuppressWarnings("deprecation")
    private void buzz(long ms) {
        if (!Prefs.get(app).vibrateOn() || vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= 26) {
            vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(ms);
        }
    }
}
