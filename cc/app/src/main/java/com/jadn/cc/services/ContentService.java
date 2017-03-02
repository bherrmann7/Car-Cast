package com.jadn.cc.services;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.aocate.media.MediaPlayer;
import com.jadn.cc.R;
import com.jadn.cc.core.AudioFocusHelper;
import com.jadn.cc.core.CarCastApplication;
import com.jadn.cc.core.Config;
import com.jadn.cc.core.Location;
import com.jadn.cc.core.MediaMode;
import com.jadn.cc.core.MusicFocusable;
import com.jadn.cc.core.Subscription;
import com.jadn.cc.core.WifiConnectedReceiver;
import com.jadn.cc.trace.TraceUtil;
import com.jadn.cc.ui.CarCast;
import com.jadn.cc.util.ExportOpml;
import com.jadn.cc.util.MailRecordings;

public class ContentService extends Service implements MediaPlayer.OnCompletionListener, MusicFocusable {
    private final IBinder binder = new LocalBinder();
    int currentPodcastInPlayer = -1;
    DownloadHelper downloadHelper;
    Location location;
    MediaMode mediaMode = MediaMode.UnInitialized;
    MediaPlayer mediaPlayer = null;
    MetaHolder metaHolder;
    SearchHelper searchHelper;
    // Dummy album art we will pass to the remote control (if the APIs are available).
    Bitmap mDummyAlbumArt;


    private PlayStatusListener playStatusListener;
    RemoteControlClientCompat mRemoteControlClientCompat;
    private Context context;
    private Config config;
    FileSubscriptionHelper subHelper;

    enum PauseReason {
        PhoneCall,
        UserRequest,  // paused by user request
        FocusLoss,    // paused because of audio focus loss
    }

    ;

    // why did we pause?
    PauseReason mPauseReason = PauseReason.UserRequest;

    // do we have audio focus?
    enum AudioFocus {
        NoFocusNoDuck,    // we don't have audio focus, and can't duck
        NoFocusCanDuck,   // we don't have focus, but can play at a low volume ("ducking")
        Focused           // we have full audio focus
    }

    AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;

    // Lifted from RandomMusicPlayer google reference application
    // our AudioFocusHelper object, if it's available (it's available on SDK level >= 8)
    // If not available, this will be null. Always check for null before using!
    AudioFocusHelper mAudioFocusHelper = null;

    public void setApplicationContext(Context context) {
        this.context = context;
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer(context, true);
                fullReset();
            }
        } catch (Exception e) {
            Log.d("CarCast", "Error doing reset", e);
        }
    }

    /**
     * Class for clients to access. Because we know this service always runs in the same process as its clients, we
     * don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public ContentService getService() {
            return ContentService.this;
        }
    }

    public static String getTimeString(int time) {
        StringBuilder sb = new StringBuilder();
        int min = time / (1000 * 60);
        if (min < 10)
            sb.append('0');
        sb.append(min);
        sb.append(':');
        int sec = (time - min * 60 * 1000) / 1000;
        if (sec < 10)
            sb.append('0');
        sb.append(sec);
        return sb.toString();
    }

    public boolean addSubscription(Subscription toAdd) {
        return subHelper.addSubscription(toAdd);
    }

    public void bumpForwardSeconds(int bump) {
        if (currentPodcastInPlayer >= metaHolder.getSize())
            return;
        try {
            int npos = mediaPlayer.getCurrentPosition() + bump * 1000;
            if (npos < 0) {
                npos = 0;
            } else if (npos > mediaPlayer.getDuration()) {
                npos = mediaPlayer.getDuration() - 1;
                mediaPlayer.seekTo(npos);
            }
            mediaPlayer.seekTo(npos);
        } catch (Exception e) {
            // do nothing
        }
        if (!mediaPlayer.isPlaying()) {
            saveState();
        }

    }

    public MetaFile currentMeta() {
        if (metaHolder.getSize() == 0) return null;
        if (currentPodcastInPlayer == -1) return null;
        if (metaHolder.getSize() <= currentPodcastInPlayer) return null;
        return metaHolder.get(currentPodcastInPlayer);
    }

    private int currentDuration() {
        if (currentPodcastInPlayer >= metaHolder.getSize()) {
            return 0;
        }
        int dur = currentMeta().getDuration();
        if (dur != -1)
            return dur;
        if (mediaMode == MediaMode.UnInitialized) {
            currentMeta().computeDuration();
            return currentMeta().getDuration();
        }
        return currentMeta().getDuration();
    }

    public File currentFile() {
        MetaFile meta = currentMeta();
        return meta == null ? null : meta.file;
    }

    int currentPostion() {
        if (currentPodcastInPlayer >= metaHolder.getSize()) {
            return 0;
        }
        return metaHolder.get(currentPodcastInPlayer).getCurrentPos();
    }

    public int currentProgress() {
        if (mediaMode == MediaMode.UnInitialized) {
            int duration = currentDuration();
            if (duration == 0)
                return 0;
            return currentPostion() * 100 / duration;
        }
        if (mediaPlayer.getDuration() == 0) {
            return 0;
        }
        return mediaPlayer.getCurrentPosition() * 100 / mediaPlayer.getDuration();
    }

    public CharSequence currentSummary() {
        StringBuilder sb = new StringBuilder();
        if (currentPodcastInPlayer >= metaHolder.getSize()) {
            if (isDownloading())
                return "Downloading podcasts";
            return "No Podcasts have been downloaded.";
        }
        sb.append(currentMeta().getFeedName());
        sb.append('\n');
        sb.append(currentMeta().getTitle());
        return sb.toString();
    }

    boolean isDownloading() {
        if (downloadHelper == null)
            return false;
        return downloadHelper.isRunning();
    }

    public String currentTitle() {
        if (currentPodcastInPlayer >= metaHolder.getSize()) {
            if (isDownloading()) {
                return "Downloading podcasts\n" + downloadHelper.getStatus();
            }
            return "No podcasts loaded.\nUse 'Menu' and 'Download Podcasts'";
        }
        return currentMeta().getTitle();
    }

    public void deleteAllSubscriptions() {
        subHelper.deleteAllSubscriptions();
    }

    // used by status when mediaplayer is not started.

    public void deleteCurrentPodcast() {
        if (mediaMode == MediaMode.Playing) {
            pauseNow();
        }
        metaHolder.get(currentPodcastInPlayer).delete();
        metaHolder = new MetaHolder(getApplicationContext(), currentFile());
        if (currentPodcastInPlayer >= metaHolder.getSize()) {
            currentPodcastInPlayer = 0;
        }
        updateRemoteDisplay();
    }

    public void deletePodcast(int position) {
        if (mediaPlayer.isPlaying() && currentPodcastInPlayer == position) {
            pauseNow();
        }

        metaHolder.delete(position);
        if (currentPodcastInPlayer >= metaHolder.getSize()) {
            if (currentPodcastInPlayer > 0)
                currentPodcastInPlayer--;
        }
        // If we are playing something after what's deleted, adjust the current
        if (currentPodcastInPlayer > position)
            currentPodcastInPlayer--;

        updateRemoteDisplay();

        try {
            fullReset();
        } catch (Throwable e) {
            // bummer.
        }
    }

    public void deleteSubscription(Subscription sub) {
        subHelper.removeSubscription(sub);
    }

    public void toggleSubscription(Subscription sub) {
        subHelper.toggleSubscription(sub);
    }

    void deleteUpTo(int upTo) {
        if (mediaPlayer.isPlaying()) {
            pauseNow();
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
        mediaMode = MediaMode.UnInitialized;
        if (upTo == -1)
            upTo = metaHolder.getSize();
        for (int i = 0; i < upTo; i++) {
            metaHolder.delete(0);
        }
        metaHolder = new MetaHolder(getApplicationContext(), currentFile());
        tryToRestoreLocation();
        if (location == null)
            currentPodcastInPlayer = 0;
    }

    void doDownloadCompletedNotification(int got) {

        // Get the notification manager service.
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);

        mNotificationManager.cancel(23);

        // Allow UI to update download text (only when in debug mode) this seems
        // suboptimal
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (got == 0 && !app_preferences.getBoolean("notifiyOnZeroDownloads", true)) {
            mNotificationManager.cancel(22);
        } else {
            Notification notification = new Notification(R.drawable.icon2, "Download complete", System.currentTimeMillis());

            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, CarCast.class), 0);

            notification.setLatestEventInfo(getBaseContext(), "Downloads Finished", "Downloaded " + got + " podcasts.", contentIntent);
            notification.flags = Notification.FLAG_AUTO_CANCEL;

            mNotificationManager.notify(22, notification);
        }

        metaHolder = new MetaHolder(getApplicationContext(), currentFile());
        if (currentPodcastInPlayer >= metaHolder.getSize()) {
            currentPodcastInPlayer = 0;
        }
        updateRemoteDisplay();
    }

    public boolean editSubscription(Subscription original, Subscription modified) {
        return subHelper.editSubscription(original, modified);
    }

    public String encodedDownloadStatus() {
        if (downloadHelper == null) {
            return "";
        }

        return downloadHelper.getEncodedStatus();
    }

    private boolean fullReset() throws Exception {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
        }

        applyVariableSpeedProperties();

        if (currentPodcastInPlayer >= metaHolder.getSize())
            return false;


        mediaPlayer.setDataSource(currentFile().toString());
        mediaPlayer.prepare();
        mediaPlayer.setOnCompletionListener(this);

        mediaPlayer.seekTo(metaHolder.get(currentPodcastInPlayer).getCurrentPos());
        return true;
    }

    private void applyVariableSpeedProperties() {
        SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean variableSpeed = app_preferences.getBoolean("variableSpeedEnabled", false);

        mediaPlayer.setUseService(variableSpeed);
        mediaPlayer.setEnableSpeedAdjustment(variableSpeed);

        Log.d("CarCast", "SPEED CHOICE " + app_preferences.getString("speedChoice", "undefined"));
        Float speed = null;
        try {
            speed = Float.parseFloat(app_preferences.getString("speedChoice", "1.75"));
        } catch (Exception e) {
            Log.d("CarCast", e.getMessage());
            speed = 1.75f;
        }

        mediaPlayer.setPlaybackSpeed(speed);
    }

    public int getCount() {
        return metaHolder.getSize();
    }

    public String getCurrentSubscriptionName() {
        if (currentPodcastInPlayer >= metaHolder.getSize()) {
            return "";
        }
        return currentMeta().getFeedName();
    }

    public String getDurationString() {
        return getTimeString(currentDuration());
    }

    public Location getLocation() {
        if (mediaMode == MediaMode.UnInitialized) {
            return new Location(currentTitle());
        }
        return new Location(currentTitle());
    }

    public String getLocationString() {
        Location useLocation = getLocation();
        if (isPlaying()) {
            return getTimeString(mediaPlayer.getCurrentPosition());
        }
        if (currentMeta() != null)
            return getTimeString(currentMeta().getCurrentPos());
        return "";
    }

    public MediaMode getMediaMode() {
        return mediaMode;
    }

    public String getPodcastEmailSummary() {
        StringBuilder sb = new StringBuilder();
        if (currentPodcastInPlayer < metaHolder.getSize()) {
            MetaFile mf = currentMeta();
            if (mf != null) {
                sb.append("\nWanted to let you know about this podcast:\n\n");
                sb.append("\nTitle: " + mf.getTitle());
                String searchName = mf.getFeedName();
                sb.append("\nFeed Title: " + searchName);
                List<Subscription> subs = getSubscriptions();
                for (Subscription sub : subs) {
                    if (sub.name.equals(searchName)) {
                        sb.append("\nFeed URL: " + sub.url);
                        break;
                    }
                }
                if (mf.getUrl() != null) {
                    sb.append("\nPodcast URL: " + mf.getUrl());
                }
            }
        }
        sb.append("\n\n\n");
        sb.append("This email sent from " + CarCastApplication.getAppTitle() + ".");
        return sb.toString();
    }

    /**
     * Gets a Map of URLs to Subscription Name
     *
     * @return a map keyed on sub url to value of sub name
     */
    public List<Subscription> getSubscriptions() {
        List<Subscription> subscriptions = subHelper.getSubscriptions();
        return subscriptions;
    }

    public String getWhereString() {
        StringBuilder sb = new StringBuilder();
        if (metaHolder.getSize() == 0)
            sb.append('0');
        else
            sb.append(currentPodcastInPlayer + 1);
        sb.append('/');
        sb.append(metaHolder.getSize());
        return sb.toString();
    }

    public void moveTo(double d) {
        if (mediaMode == MediaMode.UnInitialized) {
            if (currentDuration() == 0)
                return;
            metaHolder.get(currentPodcastInPlayer).setCurrentPos((int) (d * currentDuration()));
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(currentFile().toString());
                mediaPlayer.prepare();
            } catch (Exception e) {
                TraceUtil.report(e);
            }
            mediaMode = MediaMode.Paused;
            return;
        }
        mediaPlayer.seekTo((int) (d * mediaPlayer.getDuration()));
    }

    // Called when user hits next button. Migth be playing or not playing at the
    // time.
    public void next() {
        boolean isPlaying = mediaPlayer.isPlaying();
        if (isPlaying) {
            currentMeta().setCurrentPos(mediaPlayer.getCurrentPosition());
            currentMeta().save();
            mediaPlayer.stop();
        }
        next(isPlaying);
    }

    // called when user hits button (might be playing or not playing) and called
    // when
    // the playback engine his the "onCompletion" event (ie. a podcast has
    // finished, in which case
    // we are actually no longer playing but we were just were a millisecond or
    // so ago.)
    void next(boolean inTheActOfPlaying) {
        mediaMode = MediaMode.UnInitialized;

        // if we are at end.
        if (currentPodcastInPlayer + 1 >= metaHolder.getSize()) {
            saveState();
            // activity.disableJumpButtons();
            mediaPlayer.reset();
            // say(activity, "That's all folks");
            if (inTheActOfPlaying)
                disableNotification();
            return;
        }

        currentPodcastInPlayer++;

        updateRemoteDisplay();

        if (inTheActOfPlaying)
            play();
        else
            disableNotification();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i("CarCast", "ContentService binding " + intent);
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("CarCast", "ContentService unbinding " + intent);
        return super.onUnbind(intent);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (currentMeta() == null)
            return;
        currentMeta().setCurrentPos(0);
        currentMeta().setListenedTo();
        currentMeta().save();
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("autoPlayNext", true)) {
            next(true);
        } else {
            disableNotification();
        }
    }

    private void initDirs() {
        File legacyFile = config.getCarCastPath("podcasts.txt");
        File siteListFile = config.getCarCastPath("podcasts.properties");
        subHelper = new FileSubscriptionHelper(siteListFile, legacyFile);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        WifiConnectedReceiver.registerForWifiBroadcasts(getApplicationContext());

        config = new Config(getApplicationContext());

        initDirs();

        // Google handles surprise exceptions now, so we dont have to.
        //ExceptionHandler.register(this);

        partialWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                CarCastApplication.getAppTitle());
        partialWakeLock.setReferenceCounted(false);

        PhoneStateListener phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                super.onCallStateChanged(state, incomingNumber);

                if (state == TelephonyManager.CALL_STATE_OFFHOOK || state == TelephonyManager.CALL_STATE_RINGING) {
                    // It's possible that this listener is registered before the setApplicationContext
                    // method is called, which establishes the MediaPlayer instance.
                    if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                        mPauseReason = PauseReason.PhoneCall;
                        pauseNow();
                        bumpForwardSeconds(-5);
                    }
                }

                if (state == TelephonyManager.CALL_STATE_IDLE && mPauseReason == PauseReason.PhoneCall) {
                    mPauseReason = PauseReason.UserRequest;
                    pauseOrPlay();
                }
            }
        };

        final TelephonyManager telMgr = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        telMgr.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

        metaHolder = new MetaHolder(getApplicationContext());
//		mediaPlayer.setOnCompletionListener(this);

        // restore state;
        currentPodcastInPlayer = 0;

        restoreState();

//        remoteControlReceiver = new RemoteControlReceiver(this);
//        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
//        priority cribbed from
//        http://www.gauntface.co.uk/blog/2010/04/14/using-android-headset-buttons-earphone-buttons/
//        intentFilter.setPriority(10*IntentFilter.SYSTEM_HIGH_PRIORITY);
//        registerReceiver(remoteControlReceiver, intentFilter);

        AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        ComponentName mMediaButtonReceiverComponent = new ComponentName(this, MusicIntentReceiver.class);
        mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceiverComponent);

        if (mRemoteControlClientCompat == null) {
            Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            intent.setComponent(mMediaButtonReceiverComponent);
            mRemoteControlClientCompat = new RemoteControlClientCompat(
                    PendingIntent.getBroadcast(this /*context*/,
                            0 /*requestCode, ignored*/, intent /*intent*/, 0 /*flags*/)
            );
            RemoteControlHelper.registerRemoteControlClient(mAudioManager,
                    mRemoteControlClientCompat);
        }

        mRemoteControlClientCompat.setPlaybackState(
                RemoteControlClient.PLAYSTATE_PAUSED);

        mRemoteControlClientCompat.setTransportControlFlags(
                RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
                        RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
                        RemoteControlClient.FLAG_KEY_MEDIA_NEXT |
                        RemoteControlClient.FLAG_KEY_MEDIA_STOP
        );

        mDummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.ccp_launcher);

        updateRemoteDisplay();

        // foreground stuff
        try {
            mStartForeground = getClass().getMethod("startForeground", mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground", mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
            try {
                mSetForeground = getClass().getMethod("setForeground", mSetForegroundSignature);
            } catch (NoSuchMethodException e1) {
                throw new IllegalStateException("OS doesn't have Service.startForeground OR Service.setForeground!");
            }
        }

        // create the Audio Focus Helper, if the Audio Focus feature is available (SDK 8 or above)
        if (android.os.Build.VERSION.SDK_INT >= 8)
            mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);
        else
            mAudioFocus = AudioFocus.Focused; // no focus feature, so we always "have" audio focus

    }

    public void updateRemoteDisplay() {
        if (currentMeta() != null) {
            // Update the remote controls
            mRemoteControlClientCompat.editMetadata(true)
                    .putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, currentMeta().getFeedName())
                    .putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, currentMeta().getTitle())
                    .putString(MediaMetadataRetriever.METADATA_KEY_TITLE, currentMeta().getDescription())
                    .putLong(MediaMetadataRetriever.METADATA_KEY_DURATION,
                            currentMeta().getDuration())
                    .putBitmap(
                            RemoteControlClientCompat.MetadataEditorCompat.METADATA_KEY_ARTWORK,
                            mDummyAlbumArt)
                    .apply();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int not_sticky = Service.START_NOT_STICKY;
        Log.i("CarCast", "ContentService.onStartCommand()");

        if (intent.getAction().equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);

            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    pauseOrPlay();
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    if (!isPlaying()) {
                        play();
                    }
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    pauseNow();
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    bumpForwardSeconds(30);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    bumpForwardSeconds(-30);
                    break;
            }
            return not_sticky;
        }

        if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
            // if unplugged
            if (intent.getIntExtra("state", 0) == 0 && isPlaying()) {
                pauseNow();
                bumpForwardSeconds(-2);
                if (playStatusListener != null) {
                    playStatusListener.playStateUpdated(false);
                }

            }
            return not_sticky;
        }

        Bundle extras = intent.getExtras();
        String external = extras.getString("external");

        if (external == null)
            return not_sticky;

        Log.i("CarCast", "ContentService got intent with external extra:" + external);

        if (mediaPlayer == null)
            setApplicationContext(getApplicationContext());

        if (external.equals(ExternalReceiver.PAUSE))
            pauseNow();

        if (external.equals(ExternalReceiver.PLAY) && !isPlaying())
            play();

        if (external.equals(ExternalReceiver.PAUSEPLAY))
            pauseOrPlay();

        return not_sticky;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("CarCast", "ContentService destroyed");
        // Toast.makeText(getApplication(), "Service Destroyed", 1000).show();

        // Make sure our notification is gone.
        disableNotification();

        giveUpAudioFocus();

        AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        ComponentName mMediaButtonReceiverComponent = new ComponentName(this, MusicIntentReceiver.class);
        mAudioManager.unregisterMediaButtonEventReceiver(mMediaButtonReceiverComponent);

    }


    void giveUpAudioFocus() {
        if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null
                && mAudioFocusHelper.abandonFocus())
            mAudioFocus = AudioFocus.NoFocusNoDuck;
    }


    public void pauseNow() {
        if (mediaPlayer.isPlaying()) {

            // Save current position
            currentMeta().setCurrentPos(mediaPlayer.getCurrentPosition());
            currentMeta().save();

            mediaPlayer.pause();
            mediaMode = MediaMode.Paused;
            // say(activity, "paused " + currentTitle());
            saveState();

            mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
        }
        disableNotification();
        // activity.disableJumpButtons();
    }

    /**
     * @returns playing or not
     */
    public boolean pauseOrPlay() {
        try {
            if (mediaPlayer.isPlaying()) {
                pauseNow();
                return false;
            } else {
                if (mediaMode == MediaMode.Paused) {
                    enableNotification();
                    mediaPlayer.start();
                    mediaMode = MediaMode.Playing;
                    // activity.enableJumpButtons();
                    mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
                } else {
                    play();
                }
                return true;
            }
        } catch (Exception e) {
            Log.e("CarCast", "Unexpected exception", e);
            return false;
        }
    }

    private void play() {
        try {
            enableNotification();

            if (!fullReset())
                return;

            tryToGetAudioFocus();

            // say(activity, "started " + currentTitle());
            mediaPlayer.start();
            mediaMode = MediaMode.Playing;
            saveState();

            mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
        } catch (Exception e) {
            TraceUtil.report(e);
        }
    }

    public void play(int position) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        currentPodcastInPlayer = position;
        play();
        updateRemoteDisplay();
        mRemoteControlClientCompat.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
    }

    public void previous() {
        boolean playing = false;
        if (mediaPlayer.isPlaying()) {
            playing = true;
            mediaPlayer.stop();
            // say(activity, "stopped " + currentTitle());
        }
        mediaMode = MediaMode.UnInitialized;
        if (currentPodcastInPlayer > 0) {
            currentPodcastInPlayer--;
            updateRemoteDisplay();
        }
        if (currentPodcastInPlayer >= metaHolder.getSize())
            return;
        if (playing)
            play();

        // final TextView textView = (TextView) activity
        // .findViewById(R.id.summary);
        // textView.setText(currentTitle());
    }

    public void purgeHeard() {
        deleteUpTo(currentPodcastInPlayer);
    }

    public void resetToDemoSubscriptions() {
        subHelper.resetToDemoSubscriptions();
    }

    public void restoreState() {
        final File stateFile = config.getPodcastRootPath("state.dat");
        if (!stateFile.exists()) {
            location = null;
            return;
        }
        try {
            if (location == null) {
                location = Location.load(stateFile);
            }
            tryToRestoreLocation();
        } catch (Throwable e) {
            // bummer.
        }

    }

    public void saveState() {
        try {
            final File stateFile = config.getPodcastRootPath("state.dat");
            location = Location.save(stateFile, currentTitle());
        } catch (Throwable e) {
            // bummer.
        }
    }

    public void setCurrentPaused(int position) {
        boolean wasPlaying = mediaPlayer.isPlaying();
        if (wasPlaying) {
            currentMeta().setCurrentPos(mediaPlayer.getCurrentPosition());
            mediaPlayer.stop();
        }
        mediaMode = MediaMode.Paused;
        currentPodcastInPlayer = position;
    }

    public void setMediaMode(MediaMode mediaMode) {
        this.mediaMode = mediaMode;
    }

    // Synchronized: to ensure that maximum one startDownloadingNewPodCasts()
    // can be active at any time.
    //
    public synchronized void startDownloadingNewPodCasts(final int max) {
        if (downloadHelper != null && downloadHelper.isRunning()) {
            Log.w("carcast", "abort start - carcast already running");
            return;
        } else {
            downloadHelper = new DownloadHelper(max);
        }

        Log.w("carcast", "startDownloadingNewPodCasts");
        boolean autoDelete = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("autoDelete", false);
        if (autoDelete) {
            for (int i = metaHolder.getSize() - 1; i >= 0; i--) {
                MetaFile metaFile = metaHolder.get(i);
                if (currentTitle().equals(metaFile.getTitle())) {
                    continue;
                }
                if (metaFile.getDuration() <= 0) {
                    continue;
                }
                if (metaFile.isListenedTo()) {
                    deletePodcast(i);
                }
            }
        }

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(22);
        updateNotification("Downloading podcasts started");

        new Thread() {
            @Override
            public void run() {
                try {
                    partialWakeLock.acquire();

                    Log.i("CarCast", "starting download thread.");
                    // Lets not the phone go to sleep while doing
                    // downloads....
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ContentService download thread");

                    WifiManager.WifiLock wifiLock = null;

                    try {
                        // The intent here is keep the phone from shutting
                        // down during a download.
                        wl.acquire();

                        // If we have wifi now, lets hold on to it.
                        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                        if (wifi.isWifiEnabled()) {
                            wifiLock = wifi.createWifiLock("CarCast");
                            if (wifiLock != null)
                                wifiLock.acquire();
                            Log.i("CarCast", "Locked Wifi.");
                        }

                        String accounts = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("accounts",
                                "none");

                        downloadHelper.downloadNewPodCasts(ContentService.this);
                    } finally {
                        if (wifiLock != null) {
                            try {
                                wifiLock.release();
                                Log.i("CarCast", "released Wifi.");
                            } catch (Throwable t) {
                                Log.i("CarCast", "Yikes, issue releasing Wifi.");
                            }
                        }

                        wl.release();
                    }
                } catch (Throwable t) {
                    Log.i("CarCast", "Unpleasentness during download: " + t.getMessage());
                } finally {
                    Log.i("CarCast", "finished download thread.");
                    partialWakeLock.release();
                }
            }
        }.start();

    }

    public String startSearch(String search) {
        if (search.equals("-status-")) {
            if (searchHelper.done)
                return "done";
            return "";
        }
        if (search.equals("-results-")) {
            return searchHelper.results;
        }

        searchHelper = new ItunesSearchHelper(search);
        //searchHelper = new SearchHelper(search);
        searchHelper.start();
        return "";
    }

    private void tryToRestoreLocation() {
        try {
            if (location == null) {
                return;
            }
            boolean found = false;
            for (int i = 0; i < metaHolder.getSize(); i++) {
                if (metaHolder.get(i).getTitle().equals(location.title)) {
                    currentPodcastInPlayer = i;
                    updateRemoteDisplay();
                    found = true;
                    break;
                }
            }
            if (!found) {
                location = null;
                return;
            }
            mediaPlayer.reset();
            mediaPlayer.setDataSource(currentFile().toString());
            mediaPlayer.prepare();
            mediaPlayer.seekTo(currentMeta().getCurrentPos());
            mediaMode = MediaMode.Paused;
        } catch (Throwable e) {
            // bummer.
        }

    }

    void updateNotification(String update) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);

        Notification notification = new Notification(R.drawable.iconbusy, "Downloading started", System.currentTimeMillis());

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, CarCast.class), 0);

        notification.setLatestEventInfo(getBaseContext(), "Downloading Started", update, contentIntent);
        notification.flags = Notification.FLAG_ONGOING_EVENT;

        mNotificationManager.notify(23, notification);

    }

    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    public boolean isIdle() {
        return !isPlaying() && (downloadHelper == null || !downloadHelper.isRunning());
    }

    public void purgeAll() {
        deleteUpTo(-1);
    }

    public String getDownloadProgress() {
        if (downloadHelper == null)
            return "";
        return downloadHelper.sb.toString();
    }

    public void purgeToCurrent() {
        deleteUpTo(currentPodcastInPlayer);
    }

    public void setPlayStatusListener(PlayStatusListener playStatusListener) {
        this.playStatusListener = playStatusListener;
    }

    public void newContentAdded() {
        metaHolder = new MetaHolder(getApplicationContext(), currentFile());
    }

    public void directorySettingsChanged() {
        initDirs();
        metaHolder = new MetaHolder(getApplicationContext(), currentFile());
    }

    // This section cribbed from
    // http://developer.android.com/reference/android/app/Service.html#startForeground%28int,%20android.app.Notification%29

    private static final Class<?>[] mSetForegroundSignature = new Class[]{boolean.class};
    private static final Class<?>[] mStartForegroundSignature = new Class[]{int.class, Notification.class};
    private static final Class<?>[] mStopForegroundSignature = new Class[]{boolean.class};

    private Method mSetForeground;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mSetForegroundArgs = new Object[1];
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];

    void invokeMethod(Method method, Object[] args) {
        try {
            mStartForeground.invoke(this, mStartForegroundArgs);
        } catch (InvocationTargetException e) {
            // Should not happen.
            Log.w("CarCast-ContentService", "Unable to invoke method", e);
        } catch (IllegalAccessException e) {
            // Should not happen.
            Log.w("CarCast-ContentService", "Unable to invoke method", e);
        }
    }

    /**
     * This is a wrapper around the new startForeground method, using the older APIs if it is not available.
     */
    void startForegroundCompat(int id, Notification notification) {
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            invokeMethod(mStartForeground, mStartForegroundArgs);
            return;
        }

        // Fall back on the old API.
        mSetForegroundArgs[0] = Boolean.TRUE;
        invokeMethod(mSetForeground, mSetForegroundArgs);
    }

    /**
     * This is a wrapper around the new stopForeground method, using the older APIs if it is not available.
     */
    void stopForegroundCompat() {
        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;
            try {
                mStopForeground.invoke(this, mStopForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w("CarCast-ContentService", "Unable to invoke stopForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w("CarCast-ContentService", "Unable to invoke stopForeground", e);
            }
            return;
        }

        // Fall back on the old API. Note to cancel BEFORE changing the
        // foreground state, since we could be killed at that point.
        mSetForegroundArgs[0] = Boolean.FALSE;
        invokeMethod(mSetForeground, mSetForegroundArgs);
    }

    private WakeLock partialWakeLock;

    void enableNotification() {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, CarCast.class), 0);
        Notification notification = new Notification(R.drawable.ccp_launcher, null, System.currentTimeMillis());
        notification.flags |= Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        notification.setLatestEventInfo(this, getText(R.string.notification_status), getText(R.string.notification_text), contentIntent);

        startForegroundCompat(R.string.notification_status, notification);

        partialWakeLock.acquire();

    }

    void disableNotification() {
        stopForegroundCompat();

        partialWakeLock.release();
    }

    public SortedSet<Integer> moveTop(SortedSet<Integer> checkedItems) {
        return metaHolder.moveTop(checkedItems);
    }

    public SortedSet<Integer> moveUp(SortedSet<Integer> checkedItems) {
        return metaHolder.moveUp(checkedItems);
    }

    public SortedSet<Integer> moveBottom(SortedSet<Integer> checkedItems) {
        return metaHolder.moveBottom(checkedItems);
    }

    public SortedSet<Integer> moveDown(SortedSet<Integer> checkedItems) {
        return metaHolder.moveDown(checkedItems);
    }

    public void exportOPML(FileOutputStream fileOutputStream) {
        ExportOpml.export(getSubscriptions(), fileOutputStream);
    }

    Thread publishRecordingsThread;

    public void publishRecordings(final Activity activity) {

        if (publishRecordingsThread != null) {
            return;
        }

        publishRecordingsThread = new Thread() {
            @Override
            public void run() {
                try {
                    partialWakeLock.acquire();

                    Log.i("CarCast", "starting recording thread.");
                    // Lets not the phone go to sleep while doing
                    // downloads....
                    PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ContentService download thread");

                    WifiManager.WifiLock wifiLock = null;

                    try {
                        // The intent here is keep the phone from shutting
                        // down during a download.
                        wl.acquire();

                        // If we have wifi now, lets hold on to it.
                        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
                        if (wifi.isWifiEnabled()) {
                            wifiLock = wifi.createWifiLock("CarCast");
                            if (wifiLock != null)
                                wifiLock.acquire();
                            Log.i("CarCast", "recording Locked Wifi.");
                        }

                        Thread.sleep(2000); // Give wifi two seconds to stablize

                        MailRecordings.doIt(ContentService.this);

                    } finally {
                        if (wifiLock != null) {
                            try {
                                wifiLock.release();
                                Log.i("CarCast", "recording released Wifi.");
                            } catch (Throwable t) {
                                Log.i("CarCast", "recording Yikes, issue releasing Wifi.");
                            }
                        }

                        wl.release();
                    }
                } catch (javax.mail.AuthenticationFailedException afe) {
                    doWarning(activity, "Problem authenticating.  Check username/password.");
                } catch (final Throwable t) {
                    Log.i("CarCast", "Unpleasentness during email of recording: " + t.getMessage() + " " + t.getClass());
                    String msg = t.getMessage();
                    if (msg == null) {
                        msg = t.toString();
                    }
                    doWarning(activity, msg);
                } finally {
                    Log.i("CarCast", "finished recording post thread.");
                    partialWakeLock.release();
                    publishRecordingsThread = null;
                }
            }

            public void doWarning(Activity activity, final String message) {
                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(getApplication(), "Error sending email: " + message, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        publishRecordingsThread.start();

    }

    /**
     * Signals that audio focus was gained.
     */
    public void onGainedAudioFocus() {
        //Toast.makeText(getApplicationContext(), "CarCast: gained audio focus.", Toast.LENGTH_SHORT).show();
        mAudioFocus = AudioFocus.Focused;

        if (mPauseReason == PauseReason.FocusLoss) {
            mPauseReason = PauseReason.UserRequest;
            play();
        }
    }

    /**
     * Signals that audio focus was lost.
     *
     * @param canDuck If true, audio can continue in "ducked" mode (low volume). Otherwise, all
     *                audio must stop.
     */
    public void onLostAudioFocus(boolean canDuck) {
//        Toast.makeText(getApplicationContext(), "lost audio focus." + (canDuck ? "can duck" :
//                "no duck"), Toast.LENGTH_SHORT).show();
        mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;

        if (isPlaying()) {
            mPauseReason = PauseReason.FocusLoss;
            pauseNow();
            bumpForwardSeconds(-3);
        }
    }

    void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null
                && mAudioFocusHelper.requestFocus())
            mAudioFocus = AudioFocus.Focused;
    }


}
