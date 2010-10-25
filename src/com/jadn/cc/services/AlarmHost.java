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
public class AlarmHost extends Service {

	private PendingIntent alarm_sender;

	@Override
	public void onCreate() {
		super.onCreate();
		alarm_sender = PendingIntent.getService(AlarmHost.this, 0, new Intent(AlarmHost.this, AlarmService.class), 0);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		
		SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);

		AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
		if(app_preferences.getBoolean("autoDownload", true)) {
			//Set the alarm time
	 		int alarmHour = Integer.parseInt(app_preferences.getString("listDownloadHour", "1"));
	 		GregorianCalendar currentCalendar = new GregorianCalendar();
	 		long alarmTime = new GregorianCalendar(
	 			currentCalendar.get(Calendar.YEAR),
	 			currentCalendar.get(Calendar.MONTH),
	 			currentCalendar.get(Calendar.DAY_OF_MONTH),
				alarmHour, 
				0).getTime().getTime();
	 		
	 		//Add a day if the hour has passed
	 		if (alarmTime < currentCalendar.getTime().getTime())
	 			alarmTime = alarmTime + AlarmManager.INTERVAL_DAY;
	 		
			am.setRepeating(AlarmManager.RTC_WAKEUP, alarmTime, AlarmManager.INTERVAL_DAY, alarm_sender);
			Log.i("AlarmHost", "set " + (new Date(alarmTime)));
			
		} else {

			//The alarm host service starts on boot
			//We don't want to stick around if auto downloads are disabled
			
			//Cancel the alarm (probably redundant)
			am.cancel(alarm_sender);
			
			//Stop the host service
			AlarmHost.this.stopSelf();
 		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
