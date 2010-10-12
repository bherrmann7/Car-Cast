package com.jadn.cc.ui;

import java.util.Date;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import com.jadn.cc.services.AlarmService;
import com.jadn.cc.R;

public class Settings extends PreferenceActivity {
	
    private PendingIntent alarm_sender;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);
		
        alarm_sender = PendingIntent.getService(Settings.this, 0, new Intent(Settings.this, AlarmService.class), 0);

	}

	@Override
	protected void onStop(){
		super.onStop();
		SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		//Keep email address secret
		String accounts = app_preferences.getString("accounts", "");
		if (app_preferences.getBoolean("emailSecret", false)) {
			if (!accounts.startsWith("anon:")) {
				SharedPreferences.Editor editor = app_preferences.edit();
				editor.putString("accounts", "anon:" + System.currentTimeMillis());
				editor.commit();
			}
		} else {
			if (accounts.startsWith("anon:")) {
				SharedPreferences.Editor editor = app_preferences.edit();
				// We use null to mean ask google.
				editor.putString("accounts", null);
				editor.commit();				
			}
		}
		
		//Auto download
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		if(app_preferences.getBoolean("autoDownload", true)) {
			
			//Set the alarm time
			int alarmHour = Integer.parseInt(app_preferences.getString("listDownloadHour", "1"));
			Date currentDate = new Date();
			long alarmTime = new Date(
					currentDate.getYear(),
					currentDate.getMonth(),
					currentDate.getDay(),
					alarmHour, 0).getTime();
			
			//If we've already passed the initial alarm time, move the first time to tomorrow
			if (alarmTime < SystemClock.elapsedRealtime())
				alarmTime = alarmTime + AlarmManager.INTERVAL_DAY;
			
			//TODO:temp -- 15 secs from now
			alarmTime = new Date(
					currentDate.getYear(),
					currentDate.getMonth(),
					currentDate.getDay(),
					currentDate.getHours(), currentDate.getMinutes()).getTime() + 15000;
			
            am.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, AlarmManager.INTERVAL_DAY, alarm_sender);

		} else {
			// Cancel the alarm
            am.cancel(alarm_sender);
		}

	}
}
