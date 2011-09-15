package com.jadn.cc.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class RemoteControlReceiver extends BroadcastReceiver {
	ContentService contentService;
	
	public RemoteControlReceiver(ContentService contentService) {
		this.contentService = contentService;
	}
	
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            /* handle media button intent here by reading contents */
            /* of EXTRA_KEY_EVENT to know which key was pressed    */
        }
    }
}