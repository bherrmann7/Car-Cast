package com.jadn.cc.ui;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.jadn.cc.R;
import com.jadn.cc.services.ContentService;
import com.jadn.cc.services.MetaFile;
import com.jadn.cc.services.MetaHolder;

public class PodcastList extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.podcast_list);

		setTitle("Car Cast: Downloaded podcasts");

		ListView listView = (ListView) findViewById(R.id.podcastList);
		registerForContextMenu(listView);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				MetaHolder metaHolder = new MetaHolder();
				try {
					MetaFile mfile = metaHolder.get(position);

					if (mfile.getTitle().equals(contentService.getCurrentTitle())) {
						contentService.pauseOrPlay();
					} else {
						// This saves our position
						if(contentService.isPlaying())
							contentService.pause();
						contentService.play(position);
					}
					showPodcasts();
				} catch (RemoteException e) {
					// humm.
				}
			}
		});
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add("Play");
		menu.add("Delete");
		menu.add("Delete All Before");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		if (item.getTitle().equals("Delete")) {
			try {
				contentService.deletePodcast(info.position);
			} catch (RemoteException e) {
				// humm.
			}
			showPodcasts();
			return false;
		}
		if (item.getTitle().equals("Delete All Before")) {
			try {
				contentService.setCurrentPaused(info.position);
				contentService.purgeToCurrent();
			} catch (RemoteException e) {
				// humm.
			}
			showPodcasts();
			return false;
		}
		if (item.getTitle().equals("Play")) {
			try {
				contentService.play(info.position);
			} catch (RemoteException e) {
				// humm.
			}
			finish();
			return false;
		}
		return true;
	}

	protected void showPodcasts() {

		ListView listView = (ListView) findViewById(R.id.podcastList);

		MetaHolder metaHolder = new MetaHolder();
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
		

		for (int i = 0; i < metaHolder.getSize(); i++) {
			MetaFile metaFile = metaHolder.get(i);
			HashMap<String, String> item = new HashMap<String, String>();
			try {
				if (contentService.getCurrentTitle().equals(metaFile.getTitle())) {
					if (contentService.isPlaying()) {
						item.put("line1", "> " + metaFile.getFeedName());
					} else {
						item.put("line1", "|| " + metaFile.getFeedName());
					}
				} else {
					item.put("line1", metaFile.getFeedName());
				}
			} catch (RemoteException e) {
				esay(e);
				item.put("line1", metaFile.getFeedName());
			}
			item.put("xx:xx-xx:xx", ContentService.getTimeString(metaFile.getCurrentPos()+5)+"-"+ContentService.getTimeString(metaFile.getDuration()));
			item.put("line2", metaFile.getTitle());
			list.add(item);

		}
		SimpleAdapter notes = new SimpleAdapter(this, list,
		// R.layout.main_item_two_line_row, new String[] { "line1",
				// "line2" }, new int[] { R.id.text1, R.id.text2 });
				R.layout.podcast_items, new String[] { "line1", "xx:xx-xx:xx","line2" }, new int[] { R.id.firstLine, R.id.amountHeard, R.id.secondLine });
		listView.setAdapter(notes);
		
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.podcast_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		try {
			if (item.getItemId() == R.id.deleteAllPodcasts) {
				contentService.purgeAll();
				showPodcasts();
				finish();
			}
			if (item.getItemId() == R.id.eraseDownloadHistory) {
				contentService.eraseHistory();
				return true;
			}
		} catch (RemoteException e) {
			esay(e);
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			showPodcasts();
		}
	}

	@Override
	void onContentService() throws RemoteException {
		showPodcasts();
	}

}