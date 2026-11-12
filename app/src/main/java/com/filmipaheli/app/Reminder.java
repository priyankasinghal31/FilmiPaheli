package com.filmipaheli.app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/** A daily 7 pm nudge to play, only if the player has not played today. */
public final class Reminder {

    private static final String WORK_NAME = "daily_reminder";
    private static final String CHANNEL_ID = "daily";

    private Reminder() {
    }

    public static void schedule(Context ctx) {
        Calendar now = Calendar.getInstance();
        Calendar at = (Calendar) now.clone();
        at.set(Calendar.HOUR_OF_DAY, 19);
        at.set(Calendar.MINUTE, 0);
        at.set(Calendar.SECOND, 0);
        if (!at.after(now)) at.add(Calendar.DAY_OF_YEAR, 1);
        long delay = at.getTimeInMillis() - now.getTimeInMillis();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(ReminderWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();
        WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request);
    }

    public static void cancel(Context ctx) {
        WorkManager.getInstance(ctx).cancelUniqueWork(WORK_NAME);
    }

    /** Makes a context that uses the language chosen in Settings, for text in background work. */
    static Context localized(Context ctx) {
        Configuration config = new Configuration(ctx.getResources().getConfiguration());
        config.setLocale(new Locale(Prefs.get(ctx).language()));
        return ctx.createConfigurationContext(config);
    }

    public static class ReminderWorker extends Worker {

        public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
            super(context, params);
        }

        @NonNull
        @Override
        public Result doWork() {
            Context ctx = getApplicationContext();
            Prefs prefs = Prefs.get(ctx);
            if (!prefs.reminderOn() || prefs.dailyPlayed()) return Result.success();

            Context text = localized(ctx);
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= 26) {
                nm.createNotificationChannel(new NotificationChannel(
                        CHANNEL_ID, text.getString(R.string.reminder_channel), NotificationManager.IMPORTANCE_DEFAULT));
            }

            int streak = prefs.streak();
            String body = streak > 0
                    ? text.getResources().getQuantityString(R.plurals.reminder_streak, streak, streak)
                    : text.getString(R.string.reminder_body);

            PendingIntent open = PendingIntent.getActivity(ctx, 0,
                    new Intent(ctx, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_star)
                    .setContentTitle(text.getString(R.string.reminder_title))
                    .setContentText(body)
                    .setContentIntent(open)
                    .setAutoCancel(true);
            try {
                if (NotificationManagerCompat.from(ctx).areNotificationsEnabled()) {
                    NotificationManagerCompat.from(ctx).notify(1, b.build());
                }
            } catch (SecurityException ignored) {
                // Notification permission was not granted on Android 13 or newer.
            }
            return Result.success();
        }
    }
}
