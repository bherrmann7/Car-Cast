package com.jadn.cc.ui;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.jadn.cc.R;
import com.jadn.cc.core.Config;
import com.jadn.cc.core.ExternalMediaStatus;
import com.jadn.cc.core.Sayer;
import com.jadn.cc.core.Subscription;
import com.jadn.cc.core.Util;
import com.jadn.cc.services.DownloadHelper;
import com.jadn.cc.services.DownloadHistory;
import com.jadn.cc.services.EnclosureHandler;

public class SubscriptionEdit extends BaseActivity implements Runnable {

	Subscription currentSub;
	ProgressDialog dialog;

	@Override
	protected void onContentService() {
		if (currentSub != null) {
			((TextView) findViewById(R.id.editsite_name))
					.setText(currentSub.name);
			((TextView) findViewById(R.id.editsite_url))
					.setText(currentSub.url);
			((CheckBox) findViewById(R.id.enabled))
					.setChecked(currentSub.enabled);
			// TODO: add max count, ordering here
		} // end if
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_subscription);

		currentSub = null;

		if (getIntent().getExtras() != null) {
			currentSub = (Subscription) getIntent().getExtras().get(
					"subscription");
		}

		((Button) findViewById(R.id.saveEditSite))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						String name = ((TextView) findViewById(R.id.editsite_name))
								.getText().toString();
						String url = ((TextView) findViewById(R.id.editsite_url))
								.getText().toString();
						Boolean enabled = ((CheckBox) findViewById(R.id.enabled))
								.isChecked();
						// TODO: add max count, ordering here

						// try out the url:
						if (!Util.isValidURL(url)) {
							Util.toast(SubscriptionEdit.this,
									"URL to site is malformed.");
							return;
						} // endif

						ExternalMediaStatus status = ExternalMediaStatus
								.getExternalMediaStatus();
						if (status != ExternalMediaStatus.writeable) {
							// unable to access sdcard
							Toast.makeText(getApplicationContext(),
									"Unable to add subscription to sdcard",
									Toast.LENGTH_LONG);
							return;
						}

						Subscription newSub = new Subscription(name, url,
								enabled); // TODO add max count, ordering
						if (currentSub != null) {
							// edit:
							contentService.editSubscription(currentSub, newSub);

						} else {
							// add:
							contentService.addSubscription(newSub);
						} // endif

						SubscriptionEdit.this.setResult(RESULT_OK);
						SubscriptionEdit.this.finish();
					}

				});

		((Button) findViewById(R.id.testEditSite))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						DownloadHistory history = DownloadHistory.getInstance();
						encloseureHandler = new EnclosureHandler(Config
								.getMax(SubscriptionEdit.this), history);

						dialog = ProgressDialog.show(
								SubscriptionEdit.this, "Testing Subscription",
								"Testing Subscription URL.\nPlease wait...",
								true);
						dialog.show();

						new Thread(SubscriptionEdit.this).start();

					}

				});
	}

	EnclosureHandler encloseureHandler;

	@Override
	public void run() {
		testException = null;
		try {
			Util.downloadPodcast(getURL(), encloseureHandler);
		} catch (Exception e) {
			testException = e;
		}

		handler.sendEmptyMessage(0);
	}

	private String getURL() {
		return ((TextView) findViewById(R.id.editsite_url)).getText()
				.toString();
	}

	Exception testException;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			dialog.dismiss();
			if (testException != null) {
				Log.e("editSite", "testURL " + getURL(), testException);
				Util.toast(SubscriptionEdit.this, "Problem accessing feed. "
						+ testException.toString());
				return;
			}
			Util.toast(SubscriptionEdit.this, "Feed is OK.  Would download "
					+ encloseureHandler.metaNets.size() + " podcasts.");

			TextView nameTV = ((TextView) findViewById(R.id.editsite_name));
			if (encloseureHandler.title.length() != 0
					&& nameTV.getText().length() == 0) {
				nameTV.setText(encloseureHandler.getTitle());
			}

		}
	};

}
