package com.jadn.cc.ui; import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import com.jadn.cc.core.Subscription;

public class Subscriptions extends BaseActivity {

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
		menu.add("Edit");
		menu.add("Delete");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		if (item.getTitle().equals("Delete")) {
			try {
				contentService.deleteSite(info.position);
			} catch (RemoteException e) {
				// humm.
			}
			showSites();
			return false;
		}
		if (item.getTitle().equals("Edit")) {
			Intent intent = new Intent(this, SubscriptionEdit.class);
			intent.putExtra("site", info.position);
			startActivityForResult(intent, info.position);
		}
		return true;
	}

	protected void showSites() {

		ListView listView = (ListView) findViewById(R.id.siteList);

		List<Subscription> sites = getSubscriptions();
		if(sites==null){
			Toast.makeText(getApplicationContext(),"Unable to access sdcard", Toast.LENGTH_LONG);
		}
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		for (Subscription site : sites) {
			HashMap<String, String> item = new HashMap<String, String>();
			item.put("line1", site.name);
			item.put("line2", site.url.toString());
			list.add(item);

		}
		SimpleAdapter notes = new SimpleAdapter(this, list,
				R.layout.main_item_two_line_row, new String[] { "line1",
						"line2" }, new int[] { R.id.text1, R.id.text2 });
		listView.setAdapter(notes);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.editsite_menu, menu);
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

}
