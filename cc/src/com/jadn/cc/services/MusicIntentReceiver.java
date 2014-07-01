
package com.jadn.cc.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

// Handles,
//   <action android:name="android.media.AUDIO_BECOMING_NOISY" />
//   <action android:name="android.intent.action.MEDIA_BUTTON" />
//
public class MusicIntentReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Otherwise, it seems to replay the last Intent after connecting via Bluetooth,
        if (isInitialStickyBroadcast())
            return;
        if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN)
                return;
        }
        intent.setClass(context, ContentService.class);
        context.startService(intent);
    }
}
