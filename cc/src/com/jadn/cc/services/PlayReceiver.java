package com.jadn.cc.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.util.Log;
import android.view.KeyEvent;

public class PlayReceiver extends BroadcastReceiver {

	public static final String INTENT = "com.jadn.cc.PLAY";
	private final ContentService contentService;

	public PlayReceiver(ContentService contentService) {
		// Log.d("CarCast", "PlayReceiver created!");
		this.contentService = contentService;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		// Log.i("CarCast", "PlayReceiver received!");
                contentService.play();
	}

}
