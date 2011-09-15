package com.jadn.cc.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
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
			Log.e("CarCast", "Bone.." + ke.getKeyCode());
			if ((ke.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE || ke.getKeyCode() == KeyEvent.KEYCODE_HEADSETHOOK)
					&& ke.getAction() == KeyEvent.ACTION_UP) {
				contentService.pauseOrPlay();
				abortBroadcast();
			}
			return;
		}

		final int state = intent.getIntExtra("state", 0);
		Log.i("CarCast", "HeadsetReceiver got state of " + state);

		boolean headsetPresent = state != 0;
		contentService.headsetStatusChanged(headsetPresent);
	}
}
