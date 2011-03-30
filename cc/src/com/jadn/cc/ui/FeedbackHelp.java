package com.jadn.cc.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.jadn.cc.R;
import com.jadn.cc.core.CarCastApplication;

public class FeedbackHelp extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.feedback_help);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public void quickTour(View view) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse("http://jadn.com/cc/walk/"));
		startActivity(i);
	}

	public void qanda(View view) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse("http://jadn.com/cc/QandA/"));
		startActivity(i);
	}

	public void email(View view) {
		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		emailIntent.setType("plain/text");
		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "carcast-devs@googlegroups.com" });
		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, CarCastApplication.getAppTitle()+": feedback "+CarCastApplication.getVersion());
		startActivity(Intent.createChooser(emailIntent, "Email about podcast"));
	}

	public void ccwebsite(View view) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse("http://jadn.com/cc/"));
		startActivity(i);

	}

}
