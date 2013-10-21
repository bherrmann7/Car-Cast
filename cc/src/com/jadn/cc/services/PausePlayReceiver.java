package com.jadn.cc.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.util.Log;
import android.view.KeyEvent;

public class PausePlayReceiver extends BroadcastReceiver {

	public static final String INTENT = "com.jadn.cc.PAUSEPLAY";
	private final ContentService contentService;

	public PausePlayReceiver(ContentService contentService) {
		// Log.d("CarCast", "PausePlayReceiver created!");
		this.contentService = contentService;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// Log.i("CarCast", "PausePlayReceiver received!");
                contentService.pauseOrPlay();
	}

}
