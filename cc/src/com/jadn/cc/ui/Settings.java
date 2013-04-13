package com.jadn.cc.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import com.aocate.media.MediaPlayer;
import com.jadn.cc.R;
import com.jadn.cc.core.CarCastApplication;

public class Settings extends PreferenceActivity {
		
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String version="";
		try {
			String ourPackage = CarCast.class.getPackage().getName();
			int lastDot = ourPackage.lastIndexOf('.');
			ourPackage = ourPackage.substring(0, lastDot);
			PackageInfo pInfo = getPackageManager().getPackageInfo(ourPackage, PackageManager.GET_META_DATA);
			version = pInfo.versionName;
		} catch (NameNotFoundException e) {
			Log.e("Settings", "looking up own version", e);
		}
		addPreferencesFromResource(R.xml.settings);

		setTitle(CarCastApplication.getAppTitle()+": "+CarCastApplication.getVersion()+" / "+version);

        Preference myPref = findPreference("variableSpeedEnabled");
        myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                boolean variableSpeed = app_preferences.getBoolean(preference.getKey(), false);
                if (variableSpeed &&
                        !MediaPlayer.isPrestoLibraryInstalled(getApplicationContext()))
                {
                   tellUserAboutStableSpeed();
                }
                return true;
            }
        });
    }


    @Override
	protected void onStop() {
		super.onStop();
		SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
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

		//Prepare to cycle the alarm host service
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("com.jadn.cc.services.AlarmHostService");

		//We always want to stop
		try {
			stopService(serviceIntent);
		} catch (Throwable e)
		{
			Log.w("Settings", "stopping AlarmHostService", e);
		}

		//We might want to start
		if(app_preferences.getBoolean("autoDownload", false)) {
			try {
				startService(serviceIntent);
			} catch (Throwable e)
			{
				Log.e("Settings", "starting AlarmHostService", e);
			}
		}

		 Intent i = getApplicationContext().getPackageManager()
		 .getLaunchIntentForPackage(getApplicationContext().getPackageName() );

		 i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK );
		 startActivity(i);
	}

    private void tellUserAboutStableSpeed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder
                .setMessage(
                        "In order to use variable speed playback you need a 3rd party library. " +
                                "Choose between Stable Speed or Presto, both available in the Google Play Store")
                .setPositiveButton("Stable Speed", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        launchMarketplace("com.stuntbyte.stablespeed");
                    }
                })
                .setNegativeButton("Presto", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        launchMarketplace("com.aocate.presto");
                    }
                })

                .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();
    }

    private void launchMarketplace(String appName) {
        try {
            final Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appName));
            startActivity(i);
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id="+appName)));
        }
    }


}
