package com.jadn.cc.core;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.widget.Toast;

public class Util {

    public static void say(Activity activity, String string) {
        Toast.makeText(activity.getApplicationContext(), string, Toast.LENGTH_LONG).show();
    }

    public static boolean isValidURL(String url) {
        try {
            new URL(url);
            return true;

        } catch (MalformedURLException ex) {
            return false;
        } // endtry
    }
}