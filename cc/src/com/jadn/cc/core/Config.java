package com.jadn.cc.core;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Config {
    Context context;

    public Config(Context context) {
        this.context = context;
        getPodcastsRoot().mkdirs();
    }

    public int getMax() {
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(app_preferences.getString("listmax", "2"));
    }

    public File getCarCastRoot() {
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String file = app_preferences.getString("CarCastRoot", null);
        if (file == null) {
            SharedPreferences.Editor editor = app_preferences.edit();
            editor.putString("CarCastRoot", new File(android.os.Environment.getExternalStorageDirectory(), "carcast").toString());
            editor.commit();
            file = app_preferences.getString("CarCastRoot", null);
        }
        return new File(file);
    }

    public File getPodcastsRoot() {
        return new File(getCarCastRoot(), "podcasts");
    }

    public File getPodcastRootPath(String path) {
        return new File(getPodcastsRoot(), path);
    }

    public File getCarCastPath(String path) {
        return new File(getCarCastRoot(), path);
    }
}
