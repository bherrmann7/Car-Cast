package com.jadn.cc.core;

import android.app.Activity;
import android.widget.Toast;

public class Util {

	public static void say(Activity activity, String string) {
		Toast.makeText(activity.getApplicationContext(), string, Toast.LENGTH_LONG).show();		
	}

}
