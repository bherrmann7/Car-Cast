package com.jadn.cc.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.jadn.cc.R;

public class Settings extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);
	}

	@Override
	protected void onStop(){
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

	}
}
