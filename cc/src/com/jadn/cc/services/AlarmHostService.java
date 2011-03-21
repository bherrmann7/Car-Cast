package com.jadn.cc.services;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

/*
 *  Based on http://www.androidcompetencycenter.com/2009/06/start-service-at-boot/
 */
public class AlarmHostService extends Service {

	private PendingIntent alarm_sender;

	@Override
	public void onCreate() {
		super.onCreate();
		alarm_sender = PendingIntent.getService(AlarmHostService.this, 0, new Intent(AlarmHostService.this, AlarmService.class), 0);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);

		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);

		//Set the alarm time
 		String time = app_preferences.getString("timeAutoDownload", "2:0");
 		GregorianCalendar currentCalendar = new GregorianCalendar();
 		long alarmTime = new GregorianCalendar(
 				currentCalendar.get(Calendar.YEAR),
 				currentCalendar.get(Calendar.MONTH),
 				currentCalendar.get(Calendar.DAY_OF_MONTH),
 				Integer.valueOf(time.split(":")[0]), 
 				Integer.valueOf(time.split(":")[1])).getTime().getTime();
 		
 		//Add a day if the hour has passed
 		if (alarmTime < currentCalendar.getTime().getTime())
 			alarmTime = alarmTime + AlarmManager.INTERVAL_DAY;
 		
		am.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, AlarmManager.INTERVAL_DAY, alarm_sender);
		Log.i("AlarmHostService", "set " + (new Date(alarmTime)));
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
