package com.filmipaheli.app;

import android.app.Application;

public class FilmiApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Prefs.get(this).applyTheme();
        AdManager.get(this).init();
    }
}
