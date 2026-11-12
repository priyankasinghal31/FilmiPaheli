package com.filmipaheli.app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.os.LocaleListCompat;

/** Language (English or Hindi) and theme (system, light or dark). */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Prefs prefs = Prefs.get(this);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // ---- language ----
        RadioGroup lang = findViewById(R.id.langGroup);
        boolean hindi = "hi".equals(getResources().getConfiguration().getLocales().get(0).getLanguage());
        lang.check(hindi ? R.id.langHi : R.id.langEn);
        lang.setOnCheckedChangeListener((group, id) -> {
            String tag = id == R.id.langHi ? "hi" : "en";
            prefs.setLanguage(tag); // also used for background notification text
            // Saves the choice and restarts open screens in the new language.
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag));
        });

        // ---- sound, vibration, reminder ----
        SwitchCompat sound = findViewById(R.id.switchSound);
        sound.setChecked(prefs.soundOn());
        sound.setOnCheckedChangeListener((b, on) -> {
            prefs.setSoundOn(on);
            if (on) Feedback.get(this).tap();
        });

        SwitchCompat vibration = findViewById(R.id.switchVibration);
        vibration.setChecked(prefs.vibrateOn());
        vibration.setOnCheckedChangeListener((b, on) -> prefs.setVibrateOn(on));

        SwitchCompat reminder = findViewById(R.id.switchReminder);
        reminder.setChecked(prefs.reminderOn());
        reminder.setOnCheckedChangeListener((b, on) -> {
            prefs.setReminderOn(on);
            if (on) {
                Reminder.schedule(this);
                askNotificationPermission();
            } else {
                Reminder.cancel(this);
            }
        });

        // ---- theme ----
        RadioGroup theme = findViewById(R.id.themeGroup);
        switch (prefs.theme()) {
            case "light":
                theme.check(R.id.themeLight);
                break;
            case "dark":
                theme.check(R.id.themeDark);
                break;
            default:
                theme.check(R.id.themeSystem);
        }
        theme.setOnCheckedChangeListener((group, id) -> {
            prefs.setTheme(id == R.id.themeLight ? "light" : id == R.id.themeDark ? "dark" : "system");
            prefs.applyTheme(); // restarts open screens in the new theme
        });
    }

    /** Android 13 and newer ask the player before showing notifications. */
    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }
    }
}
