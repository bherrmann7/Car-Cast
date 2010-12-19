package com.jadn.cc.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.jadn.cc.R;
import com.jadn.cc.core.ExternalMediaStatus;
import com.jadn.cc.core.Subscription;
import com.jadn.cc.core.Util;
import com.jadn.cc.services.DownloadHistory;

/**
 * A good video about listview http://code.google.com/events/io/2010/sessions/world-of-listview-android.html
 * 
 * Although right now Subscriptions is using a very simple approach
 */

public class Subscriptions extends BaseActivity {

	private static final String DISABLE_SUBSCRIPTION = "Disable";
	private static final String ENABLE_SUBSCRIPTION = "Enable";
	private static final String DELETE_SUBSCRIPTION = "Delete";
	private static final String EDIT_SUBSCRIPTION = "Edit";
	private static final String ERASE_SUBSCRIPTIONS_S_HISTORY = "Erase History";

	SimpleAdapter listAdapter;
	ListView listView;
	List<Map<String, Object>> subscriptions = new ArrayList<Map<String, Object>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.subscription_list);
		setTitle(getAppTitle()+": Subscriptions");
		listView = (ListView) findViewById(R.id.siteList);
		registerForContextMenu(listView);

		ExternalMediaStatus status = ExternalMediaStatus.getExternalMediaStatus();
		if (status == ExternalMediaStatus.unavailable) {
			Toast.makeText(getApplicationContext(), "Unable to read subscriptions from sdcard", Toast.LENGTH_LONG);
			return;
		}

		listAdapter = new SimpleAdapter(this, subscriptions, R.layout.main_item_two_line_row, new String[] { "name", "enabled" }, new int[] {
				R.id.text1, R.id.text2 });
		listView.setAdapter(listAdapter);
	}

	// Invoked when returning from a subscription edit
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		reloadSubscriptions();
	}

	// Invoked when the background service is bound (hooked up) to this Activity
	@Override
	void onContentService() throws RemoteException {
		reloadSubscriptions();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Map<?, ?> rowData = (Map<?, ?>) listView.getAdapter().getItem(info.position);

		Subscription sub = (Subscription) rowData.get("subscription");
		
		if(item.getTitle().equals(DISABLE_SUBSCRIPTION) || item.getTitle().equals(ENABLE_SUBSCRIPTION))
		{
			try {
				contentService.toggleSubscription(sub);
			} catch (RemoteException re) {
				esay(re);
				return true;
			}
			reloadSubscriptions();
			Subscriptions.this.listAdapter.notifyDataSetChanged();
			return true;
			
		} else if (item.getTitle().equals(DELETE_SUBSCRIPTION)) {
			try {
				contentService.deleteSubscription(sub);
			} catch (RemoteException re) {
				esay(re);
				return true;
			}
			Subscriptions.this.subscriptions.remove(info.position);
			Subscriptions.this.listAdapter.notifyDataSetChanged();
			return true;

		} else if (item.getTitle().equals(EDIT_SUBSCRIPTION)) {
			Intent intent = new Intent(this, SubscriptionEdit.class);
			intent.putExtra("subscription", sub);
			startActivityForResult(intent, info.position);
		} else if (item.getTitle().equals(ERASE_SUBSCRIPTIONS_S_HISTORY)) {
			int erasedPodcasts = DownloadHistory.getInstance().eraseHistory(sub.name);
			Util.toast(this, "Removed " + erasedPodcasts + " podcasts from download history.");
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		Map<?, ?> rowData = (Map<?, ?>) listView.getAdapter().getItem(info.position);
		Subscription sub = (Subscription) rowData.get("subscription");
		menu.setHeaderTitle(sub.name);
		
		if (sub.enabled)
			menu.add(DISABLE_SUBSCRIPTION);
		else
			menu.add(ENABLE_SUBSCRIPTION);
		
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
				startActivityForResult(new Intent(this, SubscriptionEdit.class), Integer.MAX_VALUE);
				return true;
			}
			if (item.getItemId() == R.id.deleteAllSubscriptions) {
				contentService.deleteAllSubscriptions();
				reloadSubscriptions();
				return true;
			}
			if (item.getItemId() == R.id.resetToDemoSubscriptions) {
				contentService.resetToDemoSubscriptions();
				reloadSubscriptions();
				return true;
			}
			if (item.getItemId() == R.id.search) {
				startActivityForResult(new Intent(this, Search.class), Integer.MAX_VALUE);
				return true;
			}
		} catch (RemoteException e) {
			Log.e("", "", e);
		}
		return super.onMenuItemSelected(featureId, item);

	}

	protected void reloadSubscriptions() {
		subscriptions.clear();

		// If we have no content service... then game over... cant display anything
		List<Subscription> sites = new ArrayList<Subscription>();
		if (contentService != null) {
			sites = getSubscriptions();
		}
		// sort sites by name:
		Collections.sort(sites);

		for (Subscription sub : sites) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("name", sub.name);
			
			if(sub.enabled)
				item.put("enabled", "");
			else
				item.put("enabled", "(Disabled)");
			
			item.put("subscription", sub);
			subscriptions.add(item);
		}

		listAdapter.notifyDataSetChanged();
	}

}
