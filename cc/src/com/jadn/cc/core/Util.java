package com.jadn.cc.core;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.jadn.cc.services.EnclosureHandler;

import android.app.Activity;
import android.widget.Toast;

public class Util {

	public static String getShortURL(String url) {
		String shortName = url.substring(url.lastIndexOf('/') + 1);
		if (shortName.indexOf('?') != -1)
			return shortName.substring(0, shortName.indexOf('?'));
		return shortName;
	}

	public static boolean isValidURL(String url) {
		try {
			new URL(url);
			return true;

		} catch (MalformedURLException ex) {
			return false;
		}
	}

	public static void toast(Activity activity, String string) {
		Toast.makeText(activity.getApplicationContext(), string,
				Toast.LENGTH_LONG).show();
	}

	private static SAXParserFactory saxParserFactory = SAXParserFactory
			.newInstance();

	// shared with SubscriptionEdit
	public static void downloadPodcast(String url,
			EnclosureHandler encloseureHandler) throws Exception {
		SAXParser sp = saxParserFactory.newSAXParser();
		URLConnection connection = new URL(url).openConnection();
		connection.setRequestProperty("User-Agent", "http://jadn.com/carcast");
		InputStream iis = connection.getInputStream();
		sp.parse(iis, encloseureHandler);
	}

}
