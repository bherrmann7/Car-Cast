package com.jadn.cc.services;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

public class ServiceReceiver extends BroadcastReceiver {
	
	IEventService service;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			context.startService(new Intent(context, CCEventService.class));
		}
		final String action = intent.getAction();
		if (action
				.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
			Intent ccevent = new Intent(context, CCEventService.class);
			ccevent.putExtra("WifiState", intent.getBooleanExtra
					(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false));
			context.startService(ccevent);
		}

	}

}
