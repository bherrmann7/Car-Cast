package com.jadn.cc.ui; import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.widget.TextView;

import com.jadn.cc.R;
import com.jadn.cc.core.Config;
import com.jadn.cc.core.Sayer;

public class Downloader extends BaseActivity implements Sayer, Runnable {

	TextView tv;

	final Handler handler = new Handler() {
		@Override
        public void handleMessage(Message m) {
			tv.append(m.getData().getCharSequence("text"));
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.download);

		tv = (TextView) findViewById(R.id.textconsole);
	}

	// Called once a second in the UI thread to update the screen.
	public void run() {
		try {
			tv.setText(contentService.getDownloadProgress());
		} catch (Exception e) {
			esay(e);
		}
	}

	Updater updater;
	
	@Override
	protected void onResume() {
		super.onResume();
		updater = new Updater(handler, this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		// stop display thread
		updater.allDone();
	}

	public void say(String text) {
		Message message = Message.obtain();
		Bundle bundle = new Bundle();
		bundle.putCharSequence("text", text + "\n");
		message.setData(bundle);
		handler.sendMessage(message);
	}

	@Override
	void onContentService() throws RemoteException {
		try {
			contentService.startDownloadingNewPodCasts(Config.getMax(this));
		} catch (RemoteException re) {
			esay(re);
		}		
	}

}
