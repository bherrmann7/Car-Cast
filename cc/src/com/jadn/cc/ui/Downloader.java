package com.jadn.cc.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.jadn.cc.R;
import com.jadn.cc.core.CarCastApplication;
import com.jadn.cc.core.Sayer;
import com.jadn.cc.trace.TraceData;
import com.jadn.cc.util.Updater;

/**
 * Lets the user observe download details in all their command line glory.
 *
 * @author bob
 *
 */
public class Downloader extends BaseActivity implements Sayer, Runnable {

	final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message m) {
			tv.append(m.getData().getCharSequence("text"));
		}
	};

	TextView tv;
	Updater updater;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download);

		tv = (TextView) findViewById(R.id.textconsole);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.downloads_menu, menu);
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
				new String[] { "carcast-devs@googlegroups.com", "bob@jadn.com" });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
				"Issue on download...");

		StringBuilder sb = new StringBuilder();
		sb.append("Thanks. Please describe the problem. \n\n=== Basic Info ===\n");
		nv(sb, "package_name", TraceData.APP_PACKAGE);
		nv(sb, "package_version", TraceData.APP_VERSION);
		nv(sb, "phone_model", TraceData.PHONE_MODEL);
		nv(sb, "android_version", TraceData.ANDROID_VERSION);
		sb.append("\nCar Cast Download output\n====================\n");
		sb.append(tv.getText());
		sb.append("\nLog snapshot\n============\n");
		fetchLog(sb);

		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, sb.toString());

		startActivity(Intent.createChooser(emailIntent,
				"Email about podcast downloading"));

		return true;
	}

	private void nv(StringBuilder sb, String name, String value) {
		int start = sb.length();
		sb.append(name);
		while (sb.length()<start+15)
			sb.append(' ');
		sb.append(':');
		sb.append(value);
		sb.append('\n');
	}

	@Override
	protected void onPause() {
		super.onPause();

		// stop display thread
		updater.allDone();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updater = new Updater(handler, this);
	}

	// Called once a second in the UI thread to update the screen.
	@Override public void run() {
		try {
			String text = contentService.getDownloadProgress();
			if (text.length() != 0)
				tv.setText(text);
			else {
				tv.setText("\n\n\nNo download has run or is running.");
			}
		} catch (Exception e) {
			CarCastApplication.esay(e);
		}
	}

	@Override public void say(String text) {
		Message message = Message.obtain();
		Bundle bundle = new Bundle();
		bundle.putCharSequence("text", text + "\n");
		message.setData(bundle);
		handler.sendMessage(message);
	}

	void fetchLog(StringBuilder log) {
		BufferedReader reader = null;
		try {
			Process process = Runtime.getRuntime().exec(
					new String[] { "logcat", "-v", "time", "-d" });
			reader = new BufferedReader(new InputStreamReader(process
					.getInputStream()));
			String line;
			LinkedList<String> lines = new LinkedList<String>();
			while ((line = reader.readLine()) != null) {
				// life's too short to ever look at these again.
				if(line.startsWith("D/dalvikvm(")&& line.indexOf("GC freed")!=-1)
					continue;
				lines.add(line);
				if(lines.size()>1000){
					lines.remove(0);
				}
			}
			for (String aline : lines) {
				log.append(aline);
				log.append('\n');
			}
			System.out.println("Captured "+lines+" lines.");
		} catch (IOException e) {
			log.append("Problem running logcat: " + e.getMessage());
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
				}
		}
	}
}
