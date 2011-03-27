package com.jadn.cc.core;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import android.app.Activity;
import android.widget.Toast;

import com.jadn.cc.services.EnclosureHandler;
import org.xml.sax.InputSource;

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
		String charset = getCharset(connection.getContentType());
		InputSource is = new InputSource(connection.getInputStream());
		is.setEncoding(charset);
		sp.parse(is, encloseureHandler);
	}

	private static final String CHARSET = "charset=";
	
	public static String getCharset(String contentType) {
		int dex=-1;
		if(contentType!=null && (dex=contentType.indexOf(CHARSET)) !=-1 ){
			return contentType.substring(dex+CHARSET.length());
		}
		return "UTF-8";
	}

}
