package com.jadn.cc.ui;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;

import com.jadn.cc.services.HeadsetReceiver;

public abstract class MediaControlActivity extends BaseActivity {

	private AudioManager mAudioManager;
	private ComponentName mRemoteControlResponder;
	private static Method mRegisterMediaButtonEventReceiver;
	private static Method mUnregisterMediaButtonEventReceiver;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		mRemoteControlResponder = new ComponentName(getPackageName(), HeadsetReceiver.class.getName());		
	}

	private static void initializeRemoteControlRegistrationMethods() {
		try {
			if (mRegisterMediaButtonEventReceiver == null) {
				mRegisterMediaButtonEventReceiver = AudioManager.class.getMethod("registerMediaButtonEventReceiver",
						new Class[] { ComponentName.class });
			}
			if (mUnregisterMediaButtonEventReceiver == null) {
				mUnregisterMediaButtonEventReceiver = AudioManager.class.getMethod("unregisterMediaButtonEventReceiver",
						new Class[] { ComponentName.class });
			}
			/* success, this device will take advantage of better remote */
			/* control event handling */
		} catch (NoSuchMethodException nsme) {
			/* failure, still using the legacy behavior, but this app */
			/* is future-proof! */
			nsme.printStackTrace();
			Log.i("CarCast", "Boink...");
		}
	}

	private void registerRemoteControl() {
		try {
			if (mRegisterMediaButtonEventReceiver == null) {
				return;
			}
			mRegisterMediaButtonEventReceiver.invoke(mAudioManager, mRemoteControlResponder);
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (IllegalAccessException ie) {
			Log.e("CarCast", "unexpected " + ie);
		}
	}

	private void unregisterRemoteControl() {
		try {
			if (mUnregisterMediaButtonEventReceiver == null) {
				return;
			}
			mUnregisterMediaButtonEventReceiver.invoke(mAudioManager, mRemoteControlResponder);
		} catch (InvocationTargetException ite) {
			/* unpack original exception when possible */
			Throwable cause = ite.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			} else {
				/* unexpected checked exception; wrap and re-throw */
				throw new RuntimeException(ite);
			}
		} catch (IllegalAccessException ie) {
			System.err.println("unexpected " + ie);
		}
	}

	public MediaControlActivity() {
		super();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerRemoteControl();		
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		unregisterRemoteControl();
	}

	static {
		initializeRemoteControlRegistrationMethods();
	}

}