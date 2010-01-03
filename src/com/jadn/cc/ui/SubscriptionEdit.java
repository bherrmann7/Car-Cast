package com.jadn.cc.ui; import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jadn.cc.R;
import com.jadn.cc.core.Config;
import com.jadn.cc.core.Sayer;
import com.jadn.cc.core.Subscription;
import com.jadn.cc.core.Util;
import com.jadn.cc.services.DownloadHelper;
import com.jadn.cc.services.EnclosureHandler;

public class SubscriptionEdit extends BaseActivity {

	int position;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editsite);

		position = -1;
		if (getIntent().getExtras() != null) {
			position = getIntent().getExtras().getInt("site");
		}

		((Button) findViewById(R.id.saveEditSite))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						String name = ((TextView) findViewById(R.id.editsite_name))
								.getText().toString();
						String url = ((TextView) findViewById(R.id.editsite_url))
								.getText().toString();
						
						Subscription site = new Subscription();
						site.name = name;
						try {
							site.url = new URL(url);
						} catch (MalformedURLException e1) {
							Util.say(SubscriptionEdit.this, "URL to site is malformed."); 
							return;
						}						
						List<Subscription> sites = getSubscriptions();
						if (sites == null){
							// unable to access sdcard
							Toast.makeText(getApplicationContext(),"Unable to access sdcard", Toast.LENGTH_LONG);
							return;
						}
						if(position == -1){
							sites.add(site);
						} else {
							sites.set(position, site);
						}
						try {
							contentService.saveSites(Subscription.toStrings(sites));
						} catch (RemoteException e) {
							esay(e);
						}
											
						SubscriptionEdit.this.setResult(RESULT_OK);
						SubscriptionEdit.this.finish();
					}

				});

		((Button) findViewById(R.id.testEditSite))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {

						// String name = ((TextView)
						// findViewById(R.id.editsite_name))
						// .getText().toString();
						String url = ((TextView) findViewById(R.id.editsite_url))
								.getText().toString();

						SAXParserFactory spf = SAXParserFactory.newInstance();
						List<String> history = DownloadHelper.getHistory();
						EnclosureHandler encloseureHandler = new EnclosureHandler(
								Config.getMax(SubscriptionEdit.this), history,
								new Sayer() {
									@Override
									public void say(String text) {
										Util.say(SubscriptionEdit.this, text);
									}
								});
						try {
							SAXParser sp = spf.newSAXParser();
							XMLReader xr = sp.getXMLReader();
							xr.setContentHandler(encloseureHandler);
							xr
									.parse(new InputSource(new URL(url)
											.openStream()));

							Util.say(SubscriptionEdit.this, "Feed is OK.  Would download "
									+ encloseureHandler.metaNets.size() + " podcasts.");
						} catch (Exception e) {		
							Log.e("editSite", "testURL", e);
							Util.say(SubscriptionEdit.this, "Problem accessing feed. "+e.toString());
						}

						TextView nameTV = ((TextView) findViewById(R.id.editsite_name));
						// .getText().toString();

						if (encloseureHandler.title.length() != 0
								&& nameTV.getText().length() == 0) {
							nameTV.setText(encloseureHandler.getTitle());
						}
					}

				});

//		((Button) findViewById(R.id.cancelEditSite))
//				.setOnClickListener(new OnClickListener() {
//
//					@Override
//					public void onClick(View v) {
//						setResult(RESULT_CANCELED);
//						finish();
//					}
//
//				});
	}

	@Override
	void onContentService() throws RemoteException {
		if (position != -1) {
			List<Subscription> subs = getSubscriptions();
			if(subs==null){
				// can't access sdcard BOBH
				Toast.makeText(getApplicationContext(),"Unable to access sdcard", Toast.LENGTH_LONG);
				return;
			}
			Subscription site = subs.get(position);			

			((TextView) findViewById(R.id.editsite_name)).setText(site.name);
			((TextView) findViewById(R.id.editsite_url)).setText(site.url
					.toString());
		}
	}

}
