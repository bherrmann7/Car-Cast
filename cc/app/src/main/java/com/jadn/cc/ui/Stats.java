package com.jadn.cc.ui; import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.jadn.cc.R;
import com.jadn.cc.core.CarCastApplication;
import com.jadn.cc.core.Subscription;
import com.jadn.cc.services.DownloadHistory;
import com.jadn.cc.util.Updater;

import java.util.List;

public class Stats extends BaseActivity {


    protected void onContentService() {

        setTitle(CarCastApplication.getAppTitle() + ": Stats");

        StringBuilder sb = new StringBuilder();
        sb.append("History Size: ");

        DownloadHistory downloadHistory = new DownloadHistory(getApplicationContext());
        sb.append(downloadHistory.size());
        sb.append("\n");

        for(Subscription sub: getSubscriptions()){
            sb.append(sub.name);
            sb.append(": ");
            sb.append(sub.maxDownloads);
            sb.append("\n");
        }
        TextView textView = (TextView) findViewById(R.id.statsText);
        textView.setText(sb.toString());
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stats);


	}




}
