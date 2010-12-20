package com.jadn.cc.ui;

import java.util.Arrays;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.admob.android.ads.AdManager;
import com.google.android.googlelogin.GoogleLoginServiceHelper;
import com.jadn.cc.R;
import com.jadn.cc.core.Config;
import com.jadn.cc.services.ContentService;
import com.jadn.cc.trace.ExceptionHandler;

public class CarCast extends BaseActivity {
	final static String tag = CarCast.class.getSimpleName();
	boolean toggleOnPause;
	Updater updater;
	private SharedPreferences app_preferences;
	int bgcolor;
	ImageButton pausePlay = null;
	
	// Need handler for callbacks to the UI thread
	final Handler handler = new Handler();

	// Create runnable for posting
	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			updateUI();
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		// Google responding to a name request
		if (requestCode == 123) {
			for (String key : data.getExtras().keySet()) {
				if (key.equals("accounts")) {
					String accounts = Arrays.toString(data.getExtras().getStringArray(key));
					SharedPreferences.Editor editor = app_preferences.edit();
					editor.putString("accounts", accounts);
					editor.commit();
				}
			}
		}

		updateUI();
	}

	@Override
	void onContentService() throws RemoteException {
		updatePausePlay();
		updateUI();
	}
	
	void updatePausePlay() throws RemoteException {
		if (contentService == null){
			return;
		}
		if (pausePlay == null){
			pausePlay = (ImageButton) findViewById(R.id.pausePlay);
		}
		if (contentService.isPlaying()) {
			pausePlay.setImageResource(R.drawable.player_102_pause);
		} else {
			pausePlay.setImageResource(R.drawable.player_102_play);
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		ExceptionHandler.register(this);

		super.onCreate(savedInstanceState);

		Intent csIntent = new Intent(getApplicationContext(), ContentService.class);
		startService(csIntent);
		bindService(csIntent, this, Context.BIND_AUTO_CREATE);
		
		registerReceiver(new BroadcastReceiver(){
			@Override
			public void onReceive(Context context, Intent intent){
				if (intent != null && intent.getExtras().getInt("state") == 0){
						try {
							if (contentService.isPlaying()){
								contentService.pause();
								contentService.bump(-2);
								updatePausePlay();
								updateUI();
							}
						} catch (RemoteException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
				}
			}
		}, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
		
		setTitle(getAppTitle());

		int width = getWindow().getWindowManager().getDefaultDisplay().getWidth();
		int height = getWindow().getWindowManager().getDefaultDisplay().getHeight();
		// how horrible... the shame. (this should be phone neutral.)
		if (width == 320 && height == 480) {
			setContentView(R.layout.main_relative_g1);
		} else if (width == 480 && height == 854) {
			setContentView(R.layout.main_relative_droid);
		} else {
			setContentView(R.layout.main_relative);

		}

		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);
		progressBar.setProgress(0);
		progressBar.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				try {
					contentService.moveTo(event.getX() / v.getWidth());
				} catch (RemoteException e) {
					esay(e);
				}
				updateUI();
				return true;
			}
		});

		final ImageButton pausePlay = (ImageButton) findViewById(R.id.pausePlay);
		pausePlay.setBackgroundColor(0x0);
		pausePlay.setSoundEffectsEnabled(true);
		pausePlay.setImageResource(R.drawable.player_102_play);
		pausePlay.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				try {
					if (contentService.getCount() == 0)
						return;
					if (contentService.pauseOrPlay()) {
						pausePlay.setImageResource(R.drawable.player_102_pause);
					} else {
						pausePlay.setImageResource(R.drawable.player_102_play);
					}
					updateUI();
				} catch (RemoteException e) {
					esay(e);
				}
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
				if (event.getAction() != MotionEvent.ACTION_UP)
					return true;
				// if clicking on the audio recorder (lower 1/3 of screen on 1/2 of right)
				if (event.getAction() == MotionEvent.ACTION_UP && event.getX() > (v.getWidth() * 0.66)
						&& (event.getY()) > (v.getHeight() * 0.5)) {
					try {
						if (contentService.isPlaying()) {
							contentService.pause();
						}
					} catch (Exception e) {
						esay(e);
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

		String lastRun = app_preferences.getString("lastRun", null);
		if (lastRun == null || app_preferences.getBoolean("showSplash", false)) {
			startActivity(new Intent(this, Splash.class));
			SharedPreferences.Editor editor = app_preferences.edit();
			editor.putBoolean("showSplash", false);
			editor.commit();
		} else if (!lastRun.equals(releaseData[0])) {
			new AlertDialog.Builder(CarCast.this).setTitle(getAppTitle()+" updated").setMessage(releaseData[1]).setNeutralButton("Close", null)
					.show();
		}
		saveLastRun();

		String accounts = app_preferences.getString("accounts", null);
		if (accounts == null) {
			GoogleLoginServiceHelper.getAccount(this, 123, true);
		}
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.player_menu, menu);
		
		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		try {
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
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getAppTitle()+": about podcast " + contentService.getCurrentTitle());
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
		} catch (RemoteException re) {
			esay(re);
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

		updater = new Updater(handler, mUpdateResults);
	}

	private void saveLastRun() {
		SharedPreferences.Editor editor = app_preferences.edit();
		editor.putString("lastRun", releaseData[0]);
		if (!app_preferences.contains("listmax")) {
			editor.putString("listmax", "2");
		}
		editor.commit();
	}

	public void updateUI() {
		if (contentService == null)
			return;
		try {
			if (!android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {

				TextView textView = (TextView) findViewById(R.id.title);
				textView.setText("ERROR ** "+getAppTitle()+" requires the sdcard ** ");
				return;
			}
			if (!Config.PodcastsRoot.exists()) {
				if (!Config.PodcastsRoot.mkdirs()) {
					TextView textView = (TextView) findViewById(R.id.title);
					textView.setText("ERROR ** "+getAppTitle()+" cannot write to sdcard ** ");
					return;
				}
			}

			TextView textView = (TextView) findViewById(R.id.subscriptionName);
			textView.setText(contentService.getCurrentSubscriptionName());

			textView = (TextView) findViewById(R.id.title);
			textView.setText(contentService.getCurrentTitle());

			textView = (TextView) findViewById(R.id.location);
			if (MediaMode.valueOf(contentService.getMediaMode()) == MediaMode.Paused) {
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

}
