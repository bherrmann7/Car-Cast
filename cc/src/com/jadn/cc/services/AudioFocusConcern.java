package com.jadn.cc.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Build;
import android.util.Log;

@SuppressLint("NewApi")
public class AudioFocusConcern {
	
	OnAudioFocusChangeListener afChangeListener;

	public void playing(Context context) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO /* Has AudioFocus */) {
			return;
		} 
		final AudioManager am = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		
		afChangeListener = new OnAudioFocusChangeListener() {
		    public void onAudioFocusChange(int focusChange) {
		    	Log.i("carcastfocus","focusChange "+focusChange);
		        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT){
		            // Pause playback
		        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
		            // Resume playback 
		        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
		            //am.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
		        	//unregisterMyself();
		            //am.abandonAudioFocus(afChangeListener);
		            // Stop playback
		        }
		    }
		};

		// Request audio focus for playback
		int result = am.requestAudioFocus(afChangeListener,
		                                 // Use the music stream.
		                                 AudioManager.STREAM_MUSIC,
		                                 // Request permanent focus.
		                                 AudioManager.AUDIOFOCUS_GAIN);
		   
		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
		    //am.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
		    // Start playback.
		} else {
			Log.e("CarCast", "Yikes audio focus denied.");
		}
		
		
	}

	protected void unregisterMyself() {
		// TODO Auto-generated method stub
		
	}

	public void stoppedPlaying() {
		// TODO Auto-generated method stub
		
	}

}
