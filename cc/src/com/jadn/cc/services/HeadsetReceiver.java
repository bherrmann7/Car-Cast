package com.jadn.cc.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.util.Log;
import android.view.KeyEvent;

public class HeadsetReceiver extends BroadcastReceiver {

	public static final String HEADSET_PRESENT = "headsetPresent";
	private final ContentService contentService;

	public HeadsetReceiver(ContentService contentService) {
		this.contentService = contentService;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
			KeyEvent ke = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
			if ((ke.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || ke.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK)) {
				if (ke.getAction() == KeyEvent.ACTION_UP) {
					contentService.pauseOrPlay();
				}
				abortBroadcast();
				return;
			}
			if (ke.getKeyCode() == KeyEvent.KEYCODE_MEDIA_NEXT) {
				if (ke.getAction() == KeyEvent.ACTION_UP) {
					contentService.next();
				}
				abortBroadcast();
				return;
			}
			if (ke.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PREVIOUS) {
				if (ke.getAction() == KeyEvent.ACTION_UP) {
					contentService.previous();
				}
				abortBroadcast();
				return;
			}
			if (ke.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK) {
				if (ke.getAction() == KeyEvent.ACTION_UP) {
					contentService.pauseOrPlay();
				}
				abortBroadcast();
				return;
			}
			Log.i("CarCast", "Got ACTION_MEDIA_BUTTON, but didnt use it..." + ke);
			return;
		}

		final int state = intent.getIntExtra("state", 0);
		Log.i("CarCast", "HeadsetReceiver got state of " + state);

		boolean headsetPresent = state != 0;
		contentService.headsetStatusChanged(headsetPresent);
	}

}
