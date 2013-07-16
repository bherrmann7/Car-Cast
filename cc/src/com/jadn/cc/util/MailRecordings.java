package com.jadn.cc.util;

import java.util.List;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.jadn.cc.services.ContentService;

public class MailRecordings {

	private static  boolean empty(String x){
		if (x == null)
			return false;
		return x.trim().length() == 0;
	}

	public static boolean isAudioSendingConfigured(ContentService contentService){
		SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(contentService);
		String email = app_preferences.getString("audioEmail", null);
		String username = app_preferences.getString("smtp_username", null);
		String password = app_preferences.getString("smtp_password", null);
		String host = app_preferences.getString("smtp_host", null);
		if (empty(email)|| empty(username) || empty(password) || empty(host)) {
			return false;
		}
		return true;
	}

	public static void doIt(ContentService contentService) throws Exception {
		if(!isAudioSendingConfigured(contentService))
			return;
		SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(contentService);
		String email = app_preferences.getString("audioEmail", null);
		String username = app_preferences.getString("smtp_username", null);
		String password = app_preferences.getString("smtp_password", null);
		String host = app_preferences.getString("smtp_host", null);
		boolean isSecure = app_preferences.getBoolean("smtp_secure", true);
        RecordingSet recordingSet = new RecordingSet(contentService);
		List<Recording> recordings = recordingSet.getRecordings();
		for (Recording recording : recordings) {
			Mailer mailer = new Mailer(email, username, password, host, isSecure);
			mailer.addAttachment(recording.getFile().toString());
			mailer.setBody("Your recording is attached.\n\n  Length "+recording.getDurationString()+"\n  Time     "+recording.getTimeString());
			mailer.setSubject("Recording "+recording.getDurationString()+" "+recording.getTimeString());
			if(mailer.send()){
				recording.delete();
			}
		}

		if (recordingSet.getRecordings().size() == 0){
			NotificationManager mNotificationManager = (NotificationManager) contentService.getSystemService(Activity.NOTIFICATION_SERVICE);
			mNotificationManager.cancel(24);
		}

	}

}
