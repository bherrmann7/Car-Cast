package com.jadn.cc.services;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;

public class CCEventService extends Service {

	StringBuilder payload = new StringBuilder();

	private final IEventService.Stub binder = new IEventService.Stub() {
		@Override
		public void post(String message) throws RemoteException {
			esay(message);
		}
	};

	private SharedPreferences app_preferences;

	Timer timer;

	boolean wifiConnected;
	long waitUntil;

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
//
//		boolean connected = intent.getBooleanExtra("WifiState", false);
//		if (connected) {
//			if (timer != null) {
//				wifiConnected = true;
//				esay("Wifi up");
//				// bah, This causes application failure. I dont know why. Is
//				// onStart called by different thread?
//				// timer.cancel();
//				// timer = new Timer();
//				// timer.schedule(task, 40000, 15 * 60 * 1000 /* 15*60*1000 */);
//			}
//		}

	}

	private void esay(String message) {
		synchronized (payload) {
			if (payload.length() != 0)
				payload.append('|');
			payload.append(message);
		}
		;
	}

	private TimerTask task = new TimerTask() {
		public void run() {

			if (wifiConnected) {
				// ping now.
				wifiConnected = false;
				waitUntil = System.currentTimeMillis()+2000;
			} else {
				// time for ping?
				if (System.currentTimeMillis() < waitUntil) {
					return;
				}
			}
			// Compute next ping time.
			waitUntil = System.currentTimeMillis() + 1 * 60 * 1000;

			// do ping
			String uuid = app_preferences.getString("uuid", null);
			if (uuid == null) {
				uuid = UUID.randomUUID().toString();
				SharedPreferences.Editor editor = app_preferences.edit();
				editor.putString("uuid", uuid);
				editor.commit();
			}
			synchronized (payload) {
				try {
					URL url = new URL("http://jadn.com/carcast/hi/ping?uuid="
							+ uuid + "&payload="
							+ URLEncoder.encode(payload.toString()));
					HttpURLConnection con = (HttpURLConnection) url
							.openConnection();
					con.connect();
					if (con.getResponseCode() == 200) {
						payload.setLength(0);
						// read version, con.getInputStream().read
						// compare with TitleActivity.getVersion
						// if newer version and havent already nagged,
						// send notification about new version.
					}
				} catch (Throwable t) {
					// bummer.
				}

			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		setForeground(true);
//		timer = new Timer();
//		timer.schedule(task, 500, 10 * 1000);
	}

}
