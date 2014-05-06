package com.jadn.cc.core;

import com.jadn.cc.services.ContentService;
import com.jadn.cc.util.Recording;
import com.jadn.cc.util.RecordingSet;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

public class WifiConnectedReceiver extends BroadcastReceiver {

	public static void registerForWifiBroadcasts(Context context) {
		context.registerReceiver(new WifiConnectedReceiver(), new IntentFilter(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION));
	}

	@Override
	public void onReceive(Context context, Intent intent) {

		final String action = intent.getAction();
		if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
			if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
				// if sending recordings on wifi connect
				Log.i("carcast", "connected");

				SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
				String username = app_preferences.getString("smtp_username", null);
				String password = app_preferences.getString("smtp_password", null);

				if (username == null || username.trim().length() == 0 || password == null && password.trim().length() == 0) {
					return;
				}
				if (new RecordingSet(context).getRecordings().size() == 0)
					return;
				//
				// Start emailing recorded audio
				ServiceConnection conn = new ServiceConnection() {

					@Override
					public void onServiceConnected(ComponentName name, IBinder service) {
						ContentService contentService = ((ContentService.LocalBinder) service).getService();
						contentService.publishRecordings(null);
					}

					@Override
					public void onServiceDisconnected(ComponentName name) {
					}
				};

				Intent csIntent = new Intent(context, ContentService.class);
				context.bindService(csIntent, conn, Context.BIND_AUTO_CREATE);
			}

		} else {
			// wifi connection was lost
		}
	}

}
