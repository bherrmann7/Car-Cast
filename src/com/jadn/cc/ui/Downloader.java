package com.jadn.cc.ui; import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.jadn.cc.R;
import com.jadn.cc.core.Config;
import com.jadn.cc.core.Sayer;

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
	
	@Override
	void onContentService() throws RemoteException {
//		try {
//			contentService.startDownloadingNewPodCasts(Config.getMax(this));
//		} catch (RemoteException re) {
//			esay(re);
//		}		
	}
	
	PowerManager.WakeLock wl; 


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download);

		tv = (TextView) findViewById(R.id.textconsole);
		
		// If you are running the debug screen, then do not go to sleep 
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "My Tag");
		wl.acquire();			
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
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "carcast-devs@googlegroups.com" });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Issue on download...");
		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, tv.getText());
		startActivity(Intent.createChooser(emailIntent, "Email about podcast downloading"));

		return true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();

		// stop display thread
		updater.allDone();
		
		wl.release();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updater = new Updater(handler, this);
	}

	// Called once a second in the UI thread to update the screen.
	public void run() {
		try {
			String text = contentService.getDownloadProgress();
			if(text.length()!=0)
				tv.setText(text);
			else {
				tv.setText("\n\n\nNo download has run or is running.");
			}
		} catch (Exception e) {
			esay(e);
		}
	}

	public void say(String text) {
		Message message = Message.obtain();
		Bundle bundle = new Bundle();
		bundle.putCharSequence("text", text + "\n");
		message.setData(bundle);
		handler.sendMessage(message);
	}

}
