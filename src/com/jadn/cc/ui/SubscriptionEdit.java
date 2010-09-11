package com.jadn.cc.ui; import java.net.URL;

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
import com.jadn.cc.core.ExternalMediaStatus;
import com.jadn.cc.core.Sayer;
import com.jadn.cc.core.Subscription;
import com.jadn.cc.core.Util;
import com.jadn.cc.services.DownloadHistory;
import com.jadn.cc.services.EnclosureHandler;

public class SubscriptionEdit extends BaseActivity {

	Subscription currentSub;

	@Override
	void onContentService() throws RemoteException {
	    if (currentSub != null) {
	        ((TextView) findViewById(R.id.editsite_name)).setText(currentSub.name);
	        ((TextView) findViewById(R.id.editsite_url)).setText(currentSub.url);
	        // TODO: add max count, ordering here
        } // endif
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editsite);

		currentSub = null;
		
		if (getIntent().getExtras() != null) {
            currentSub = (Subscription) getIntent().getExtras().get("subscription");
		}

		((Button) findViewById(R.id.saveEditSite))
				.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						String name = ((TextView) findViewById(R.id.editsite_name))
								.getText().toString();
						String url = ((TextView) findViewById(R.id.editsite_url))
								.getText().toString();
				        // TODO: add max count, ordering here
						
						// try out the url:
						if (!Util.isValidURL(url)) {
                            Util.toast(SubscriptionEdit.this, "URL to site is malformed."); 
                            return;
                        } // endif

						ExternalMediaStatus status = ExternalMediaStatus.getExternalMediaStatus();
						if (status != ExternalMediaStatus.writeable) {
							// unable to access sdcard
							Toast.makeText(getApplicationContext(),"Unable to add subscription to sdcard", Toast.LENGTH_LONG);
							return;
						}

						try {
						    Subscription newSub = new Subscription(name, url); // TODO add max count, ordering
						    if (currentSub != null) {
						        // edit:
                                contentService.editSubscription(currentSub, newSub);

						    } else {
						        // add:
                                contentService.addSubscription(newSub);
                            } // endif

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
						DownloadHistory history = DownloadHistory.getInstance();
						EnclosureHandler encloseureHandler = new EnclosureHandler(
								Config.getMax(SubscriptionEdit.this), history,
								new Sayer() {
									@Override
									public void say(String text) {
										Util.toast(SubscriptionEdit.this, text);
									}
								});
						try {
							SAXParser sp = spf.newSAXParser();
							XMLReader xr = sp.getXMLReader();
							xr.setContentHandler(encloseureHandler);
							xr
									.parse(new InputSource(new URL(url)
											.openStream()));

							Util.toast(SubscriptionEdit.this, "Feed is OK.  Would download "
									+ encloseureHandler.metaNets.size() + " podcasts.");
						} catch (Exception e) {		
							Log.e("editSite", "testURL", e);
							Util.toast(SubscriptionEdit.this, "Problem accessing feed. "+e.toString());
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

}
