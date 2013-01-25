package com.jadn.cc.util;

import java.util.List;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.jadn.cc.services.ContentService;

public class MailRecordings {

	public static void doIt(ContentService contentService) throws Exception {
		SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(contentService);		
		String username = app_preferences.getString("smtp_username", null);
		String password = app_preferences.getString("smtp_password", null);
		String host = app_preferences.getString("smtp_host", null);
		boolean isSecure = app_preferences.getBoolean("smtp_secure", true);
		if (username == null || username.trim().length() == 0 || password == null && password.trim().length() == 0) {
			return;
		}
		List<Recording> recordings = Recording.getRecordings();
		for (Recording recording : recordings) {
			Mailer mailer = new Mailer(username, password, host, isSecure);
			mailer.addAttachment(recording.getFile().toString());
			mailer.setBody("Your recording is attached.\n\n  Length "+recording.getDurationString()+"\n  Time     "+recording.getTimeString());
			mailer.setSubject("Recording "+recording.getDurationString()+" "+recording.getTimeString());
			if(mailer.send()){
				recording.getFile().delete();
			}			
		}
		
		if (Recording.getRecordings().size() == 0){
			NotificationManager mNotificationManager = (NotificationManager) contentService.getSystemService(Activity.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(24);
		}

	}

}
