package com.jadn.cc.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.util.Log;
import android.view.KeyEvent;

public class ExternalReceiver extends BroadcastReceiver {

	public static final String PAUSE     = "com.jadn.cc.services.external.PAUSE";
	public static final String PLAY      = "com.jadn.cc.services.external.PLAY";
	public static final String PAUSEPLAY = "com.jadn.cc.services.external.PAUSEPLAY";

	private final ContentService contentService;

	public ExternalReceiver(ContentService contentService) {
		this.contentService = contentService;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if ( action.equals(PAUSE) )
                {
                         contentService.pauseNow();
			 abortBroadcast();
			 return;
                }

                if ( action.equals(PLAY) )
                {
                         contentService.play();
			 abortBroadcast();
			 return;
                }

                if ( action.equals(PAUSEPLAY) )
                {
                         contentService.pauseOrPlay();
			 abortBroadcast();
			 return;
                }

		Log.i("CarCast", "Got external intent, but didnt use it...");
                return;
	}

}
