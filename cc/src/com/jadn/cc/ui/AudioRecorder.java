package com.jadn.cc.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

import com.jadn.cc.R;
import com.jadn.cc.core.CarCastApplication;

public class AudioRecorder extends BaseActivity {

	private Button fb(int id) {
		return (Button)findViewById(id);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		if (item.getTitle().equals("Play")) {
			Recording recording = Recording.getRecordings().get(info.position);
			recording.play();
		}
		if (item.getTitle().equals("Delete")) {
			Recording recording = Recording.getRecordings().get(info.position);
			recording.delete(this);
			showRecordings();
		}
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recorder);

		setTitle(CarCastApplication.getAppTitle()+": Audio Note Recorder");

		setReadyToRecord(true);

		fb(R.id.audioRecorderRecordButton).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Recording.record();
				// transition to record mode
				setReadyToRecord(false);
			}});

		fb(R.id.audioRecorderCancelButton).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Recording.cancel();
				// transition back to ready
				setReadyToRecord(true);
			}});

		fb(R.id.audioRecorderSaveButton).setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Recording.save(AudioRecorder.this);

				// transition back to ready
				setReadyToRecord(true);
				showRecordings();
			}});

		ListView listView = (ListView) findViewById(R.id.audioRecorderListing);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				Recording.getRecordings().get(position).play();
			}
		});

		registerForContextMenu(listView);

		showRecordings();

	}


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add("Play");
		menu.add("Delete");
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.audio_recorder_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		if (item.getItemId() == R.id.deleteAll) {
				Recording.deleteAll();
				showRecordings();
			}
			if (item.getItemId() == R.id.playAll) {
			}
		return super.onMenuItemSelected(featureId, item);
	}


	private void setReadyToRecord(boolean ready) {
		if (ready) {
			fb(R.id.audioRecorderRecordButton).setVisibility(View.VISIBLE);
			fb(R.id.audioRecorderCancelButton).setVisibility(View.INVISIBLE);
			fb(R.id.audioRecorderSaveButton).setVisibility(View.INVISIBLE);
			((ProgressBar)findViewById(R.id.audioRecorderBusy)).setVisibility(View.INVISIBLE);
		} else {
			fb(R.id.audioRecorderRecordButton).setVisibility(View.INVISIBLE);
			fb(R.id.audioRecorderCancelButton).setVisibility(View.VISIBLE);
			fb(R.id.audioRecorderSaveButton).setVisibility(View.VISIBLE);
			((ProgressBar)findViewById(R.id.audioRecorderBusy)).setVisibility(View.VISIBLE);
		}


	}

	private void showRecordings() {
		ListView listView = (ListView) findViewById(R.id.audioRecorderListing);

		List<Recording> recordings = Recording.getRecordings();
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		for (Recording recording: recordings){
			HashMap<String, String> item = new HashMap<String, String>();
            item.put("line1", "");
			item.put("line2", recording.getTimeString());
			item.put("amountHeard", recording.getDurationString());
			list.add(item);

		}
		/*
		ArrayAdapter<HashMap<String, String>> notes = new ArrayAdapter<HashMap<String, String>>(this, R.layout.podcast_items, list);
*/
	SimpleAdapter notes = new SimpleAdapter(this,
				list,
				R.layout.podcast_items, new String[] { "line1", "line2", "amountHeard" },
				new int[] { R.id.firstLine, R.id.secondLine, R.id.amountHeard });

		listView.setAdapter(notes);

	}



}
