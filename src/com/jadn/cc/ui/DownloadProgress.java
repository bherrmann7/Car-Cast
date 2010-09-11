package com.jadn.cc.ui; import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.jadn.cc.R;
import com.jadn.cc.core.Config;

public class DownloadProgress extends BaseActivity implements Runnable {

	final Handler handler = new Handler();

	Updater updater;

	boolean wasStarted;

	@Override
	void onContentService() throws RemoteException {
		boolean idle = contentService.encodedDownloadStatus().equals("");
		Button startDownloads = (Button) findViewById(R.id.startDownloads);
		Button abort = (Button) findViewById(R.id.AbortDownloads);
		startDownloads.setEnabled(idle);
		abort.setEnabled(!idle);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download_progress);

		final Button startDownloads = (Button) findViewById(R.id.startDownloads);
		startDownloads.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				SharedPreferences app_preferences = PreferenceManager.getDefaultSharedPreferences(DownloadProgress.this);
				if (app_preferences.getBoolean("downloadDetails", false)) {
					startActivity(new Intent(DownloadProgress.this, Downloader.class));
					return;
				}

				try {
					reset();
					contentService.startDownloadingNewPodCasts(Config
							.getMax(DownloadProgress.this));
				} catch (RemoteException re) {
					esay(re);
				}

				findViewById(R.id.AbortDownloads).setEnabled(true);
				startDownloads.setEnabled(false);
			}
		});

		final Button abort = (Button) findViewById(R.id.AbortDownloads);
		abort.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				NotificationManager mNotificationManager = (NotificationManager) getSystemService(Activity.NOTIFICATION_SERVICE);
				mNotificationManager.cancel(22);
				mNotificationManager.cancel(23);
				// Crude... but effective...
				System.exit(-1);
			}
		});
		
		startDownloads.setEnabled(false);
		abort.setEnabled(false);

		reset();

	}

	@Override
	protected void onPause() {
		super.onPause();

		// stop display thread
		updater.allDone();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updater = new Updater(handler, this);
		
		try {
			if(!contentService.encodedDownloadStatus().equals("")){
				findViewById(R.id.startDownloads).setEnabled(false);
				findViewById(R.id.AbortDownloads).setEnabled(true);
			}
		} catch (RemoteException e) {
		}
	}

	private void reset() {

		TextView labelSubscriptionSites = (TextView) findViewById(R.id.labelSubscriptionSites);
		TextView scanning = (TextView) findViewById(R.id.scanning);
		TextView downloadingLabel = (TextView) findViewById(R.id.downloadingLabel);
		TextView progressSimple = (TextView) findViewById(R.id.progressSimple);
		TextView progressBytes = (TextView) findViewById(R.id.progressBytes);
		ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);
		TextView subscriptionName = (TextView) findViewById(R.id.subscriptionName);
		TextView title = (TextView) findViewById(R.id.title);

		labelSubscriptionSites.setVisibility(TextView.INVISIBLE);
		scanning.setVisibility(TextView.INVISIBLE);
		downloadingLabel.setVisibility(TextView.INVISIBLE);
		progressSimple.setVisibility(TextView.INVISIBLE);
		progressBytes.setVisibility(TextView.INVISIBLE);
		progressBar.setVisibility(TextView.INVISIBLE);

		scanning.setText("");
		progressSimple.setText("");
		progressBytes.setText("");
		progressBar.setProgress(0);
		subscriptionName.setText("");
		title.setText("");
	}

	// Called once a second in the UI thread to update the screen.
	public void run() {
		String downloadStatus = null;
		try {
			downloadStatus=contentService.encodedDownloadStatus();
			if (downloadStatus.equals("")) {
				if (wasStarted) {
					wasStarted = false;
					TextView downloadingLabel = (TextView) findViewById(R.id.downloadingLabel);
					TextView progressSimple = (TextView) findViewById(R.id.progressSimple);
					if (progressSimple.getText().equals("")) {
						downloadingLabel.setVisibility(TextView.VISIBLE);
						progressSimple.setVisibility(TextView.VISIBLE);
						progressSimple.setText("  No new podcasts found.");
					} else {
						TextView subscriptionName = (TextView) findViewById(R.id.subscriptionName);
						TextView title = (TextView) findViewById(R.id.title);
						subscriptionName.setText("");
						title.setText("   *** COMPLETED ***");
					}
				}
				findViewById(R.id.startDownloads).setEnabled(true);
				findViewById(R.id.AbortDownloads).setEnabled(false);
			} else {
				wasStarted = true;
				TextView labelSubscriptionSites = (TextView) findViewById(R.id.labelSubscriptionSites);
				TextView scanning = (TextView) findViewById(R.id.scanning);
				labelSubscriptionSites.setVisibility(TextView.VISIBLE);
				scanning.setVisibility(TextView.VISIBLE);
				TextView downloadingLabel = (TextView) findViewById(R.id.downloadingLabel);
				TextView progressSimple = (TextView) findViewById(R.id.progressSimple);
				TextView progressBytes = (TextView) findViewById(R.id.progressBytes);
				ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress);

				String[] status = downloadStatus.split(",");
				labelSubscriptionSites.setVisibility(TextView.VISIBLE);
				scanning.setText("  Scanning sites " + status[0] + "/"
						+ status[1]);
				if (status[3].equals("0")) {
					downloadingLabel.setVisibility(TextView.INVISIBLE);
					progressSimple.setVisibility(TextView.INVISIBLE);
					progressBytes.setVisibility(TextView.INVISIBLE);
					progressBar.setVisibility(TextView.INVISIBLE);
					progressBar.setProgress(0);
				} else {
					downloadingLabel.setVisibility(TextView.VISIBLE);
					progressSimple.setVisibility(TextView.VISIBLE);
					progressBytes.setVisibility(TextView.VISIBLE);
					progressBar.setVisibility(TextView.VISIBLE);
					progressSimple.setText("   Podcast " + status[2] + "/"
							+ status[3]);
					long cb = Long.parseLong(status[4]);
					long tb = Long.parseLong(status[5]);
					if (tb == 0) {
						progressBytes.setText("");
						progressBar.setProgress(0);
					} else {
						progressBytes.setText("   " + cb / 1024 + "k/" + tb
								/ 1024 + "k");
						progressBar.setProgress((int) ((cb * 100) / tb));
					}
				}
				TextView subscriptionName = (TextView) findViewById(R.id.subscriptionName);
				TextView title = (TextView) findViewById(R.id.title);
				subscriptionName.setText(status[6]);
				title.setText(status[7]);
			}
		} catch (Exception e) {
			esay(new RuntimeException("downloadStatus was: "+downloadStatus,e));
		}
	}

}