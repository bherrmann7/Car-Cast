package com.jadn.cc.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/*
 * Based on http://www.androidcompetencycenter.com/2009/06/start-service-at-boot/
 */
public class AlarmReceiver extends BroadcastReceiver{
	@Override
	public void onReceive(Context context, Intent intent) {
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("com.jadn.cc.services.AlarmHost");
		context.startService(serviceIntent);
	}
}
