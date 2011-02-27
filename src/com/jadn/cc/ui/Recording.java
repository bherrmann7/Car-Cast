package com.jadn.cc.ui;

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
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

import com.jadn.cc.R;
import com.jadn.cc.core.Config;

public class Recording {

	static File recordDir = new File(Config.CarCastRoot, "recordings");
	static MediaRecorder recorder;

	static File recordFile;

	static SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d h:mm a");

	static {
		recordDir.mkdirs();
	}

	public static void cancel() {
		recorder.stop();
		recorder = null;
		recordFile.delete();
	}

	public static void deleteAll() {
		for (File file : recordDir.listFiles()) {
			if (file.getName().endsWith(".3gp"))
				file.delete();
		}
	}

	public static List<Recording> getRecordings() {
		List<Recording> list = new ArrayList<Recording>();
		// If you have no flash card this might happen
		if(recordDir.listFiles()==null){
			return list;
		}
		for (File file : recordDir.listFiles()) {
			if (file.getName().endsWith(".3gp"))
				list.add(new Recording(file));
		}
		return list;
	}

	public static void record() {
		recordFile = new File(recordDir, System.currentTimeMillis() + ".tmp");
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setOutputFile(recordFile.toString());
		try {
			recorder.prepare();
		} catch (Exception e) {
		}
		recorder.start();
	}

	public static void save(Activity activity) {
		recorder.stop();
		recorder = null;
		try {
			MediaPlayer mediaPlayer = new MediaPlayer();
			mediaPlayer.setDataSource(recordFile.toString());
			mediaPlayer.prepare();
			int duration = mediaPlayer.getDuration();
			if (duration > 0)
				duration = duration / 1000;
			mediaPlayer.release();

			File newFile = new File(recordDir, recordFile.getName().replaceAll("\\.tmp$", "-" + duration + ".3gp"));
			recordFile.renameTo(newFile);

			updateNotification(activity);
		} catch (Exception e) {
			Log.e("carcast", "Recording.save", e);
		}

	}

	File file;

	public static void updateNotification(Activity activity) {
		NotificationManager mNotificationManager = (NotificationManager) activity.getSystemService(Activity.NOTIFICATION_SERVICE);

		mNotificationManager.cancel(24);

		int r = getRecordings().size();
		if (r != 0) {

			Notification notification = new Notification(R.drawable.idea, "Audio Recordings ", System.currentTimeMillis());

			PendingIntent contentIntent = PendingIntent.getActivity(activity, 0, new Intent(activity, AudioRecorder.class), 0);

			notification.setLatestEventInfo(activity.getBaseContext(), "Audio Recordings", "You have " + r + " recording"
					+ (r == 1 ? "." : "s."), contentIntent);
			//notification.flags = Notification.FLAG_AUTO_CANCEL;

			mNotificationManager.notify(24, notification);
		}

	}

	public Recording(File file) {
		this.file = file;
	}

	public void delete(Activity activity) {
		file.delete();

		if (getRecordings().size() == 0) {
			NotificationManager mNotificationManager = (NotificationManager) activity.getSystemService(Activity.NOTIFICATION_SERVICE);

			mNotificationManager.cancel(24);
		}
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

}
