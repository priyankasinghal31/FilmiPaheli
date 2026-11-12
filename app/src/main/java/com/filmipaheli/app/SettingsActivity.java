package com.filmipaheli.app;

import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
            // Saves the choice and restarts open screens in the new language.
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag));
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
}
