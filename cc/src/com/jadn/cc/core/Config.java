package com.jadn.cc.core;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Config {
    Context context;

    public Config(Context context){
        this.context = context;
    }

	public int getMax(){
		SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(app_preferences.getString("listmax", "2"));
	}

    public File getCarCastRoot(){
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String file = app_preferences.getString("CarCastRoot", new File(android.os.Environment.getExternalStorageDirectory(),"carcast").toString());
        return new File(file);
    }

    public File getPodcastsRoot(){
        return new File(getCarCastRoot(), "podcasts");
    }

    public File getPodcastRootPath(String path){
        return new File(getPodcastsRoot(), path);
    }

    public File getCarCastPath(String path){
        return new File(getCarCastRoot(), path);
    }
}
