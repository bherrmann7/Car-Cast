package com.jadn.cc.core;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.Activity;
import android.widget.Toast;

public class Util {
	
	public static String getShortURL(String url) {
		String shortName = url.substring(url.lastIndexOf('/') + 1);
		if (shortName.indexOf('?') != -1)
			return shortName.substring(0, shortName.indexOf('?'));
		return shortName;
	}

	public static boolean isValidURL(String url) {
		try {
			new URL(url);
			return true;
			
		} catch (MalformedURLException ex) {
			return false;
		}
	}

	public static void toast(Activity activity, String string) {
		Toast.makeText(activity.getApplicationContext(), string, Toast.LENGTH_LONG).show();
	}
}
