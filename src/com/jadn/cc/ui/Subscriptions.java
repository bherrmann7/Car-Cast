package com.jadn.cc.ui; import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.jadn.cc.R;
import com.jadn.cc.core.ExternalMediaStatus;
import com.jadn.cc.core.Subscription;
import com.jadn.cc.core.Util;
import com.jadn.cc.services.DownloadHistory;

public class Subscriptions extends BaseActivity {

	private static final String ERASE_SUBSCRIPTIONS_S_HISTORY = "Erase Subscriptions's History";
	private static final String DELETE_SUBSCRIPTION = "Delete Subscription";
	private static final String EDIT_SUBSCRIPTION = "Edit Subscription";

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// This occurred on the device... I don't know why
		if(contentService!=null)
			showSites();
	}

	@Override
	void onContentService() throws RemoteException {
		showSites();
	}

    @Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		ListView listView = (ListView) findViewById(R.id.siteList);
		Map<?, ?> rowData = (Map<?,?>)listView.getAdapter().getItem(info.position);

		Subscription sub = (Subscription)rowData.get("subscription");
		if (item.getTitle().equals(DELETE_SUBSCRIPTION)) {
			try {
                contentService.deleteSubscription(sub);

			} catch (RemoteException e) {
				// humm.
			}
			showSites();
			return false;

		} else if (item.getTitle().equals(EDIT_SUBSCRIPTION)) {
			Intent intent = new Intent(this, SubscriptionEdit.class);
            intent.putExtra("subscription", sub);
			startActivityForResult(intent, info.position);
		} else if (item.getTitle().equals(ERASE_SUBSCRIPTIONS_S_HISTORY)) {
			int erasedPodcasts = DownloadHistory.getInstance().eraseHistory(sub.name);
			Util.toast(this, "Removed "+erasedPodcasts+" podcasts from download history.");
		}
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.siteslist);

		setTitle("Car Cast: Subscriptions");

		ListView listView = (ListView) findViewById(R.id.siteList);
		registerForContextMenu(listView);

	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(EDIT_SUBSCRIPTION);
		menu.add(DELETE_SUBSCRIPTION);
		menu.add(ERASE_SUBSCRIPTIONS_S_HISTORY);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.edit_subscription_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		try {

			if (item.getItemId() == R.id.addSubscription) {
				startActivityForResult(
						new Intent(this, SubscriptionEdit.class),
						Integer.MAX_VALUE);
				return true;
			}
			if (item.getItemId() == R.id.deleteAllSubscriptions) {
				contentService.deleteAllSubscriptions();
				showSites();
				return true;
			}
			if (item.getItemId() == R.id.resetToDemoSubscriptions) {
				contentService.resetToDemoSubscriptions();
				showSites();
				return true;
			}
			if (item.getItemId() == R.id.search) {
				startActivityForResult(
						new Intent(this, Search.class),
						Integer.MAX_VALUE);				
				return true;
			}
		} catch (RemoteException e) {
			Log.e("", "", e);
		}
		return super.onMenuItemSelected(featureId, item);

	}

	protected void showSites() {
		ExternalMediaStatus status = ExternalMediaStatus.getExternalMediaStatus();

        if (status == ExternalMediaStatus.unavailable){
            Toast.makeText(getApplicationContext(),"Unable to read subscriptions from sdcard", Toast.LENGTH_LONG);
		    return;
		}

		List<Subscription> sites = getSubscriptions();
		// sort sites by name:
        Collections.sort(sites); 

        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

		for (Subscription sub: sites) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("name", sub.name);
			item.put("url", sub.url);
			item.put("subscription", sub);
			rows.add(item);
		}

		SimpleAdapter notes = new SimpleAdapter(this, rows,
				R.layout.main_item_two_line_row, new String[] { "name",
						"url" }, new int[] { R.id.text1, R.id.text2 });

		ListView listView = (ListView) findViewById(R.id.siteList);
		listView.setAdapter(notes);
	}

}
