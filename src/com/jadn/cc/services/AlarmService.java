package com.jadn.cc.services;

import com.jadn.cc.core.ExternalMediaStatus;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.util.Log;

/*
 * Based on
 * 	http://developer.android.com/resources/samples/ApiDemos/src/com/example/android/apis/app/AlarmService_Service.html
 * 	and
 * 	http://www.androidcompetencycenter.com/2009/01/basics-of-android-part-iii-android-services/
 * 
 */
public class AlarmService extends Service {

	SharedPreferences app_preferences;
	
    @Override
    public void onCreate() {
    	super.onCreate();

    	app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
    	
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        Thread thr = new Thread(null, task, "AlarmService");
        thr.start();
    }

    /**
     * The function that runs in our worker thread
     */
    Runnable task = new Runnable() {
        public void run() {
        	        	
        	try {
        		//Deal with WIFI option
        		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        		if(app_preferences.getBoolean("wifiDownload", true) && !wifi.isWifiEnabled()) 
        		{
        			Log.w("AlarmService", "Elected not to download podcasts: WIFI not enabled");
        			return;
        		}
        		
        		//Check SD card - reject if not writable
        		if (ExternalMediaStatus.getExternalMediaStatus() != ExternalMediaStatus.writeable)
        		{
        			Log.w("AlarmService", "Elected not to download podcasts: SD card not writable");
        			return;
        		}

        		//Check SD card space - reject if less than 20 MB available
        		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath()); 
        		long bytesAvailable = (long)stat.getBlockSize() * (long)stat.getBlockCount(); 
        		if (bytesAvailable < 20971520)
        		{
        			Log.w("AlarmService", "Elected not to download podcasts: insufficient space available");
        			return;
        		}

	        	// Start downloading podcasts
        		ServiceConnection conn = new ServiceConnection() {

					@Override
					public void onServiceConnected(ComponentName name, IBinder service) {

		        		int maxDownloads = Integer.parseInt(app_preferences.getString("listmax", "2"));

        				IContentService contentService = (IContentService) service;
        				try {
							contentService.startDownloadingNewPodCasts(maxDownloads);
						} catch (RemoteException e) {
							Log.e("AlarmService", "downloading new podcasts", e);
						}						
					}

					@Override
					public void onServiceDisconnected(ComponentName name) {
					}
        		};
        		
        		Intent csIntent = new Intent(getApplicationContext(), ContentService.class);
				getApplicationContext().bindService(csIntent, conn, BIND_AUTO_CREATE);

        	} catch(Throwable e) {
				Log.e("AlarmService", "unknown failure", e);
        	}

            // Done with our work...  stop the service!
            AlarmService.this.stopSelf();
        }
    };

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
