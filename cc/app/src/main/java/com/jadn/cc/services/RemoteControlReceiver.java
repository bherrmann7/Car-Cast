package com.jadn.cc.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class RemoteControlReceiver extends BroadcastReceiver {
	ContentService contentService;
	
	public RemoteControlReceiver() {
		//this.contentService = contentService;
	}
	
    @Override
    public void onReceive(Context context, Intent intent) {    
    	Log.i("carcast", "dead jim");
        if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
            /* handle media button intent here by reading contents */
            /* of EXTRA_KEY_EVENT to know which key was pressed    */
        }
    }
}