package com.jadn.cc.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class HeadsetReceiver extends BroadcastReceiver {

	public static final String HEADSET_PRESENT = "headsetPresent";
	private final ContentService contentService;

	public HeadsetReceiver(ContentService contentService) {
		this.contentService = contentService;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		final int state = intent.getIntExtra("state", 0);
		Log.i("CarCast", "HeadsetReceiver got state of "+state);

		boolean headsetPresent = state != 0;
		contentService.headsetStatusChanged(headsetPresent);
	}
}
