package com.jadn.cc.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.jadn.cc.R;
import com.jadn.cc.core.Config;
import com.jadn.cc.core.ExternalMediaStatus;
import com.jadn.cc.core.OrderingPreference;
import com.jadn.cc.core.Subscription;
import com.jadn.cc.core.Util;
import com.jadn.cc.services.DownloadHistory;
import com.jadn.cc.services.EnclosureHandler;

public class SubscriptionEdit extends BaseActivity implements Runnable {

	Subscription currentSub;
	ProgressDialog dialog;

	@Override
	protected void onContentService() {
		if (currentSub != null) {
			((TextView) findViewById(R.id.editsite_name)).setText(currentSub.name);
			((TextView) findViewById(R.id.editsite_url)).setText(currentSub.url);
			((CheckBox) findViewById(R.id.enabled)).setChecked(currentSub.enabled);
			((CheckBox) findViewById(R.id.fifoLifo)).setChecked(currentSub.orderingPreference == OrderingPreference.FIFO);
			Spinner spinner = (Spinner) findViewById(R.id.subMax);
			int max = currentSub.maxDownloads;
			for (int i = 0; i < mValues.length; i++) {
				if (max == mValues[i])
					spinner.setSelection(i);
			}

		} // end if
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_subscription);

		currentSub = null;

		((Button) findViewById(R.id.saveEditSite)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String name = ((TextView) findViewById(R.id.editsite_name)).getText().toString();
				String url = ((TextView) findViewById(R.id.editsite_url)).getText().toString();
				Boolean enabled = ((CheckBox) findViewById(R.id.enabled)).isChecked();
				CheckBox newestFirst = ((CheckBox) findViewById(R.id.fifoLifo));
				Spinner spinner = (Spinner) findViewById(R.id.subMax);
				int max = mValues[spinner.getSelectedItemPosition()];
				OrderingPreference orderingPreference = OrderingPreference.FIFO;
				if (!newestFirst.isChecked()) {
					orderingPreference = OrderingPreference.LIFO;
				}

				// try out the url:
				if (!Util.isValidURL(url)) {
					Util.toast(SubscriptionEdit.this, "URL to site is malformed.");
					return;
				} // endif

				ExternalMediaStatus status = ExternalMediaStatus.getExternalMediaStatus();
				if (status != ExternalMediaStatus.writeable) {
					// unable to access sdcard
					Toast.makeText(getApplicationContext(), "Unable to add subscription to sdcard", Toast.LENGTH_LONG);
					return;
				}

				Subscription newSub = new Subscription(name, url, max, orderingPreference, enabled);
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

		((Button) findViewById(R.id.testEditSite)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				testUrl();
			}


		});

		Spinner s1 = (Spinner) findViewById(R.id.subMax);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mStrings);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s1.setAdapter(adapter);
		if (currentSub != null) {
			for (int i = 0; i < mValues.length; i++) {
				if (mValues[i] == currentSub.maxDownloads) {
					s1.setSelection(i);
				}
			}
		}
		
		
		if (getIntent().hasExtra("subscription")) {
			currentSub = (Subscription) getIntent().getExtras().get(
					"subscription");
		} else {
			// we're coming from the browser
			if( Intent.ACTION_VIEW.equals( getIntent().getAction() ) ) {
				Log.d("onCreate", "data: "+getIntent().getDataString());
				String feedUrl = getIntent().getDataString();
				((TextView) findViewById(R.id.editsite_url)).setText(feedUrl);
//				currentSub = new Subscription("", feedUrl);
				testUrl();
			}
		}


	}
	
	private void testUrl() {
		DownloadHistory history = DownloadHistory.getInstance();
		encloseureHandler = new EnclosureHandler(history);
		Spinner spinner = (Spinner) findViewById(R.id.subMax);
		int max = mValues[spinner.getSelectedItemPosition()];
		if (max == Subscription.GLOBAL) {
			max = Config.getMax(SubscriptionEdit.this);
		}
		encloseureHandler.setMax(max);

		dialog = ProgressDialog.show(SubscriptionEdit.this, "Testing Subscription", "Testing Subscription URL.\nPlease wait...",
				true);
		dialog.show();

		new Thread(SubscriptionEdit.this).start();

	}


	private static final String[] mStrings = { "global setting", "2", "4", "6", "10", "Unlimited" };
	private static final int[] mValues = { Subscription.GLOBAL, 2, 4, 6, 10, EnclosureHandler.UNLIMITED };

	EnclosureHandler encloseureHandler;

	@Override
	public void run() {
		testException = null;
		try {
			Util.findAvailablePodcasts(getURL(), encloseureHandler);
		} catch (Exception e) {
			testException = e;
		}

		handler.sendEmptyMessage(0);
	}

	private String getURL() {
		return ((TextView) findViewById(R.id.editsite_url)).getText().toString();
	}

	Exception testException;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			dialog.dismiss();
			if (testException != null) {
				Log.e("editSite", "testURL " + getURL(), testException);
				Util.toast(SubscriptionEdit.this, "Problem accessing feed. " + testException.toString());
				TextView urlTV = (TextView) findViewById(R.id.editsite_url);
				urlTV.requestFocus();
				return;
			}
			Util.toast(SubscriptionEdit.this, "Feed is OK.  Would download " + encloseureHandler.metaNets.size() + " podcasts.");

			TextView nameTV = ((TextView) findViewById(R.id.editsite_name));
			if (encloseureHandler.title.length() != 0 && nameTV.getText().length() == 0) {
				nameTV.setText(encloseureHandler.getTitle());
			}
			
			((Button) findViewById(R.id.saveEditSite)).requestFocus();

		}
	};

}
