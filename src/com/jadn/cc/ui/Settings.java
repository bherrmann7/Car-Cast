package com.jadn.cc.ui; import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.jadn.cc.R;

public class Settings extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);
	}
}
