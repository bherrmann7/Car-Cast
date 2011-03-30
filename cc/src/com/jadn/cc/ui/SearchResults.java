package com.jadn.cc.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Bundle;
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
import android.widget.AdapterView.OnItemClickListener;

import com.jadn.cc.R;
import com.jadn.cc.core.CarCastApplication;
import com.jadn.cc.core.Subscription;
import com.jadn.cc.trace.TraceUtil;

public class SearchResults extends BaseActivity {

	String lastResults;

	@SuppressWarnings("unchecked")
    private void add(int position) {
        ListView listView = (ListView) findViewById(R.id.siteList);
        Map<String, String> rowData = (Map<String, String>) listView.getAdapter().getItem(position);

        String name = rowData.get("name");
	    String url = rowData.get("url");

		boolean b = contentService.addSubscription(new Subscription(name, url));
		if (b) {
			Toast.makeText(getApplicationContext(),
					"Added subscription to " + name, Toast.LENGTH_LONG)
					.show();
		} else {
			Toast.makeText(getApplicationContext(),
					"Already subscribed to " + name, Toast.LENGTH_LONG)
					.show();
		}
	}

	private List<Subscription> getResults() {
		List<Subscription> res = new ArrayList<Subscription>();
		try {
			lastResults = contentService.startSearch("-results-");
			String[] lines = lastResults.split("\\n");
			for (String line : lines) {
				if (!line.trim().equals("") && !line.startsWith("#")) {
				    int eq = line.indexOf('=');
			        if (eq != -1) {
			            String name = line.substring(0, eq);
			            String url = line.substring(eq + 1);
			            res.add(new Subscription(name, url));
			        }
				}
			}
		} catch (Exception e) {
			CarCastApplication.esay(e);
		}
		return res;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			showResults();
		}
	}

	@Override
	protected void onContentService() {
		showResults();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		if (item.getTitle().equals("Subscribe")) {
		    add(info.position);
			return false;
		}
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.subscription_list);

		setTitle(CarCastApplication.getAppTitle()+": subscription search results");

		ListView listView = (ListView) findViewById(R.id.siteList);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				add(position);
			}
		});
		registerForContextMenu(listView);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add("Subscribe");
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search_results_menu, menu);
		return true;
	}

	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (item.getItemId() == R.id.searchAgain) {
			finish();
			return true;
		}
		add(((AdapterContextMenuInfo) item.getMenuInfo()).position);
		return true;
	}

	protected void showResults() {

		try {
			ListView listView = (ListView) findViewById(R.id.siteList);

			List<Subscription> sites = getResults();

			Toast.makeText(getApplicationContext(),
					"Found " + sites.size() + " results", Toast.LENGTH_LONG)
					.show();

			List<Map<String, String>> list = new ArrayList<Map<String, String>>();

			for (Subscription sub: sites) {
				Map<String, String> item = new HashMap<String, String>();
				item.put("name", sub.name);
				item.put("url", sub.url);
				list.add(item);

			}
			SimpleAdapter notes = new SimpleAdapter(this, list,
					R.layout.main_item_two_line_row, new String[] { "name",
							"url" }, new int[] { R.id.text1, R.id.text2 });
			listView.setAdapter(notes);
		} catch (Throwable t) {
			TraceUtil.report(new RuntimeException("lastResults="+lastResults,t));
			Toast.makeText(getApplicationContext(),
					"Sorry, problem with search results.",
					Toast.LENGTH_LONG).show();
		}
	}

}
