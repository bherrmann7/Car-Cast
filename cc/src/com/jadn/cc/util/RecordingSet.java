package com.jadn.cc.util;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import com.jadn.cc.R;
import com.jadn.cc.core.Config;
import com.jadn.cc.ui.AudioRecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RecordingSet {

    static File recordDir;
    static MediaRecorder recorder;
    static File recordFile;  // a file that in progress
    private File file;
    private Context context;

    public RecordingSet(Context context) {
        this.context = context;

        Config config = new Config(context);
        recordDir = config.getCarCastPath("recordings");
        recordDir.mkdirs();
    }

    public void cancel() {
        recorder.stop();
        recorder = null;
        recordFile.delete();
    }

    public void deleteAll() {
        for (File file : recordDir.listFiles()) {
            if (file.getName().endsWith(".3gp"))
                file.delete();
        }
    }

    public List<Recording> getRecordings() {
        List<Recording> list = new ArrayList<Recording>();
        // If you have no flash card this might happen
        if (recordDir.listFiles() == null) {
            return list;
        }
        for (File file : recordDir.listFiles()) {
            if (file.getName().endsWith(".3gp"))
                list.add(new Recording(file));
        }
        return list;
    }

    public void record() {
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

    public void save() {
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

            updateNotification();
        } catch (Exception e) {
            Log.e("carcast", "Recording.save", e);
        }

    }

    public void clearNotifications(){
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(24);
    }

    public void updateNotification() {
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

        int r = getRecordings().size();
        if (r == 0) {
            mNotificationManager.cancel(24);
        } else {

            Notification notification = new Notification(R.drawable.idea, "Audio Recordings ", System.currentTimeMillis());

            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, AudioRecorder.class), 0);

            notification.setLatestEventInfo(context, "Audio Recordings", "You have " + r + " recording"
                    + (r == 1 ? "." : "s."), contentIntent);
            //notification.flags = Notification.FLAG_AUTO_CANCEL;

            mNotificationManager.notify(24, notification);
        }

    }

    public void delete(Recording recording) {
        recording.delete();

        if (getRecordings().size() == 0) {
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

            mNotificationManager.cancel(24);
        }
    }


}
