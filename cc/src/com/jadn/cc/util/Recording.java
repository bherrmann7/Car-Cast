package com.jadn.cc.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.util.Log;

import com.jadn.cc.R;
import com.jadn.cc.core.Config;
import com.jadn.cc.ui.AudioRecorder;

public class Recording {
	private File file;
    static SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d h:mm a");

    public Recording(File file) {
		this.file = file;
	}


	public String getDurationString() {
		int millis = Integer.parseInt(file.getName().substring(file.getName().indexOf('-') + 1, file.getName().indexOf('.')));
		int min = millis / 60;
		int sec = millis - (60 * min);
		if (sec <= 9) {
			return min + ":0" + sec;
		}
		return min + ":" + sec;
	}

	public String getTimeString() {
		long millis = Long.parseLong(file.getName().substring(0, file.getName().indexOf('-')));
		return sdf.format(new Date(millis));
	}

	public void play() {
		try {
			final MediaPlayer mediaPlayer = new MediaPlayer();
			mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					mediaPlayer.release();
				}
			});
			mediaPlayer.setDataSource(file.toString());
			mediaPlayer.prepare();
			mediaPlayer.start();
		} catch (Exception e) {
			Log.e("carcast", "Recording.play", e);
		}

	}

	public File getFile() {
		return file;
	}

    protected void delete(){
        file.delete();
    }

}
