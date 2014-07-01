package com.jadn.cc.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jadn.cc.R;
import com.jadn.cc.core.CarCastApplication;
import com.jadn.cc.core.Config;
import com.jadn.cc.core.MediaMode;
import com.jadn.cc.trace.ExceptionHandler;
import com.jadn.cc.util.RecordingSet;
import com.jadn.cc.util.Updater;

import java.io.File;
import java.lang.reflect.Method;

public class CarCast extends BaseActivity {
	final static String tag = CarCast.class.getSimpleName();
	boolean toggleOnPause;
	Updater updater;
	private SharedPreferences app_preferences;
	int bgcolor;
	ImageButton pausePlay = null;
    private Config config;
    private File podroot;

	// Need handler for callbacks to the UI thread
	final Handler handler = new Handler();

	// Create runnable for posting
	final Runnable mUpdateResults = new Runnable() {
		@Override
		public void run() {
			updateUI();
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		updateUI();
	}

	@Override
	public void playStateUpdated(boolean playing) {
		updatePausePlay();
		updateUI();
	}

	@Override
	protected void onContentService() {
		updatePausePlay();
		updateUI();
	}

	void updatePausePlay() {
		if (contentService == null) {
			return;
		}
		if (pausePlay == null) {
			pausePlay = (ImageButton) findViewById(R.id.pausePlay);
		}
		if (contentService.isPlaying()) {
			pausePlay.setImageResource(R.drawable.player_102_pause);
		} else {
			pausePlay.setImageResource(R.drawable.player_102_play);
		}
	}

    // Yea, should be something like "Capabilities.has(MICROPHONE)" - but Android doesnt seem to have this
	private boolean isDeviceWithoutMicrophone() {
        // From https://developer.amazon.com/appsandservices/solutions/devices/kindle-fire/specifications/01-device-and-feature-specifications
        // The Kindle Fire HDX has a microphone
        if ( android.os.Build.MODEL.equals("KFAPWA (WAN)") ||
                android.os.Build.MODEL.equals("KFAPWI (Wi-Fi)") ){
            return false;
        }
        // Other kindles do not.
		return android.os.Build.MODEL.equals("Kindle Fire")
                || android.os.Build.MODEL.startsWith("KF");
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// google handles surprise exceptions
	    //ExceptionHandler.register(this);

		super.onCreate(savedInstanceState);

        ExceptionHandler.setTraceData(getApplicationContext());

		setTitle(CarCastApplication.getAppTitle());

		setContentView(R.layout.player);

		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);
		progressBar.setProgress(0);
		progressBar.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				contentService.moveTo(event.getX() / v.getWidth());
				updateUI();
				return true;
			}
		});

		final ImageButton pausePlay = (ImageButton) findViewById(R.id.pausePlay);
		pausePlay.setBackgroundColor(0x0);
		pausePlay.setSoundEffectsEnabled(true);
		pausePlay.setImageResource(R.drawable.player_102_play);
		pausePlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (contentService.getCount() == 0)
					return;
				if (contentService.pauseOrPlay()) {
					pausePlay.setImageResource(R.drawable.player_102_pause);
				} else {
					pausePlay.setImageResource(R.drawable.player_102_play);
				}
				updateUI();
			}
		});

		ImageButton rewind30Button = (ImageButton) findViewById(R.id.rewind30);
		rewind30Button.setBackgroundColor(0x0);
		rewind30Button.setSoundEffectsEnabled(true);
		rewind30Button.setOnClickListener(new Bumper(this, -30));

		ImageButton forward60Button = (ImageButton) findViewById(R.id.forward30);
		forward60Button.setBackgroundColor(0x0);
		forward60Button.setSoundEffectsEnabled(true);
		forward60Button.setOnClickListener(new Bumper(this, 30));

		ImageButton nextButton = (ImageButton) findViewById(R.id.next);
		nextButton.setBackgroundColor(0x0);
		nextButton.setSoundEffectsEnabled(true);
		nextButton.setOnClickListener(new BumpCast(this, true));

		ImageButton previousButton = (ImageButton) findViewById(R.id.previous);
		previousButton.setBackgroundColor(0x0);
		previousButton.setSoundEffectsEnabled(true);
		previousButton.setOnClickListener(new BumpCast(this, false));

		TextView textView = (TextView) findViewById(R.id.title);
		textView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (isAudioRecorderOff())
					return true;
				if (event.getAction() != MotionEvent.ACTION_UP)
					return true;
				// if clicking on the audio recorder (lower 1/3 of screen on 1/2
				// of right)
				if (event.getAction() == MotionEvent.ACTION_UP && event.getX() > (v.getWidth() * 0.66)
						&& (event.getY()) > (v.getHeight() * 0.5)) {
					try {
						if (contentService.isPlaying()) {
							contentService.pauseNow();
						}
					} catch (Exception e) {
						CarCastApplication.esay(e);
					}

					pausePlay.setImageResource(R.drawable.player_102_play);
					startActivityForResult(new Intent(CarCast.this, AudioRecorder.class), 0);
				}
				return true;
			}

		});

		app_preferences = PreferenceManager.getDefaultSharedPreferences(this);

		if (!app_preferences.contains("listmax")) {
			SharedPreferences.Editor editor = app_preferences.edit();
			editor.putString("listmax", "2");
			editor.commit();
		}

		if (!app_preferences.contains("speedChoice")) {
			SharedPreferences.Editor editor = app_preferences.edit();
			editor.putString("speedChoice", "1");
			editor.commit();
		}

		if (!app_preferences.contains("variableSpeedEnabled")) {
			SharedPreferences.Editor editor = app_preferences.edit();
			editor.putBoolean("variableSpeedEnabled", false);
			editor.commit();
		}

		String lastRun = app_preferences.getString("lastRun", null);
		if (lastRun == null || app_preferences.getBoolean("showSplash", false)) {
			startActivity(new Intent(this, Splash.class));
			SharedPreferences.Editor editor = app_preferences.edit();
			editor.putBoolean("showSplash", false);
			editor.commit();
		} else if (!lastRun.equals(CarCastApplication.releaseData[0])) {
				new AlertDialog.Builder(CarCast.this).setTitle(CarCastApplication.getAppTitle() + " updated")
						.setMessage(CarCastApplication.releaseData[1]).setNeutralButton("Close", null).show();
		}
		saveLastRun();

		String[] orientations = { "AUTO", "Landscape", "Flipped Landscape", "Portrait", "Flipped Portrait" };
		int[] orientationValues = { ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
				ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
				ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT };
		String orientation = app_preferences.getString("orientation", null);
		if (orientation != null) {
			for (int i = 0; i < orientations.length; i++) {
				if (orientation.equals(orientations[i])) {
					setRequestedOrientation(orientationValues[i]);
					Log.i("CarCast", "Orientation set to " + orientation + " v=" + orientationValues[i]);
					break;
				}
			}
		}

	}

    public boolean isAudioRecorderOff(){
        if (isDeviceWithoutMicrophone()){
            return true;
        }
        app_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!app_preferences.contains("audioRecorderOff")) {
            SharedPreferences.Editor editor = app_preferences.edit();
            editor.putBoolean("audioRecorderOff", false);
            editor.commit();
        }
        return app_preferences.getBoolean("audioRecorderOff", false);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.player_menu, menu);

		return true;
	}

	@Override
	public void finish() {
		Log.i("CarCast", "Finishing CC; contentService is " + contentService);
		if (contentService != null && contentService.isIdle()) {
			getCarCastApplication().stopContentService();
		}
		super.finish();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == R.id.purgeAll) {
			contentService.purgeAll();
			return true;
		}
		if (item.getItemId() == R.id.downloadNewPodcasts) {

			startActivityForResult(new Intent(this, DownloadProgress.class), 0);

			return true;
		}
		if (item.getItemId() == R.id.email) {
			Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "" });
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, CarCastApplication.getAppTitle() + ": about podcast "
					+ contentService.currentTitle());
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, contentService.getPodcastEmailSummary());
			startActivity(Intent.createChooser(emailIntent, "Email about podcast"));
		}
		if (item.getItemId() == R.id.feedback) {
			startActivityForResult(new Intent(this, FeedbackHelp.class), 0);
			return true;
		}
		if (item.getItemId() == R.id.settings) {
			startActivity(new Intent(this, Settings.class));
			return true;
		}
		if (item.getItemId() == R.id.menuSiteList) {
			startActivityForResult(new Intent(this, Subscriptions.class), 0);
			return true;
		}
		// if (item.getItemId() == R.id.delete) {
		// startActivityForResult(new Intent(this, Delete.class), 0);
		// return true;
		// }
		if (item.getItemId() == R.id.listPodcasts) {
			// startActivityForResult(new Intent(this, Search.class), 0);
			startActivityForResult(new Intent(this, PodcastList.class), 0);
			return true;
		}
		return false;
	}

	@Override
	protected void onPause() {
		super.onPause();

		updater.allDone();
	}

	@Override
	protected void onResume() {
		super.onResume();
        config = null;

		if (app_preferences.getBoolean("keep_display_on", true))
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		else
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            TextView titleTextView = (TextView) findViewById(R.id.title);

        RecordingSet recordingSet = new RecordingSet(this);
        if(isAudioRecorderOff()){
            recordingSet.clearNotifications();
            titleTextView.setBackgroundDrawable((Drawable) null);
        } else {
            titleTextView.setBackgroundResource(R.drawable.background_319);
            recordingSet.updateNotification();
        }

        updater = new Updater(handler, mUpdateResults);
	}

	private void saveLastRun() {
		SharedPreferences.Editor editor = app_preferences.edit();
		editor.putString("lastRun", CarCastApplication.releaseData[0]);
		if (!app_preferences.contains("listmax")) {
			editor.putString("listmax", "2");
		}
		editor.commit();
	}


	public void updateUI() {
		if (contentService == null)
			return;
        if(config == null){
            config = new Config(getApplicationContext());
            podroot = config.getPodcastsRoot();
        }
		try {
			if (!podroot.exists() || !podroot.canWrite()) {
				if (!podroot.mkdirs() || !podroot.canWrite()) {
					TextView textView = (TextView) findViewById(R.id.title);
                    StringBuilder sb = new StringBuilder();
                    sb.append("ERROR ** " + CarCastApplication.getAppTitle() + " cannot write to storage: "+podroot+" ** ");
                    sb.append(moreSpaceSuggestions());
					textView.setText(sb.toString());
					return;
				}
			}

			TextView textView = (TextView) findViewById(R.id.subscriptionName);
			textView.setText(contentService.getCurrentSubscriptionName());

			textView = (TextView) findViewById(R.id.title);
			textView.setText(contentService.currentTitle());

			textView = (TextView) findViewById(R.id.location);
			if (contentService.getMediaMode() == MediaMode.Paused) {
				if (toggleOnPause == true) {
					toggleOnPause = false;
					textView.setText("");
				} else {
					toggleOnPause = true;
					textView.setText(contentService.getLocationString());
				}
			} else {
				textView.setText(contentService.getLocationString());
			}

			textView = (TextView) findViewById(R.id.where);
			textView.setText(contentService.getWhereString());

			textView = (TextView) findViewById(R.id.duration);
			textView.setText(contentService.getDurationString());

			ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);
			progressBar.setProgress(contentService.currentProgress());
			updatePausePlay();

		} catch (Throwable e) {
			Log.e("cc", "", e);
		}
	}


    String moreSpaceSuggestions(){

        // getExternalFilesDirs
        try {
            Method method = getApplicationContext().getClass().getMethod("getExternalFilesDirs", String.class);

            File[] fileBases = (File[])method.invoke(getApplicationContext(), new Object[]{null});

            StringBuilder sb = new StringBuilder("\n\nTry one of:\n ");
            sb.append(" ");
            sb.append(android.os.Environment.getExternalStorageDirectory());
            sb.append("\n");
            for(File file: fileBases){
                sb.append("  ");
                sb.append(file);
                sb.append("\n");
            }

            return sb.toString();

        } catch (Throwable e){
            return "";
        }
    }
}
