package com.jadn.cc.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.jadn.cc.R;
import com.jadn.cc.core.CarCastApplication;
import com.jadn.cc.util.MailRecordings;
import com.jadn.cc.util.Recording;
import com.jadn.cc.util.RecordingSet;
import com.jadn.cc.util.Updater;

public class AudioRecorder extends BaseActivity {

	Updater updater;
	// Need handler for callbacks to the UI thread
	final Handler handler = new Handler();
    RecordingSet recordingSet;

	final Runnable mUpdateResults = new Runnable() {
		@Override
		public void run() {

			ListView listView = (ListView) findViewById(R.id.audioRecorderListing);
			if (listView.getCount() != recordingSet.getRecordings().size())
				showRecordings();
		}
	};

	@Override
	protected void onPause() {
		super.onPause();
		updater.allDone();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updater = new Updater(handler, mUpdateResults);
	}

	private Button fb(int id) {
		return (Button) findViewById(id);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		if (item.getTitle().equals("Play")) {
			Recording recording = recordingSet.getRecordings().get(info.position);
			recording.play();
		}
		if (item.getTitle().equals("Delete")) {
			Recording recording = recordingSet.getRecordings().get(info.position);
            recordingSet.delete(recording);
			showRecordings();
		}
		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.recorder);

        recordingSet = new RecordingSet(this);

        setTitle(CarCastApplication.getAppTitle() + ": Audio Note Recorder");

		setReadyToRecord(true);

		fb(R.id.audioRecorderRecordButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				recordingSet.record();
				// transition to record mode
				setReadyToRecord(false);
			}
		});

		fb(R.id.audioRecorderCancelButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                recordingSet.cancel();
				// transition back to ready
				setReadyToRecord(true);
			}
		});

		fb(R.id.audioRecorderSaveButton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                recordingSet.save();

				// transition back to ready
				setReadyToRecord(true);
				showRecordings();

				// only let wifi connected trigger this 4 now,
				// contentService.publishRecordings();
			}
		});

		ListView listView = (ListView) findViewById(R.id.audioRecorderListing);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				recordingSet.getRecordings().get(position).play();
			}
		});

		registerForContextMenu(listView);

		showRecordings();


		updater = new Updater(handler, mUpdateResults);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
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
			recordingSet.deleteAll();
			showRecordings();
		}
		if (item.getItemId() == R.id.sendAudioToEmail) {
			if(MailRecordings.isAudioSendingConfigured(contentService))
				contentService.publishRecordings(this);
			else
				Toast.makeText(this, "Audio Note emailing not configured.  See settings.", Toast.LENGTH_LONG).show();
		}
		return super.onMenuItemSelected(featureId, item);
	}

	private void setReadyToRecord(boolean ready) {
		if (ready) {
			fb(R.id.audioRecorderRecordButton).setVisibility(View.VISIBLE);
			fb(R.id.audioRecorderCancelButton).setVisibility(View.INVISIBLE);
			fb(R.id.audioRecorderSaveButton).setVisibility(View.INVISIBLE);
			((ProgressBar) findViewById(R.id.audioRecorderBusy)).setVisibility(View.INVISIBLE);
		} else {
			fb(R.id.audioRecorderRecordButton).setVisibility(View.INVISIBLE);
			fb(R.id.audioRecorderCancelButton).setVisibility(View.VISIBLE);
			fb(R.id.audioRecorderSaveButton).setVisibility(View.VISIBLE);
			((ProgressBar) findViewById(R.id.audioRecorderBusy)).setVisibility(View.VISIBLE);
		}

	}

	private void showRecordings() {
		ListView listView = (ListView) findViewById(R.id.audioRecorderListing);

		List<Recording> recordings = recordingSet.getRecordings();
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		for (Recording recording : recordings) {
			HashMap<String, String> item = new HashMap<String, String>();
			item.put("line1", "");
			item.put("line2", recording.getTimeString());
			item.put("amountHeard", recording.getDurationString());
			list.add(item);

		}
		/*
		 * ArrayAdapter<HashMap<String, String>> notes = new ArrayAdapter<HashMap<String, String>>(this,
		 * R.layout.recorded_items, list);
		 */
		SimpleAdapter notes = new SimpleAdapter(this, list, R.layout.recorded_items, new String[] { "line1", "line2", "amountHeard" },
				new int[] { R.id.firstLine, R.id.secondLine, R.id.amountHeard });

		listView.setAdapter(notes);

	}

}
