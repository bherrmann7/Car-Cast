package com.jadn.cc.ui;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.MediaPlayer.OnCompletionListener;
import android.util.Log;

import com.jadn.cc.core.Config;

public class Recording {

	static SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d h:mm a");
	File file;

	public Recording(File file) {
		this.file = file;
	}

	public String getTimeString() {
		long millis = Long.parseLong(file.getName().substring(0,
				file.getName().indexOf('-')));
		return sdf.format(new Date(millis));
	}

	public String getDurationString() {
		int millis = Integer.parseInt(file.getName().substring(
				file.getName().indexOf('-') + 1, file.getName().indexOf('.')));
		int min = millis / 60;
		int sec = millis - (60 * min);
		if (sec <= 9) {
			return min + ":0" + sec;
		}
		return min + ":" + sec;
	}

	public static List<Recording> getRecordings() {
		List<Recording> list = new ArrayList<Recording>();
		for (File file : recordDir.listFiles()) {
			if (file.getName().endsWith(".3gp"))
				list.add(new Recording(file));
		}
		return list;
	}

	public static void deleteAll() {
		for (File file : recordDir.listFiles()) {
			if(file.getName().endsWith(".3gp"))
				file.delete();
		}
	}

	static File recordDir = new File(Config.CarCastRoot, "recordings");
	static {
		recordDir.mkdirs();
	}
	
	static File recordFile;
	static MediaRecorder recorder;

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

	public static void cancel() {
		recorder.stop();
		recorder = null;
		recordFile.delete();
	}

	public static void save() {
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

			File newFile = new File(recordDir, recordFile.getName().replaceAll(
					"\\.tmp$", "-" + duration + ".3gp"));
			recordFile.renameTo(newFile);
		} catch (Exception e) {
			Log.e("carcast", "Recording.save", e);
		}
	}

	public void play() {
		try {
			final MediaPlayer mediaPlayer = new MediaPlayer();
			mediaPlayer.setOnCompletionListener(new OnCompletionListener(){
				@Override
				public void onCompletion(MediaPlayer mp) {
					mediaPlayer.release();
				}});
			mediaPlayer.setDataSource(file.toString());
			mediaPlayer.prepare();
			mediaPlayer.start();
		} catch (Exception e) {
			Log.e("carcast", "Recording.play", e);
		}

	}

	public void delete() {
		file.delete();
	}

}
