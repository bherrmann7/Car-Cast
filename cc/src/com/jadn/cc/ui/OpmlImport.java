package com.jadn.cc.ui;

import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;

import android.net.Uri;
import android.os.Bundle;
import android.util.Xml;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.jadn.cc.R;
import com.jadn.cc.core.Subscription;

public class OpmlImport extends BaseActivity {

	boolean replaceAllonImport = true;
	Uri feedFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.oiml_import);

		final Button replaceAll = (Button) findViewById(R.id.replaceAll);
		final Button addToButton = (Button) findViewById(R.id.addToSubscription);
		replaceAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				replaceAll.setEnabled(false);
//				addToButton.setEnabled(false);
				importOpml();
			}
		});
		addToButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				replaceAll.setEnabled(false);
//				addToButton.setEnabled(false);
				replaceAllonImport = false;
				importOpml();
			}
		});

		feedFile = getIntent().getData();

	}

	void importOpml() {
		if(replaceAllonImport){
			contentService.deleteAllSubscriptions();
		}
		InputStream in = null;
		try {
			in = getContentResolver().openInputStream(feedFile);
			XmlPullParser parser = Xml.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);

			int eventType;
			int count = 0;
			while ((eventType = parser.next()) != XmlPullParser.END_DOCUMENT) {	
				if(eventType == XmlPullParser.START_TAG) {
					if(parser.getName().equals("outline")){
						String title = parser.getAttributeValue(null, "title");
						String xmlUrl = parser.getAttributeValue(null, "xmlUrl");
						//Log.i("com.jadn.cc.oiml", "title="+title+"  xmlUrl="+xmlUrl);
						if(contentService.addSubscription(new Subscription(title, xmlUrl)))
							count++;
					}
					
				}				
			}
			Toast.makeText(getApplicationContext(), "Imported "+count+" subscriptions", Toast.LENGTH_LONG).show();
			finish();
		} catch (Throwable t) {
            Toast.makeText(getApplicationContext(), "Yikes "+t.getMessage(), Toast.LENGTH_LONG).show();
			//t.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}

}
