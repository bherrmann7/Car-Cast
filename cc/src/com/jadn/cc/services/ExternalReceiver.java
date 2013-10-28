package com.jadn.cc.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ExternalReceiver extends BroadcastReceiver {

	public static final String PAUSE     = "com.jadn.cc.services.external.PAUSE";
	public static final String PLAY      = "com.jadn.cc.services.external.PLAY";
	public static final String PAUSEPLAY = "com.jadn.cc.services.external.PAUSEPLAY";

	public ExternalReceiver() {
                Log.i("CarCast", "ExternalReceiver()");
	}

	@Override
	public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                Log.i("CarCast", "ExternalReceiver.onReceive: " + action);

                Intent i = new Intent(context, ContentService.class);
                i.putExtra("external", action);
                context.startService(i);
                abortBroadcast();
                return;
	}
}

