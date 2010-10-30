package com.jadn.cc.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/*
 * Based on http://www.androidcompetencycenter.com/2009/06/start-service-at-boot/
 */
public class AlarmReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {

		SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(context);

		if(app_preferences.getBoolean("autoDownload", false)) {
			Intent serviceIntent = new Intent();
			serviceIntent.setAction("com.jadn.cc.services.AlarmHostService");
			context.startService(serviceIntent);
		}
	}
}
