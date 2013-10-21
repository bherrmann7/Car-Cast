package com.jadn.cc.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.util.Log;
import android.view.KeyEvent;

public class PauseReceiver extends BroadcastReceiver {

	public static final String INTENT = "com.jadn.cc.PAUSE";
	private final ContentService contentService;

	public PauseReceiver(ContentService contentService) {
		// Log.d("CarCast", "PauseReceiver created!");
		this.contentService = contentService;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// Log.i("CarCast", "PauseReceiver received!");
                contentService.pauseNow();
	}

}
