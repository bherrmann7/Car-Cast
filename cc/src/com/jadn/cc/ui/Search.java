package com.jadn.cc.ui; import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.jadn.cc.R;
import com.jadn.cc.core.CarCastApplication;

public class Search extends BaseActivity {

	// Need handler for callbacks to the UI thread
	final Handler handler = new Handler();

	// Create runnable for posting
	final Runnable mUpdateResults = new Runnable() {
		@Override public void run() {
			if (contentService.startSearch("-status-").equals("done")) {
				updater.allDone();
				if (contentService.startSearch("-results-").equals("")) {
					Toast.makeText(getApplicationContext(),
							"No Results Found.", Toast.LENGTH_LONG).show();
					TextView searchText = (TextView) findViewById(R.id.searchText);
					Button searchButton = (Button) findViewById(R.id.searchButton);
					searchButton.setEnabled(true);
					searchText.setEnabled(true);
				} else {
					startActivity(new Intent(Search.this,
							SearchResults.class));
				}
			}
		}
	};

	Updater updater;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search);

		setTitle(CarCastApplication.getAppTitle()+": search for new subscriptions");

		// ListView listView = (ListView) findViewById(R.id.siteList);
		// registerForContextMenu(listView);

		final TextView searchText = (TextView) findViewById(R.id.searchText);
		final Button searchButton = (Button) findViewById(R.id.searchButton);
		searchText.setOnEditorActionListener(new OnEditorActionListener(){

			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				searchButton.setEnabled(false);
				searchText.setEnabled(false);
				contentService.startSearch(searchText.getText().toString());
				updater = new Updater(handler, mUpdateResults);
				return true;
			}});

		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchButton.setEnabled(false);
				searchText.setEnabled(false);
				contentService.startSearch(searchText.getText().toString());
				updater = new Updater(handler, mUpdateResults);
			}
		});

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (updater!=null)
			updater.allDone();
	}

	@Override
	protected void onResume() {
		super.onResume();
		TextView searchText = (TextView) findViewById(R.id.searchText);
		Button searchButton = (Button) findViewById(R.id.searchButton);
		searchButton.setEnabled(true);
		searchText.setEnabled(true);
	}

}
