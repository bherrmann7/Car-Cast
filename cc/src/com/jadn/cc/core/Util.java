package com.jadn.cc.core;

import java.io.PushbackInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.jadn.cc.services.EnclosureHandler;

public class Util {

	/*
	 * public static String getShortURL(String url) { String shortName = url.substring(url.lastIndexOf('/') + 1); if
	 * (shortName.indexOf('?') != -1) return shortName.substring(0, shortName.indexOf('?')); return shortName; }
	 */

	// shared with SubscriptionEdit
	public static void findAvailablePodcasts(String url, EnclosureHandler encloseureHandler) throws Exception {
		Log.i("CarCast", "Processing URL: " + url);
		SAXParser sp = saxParserFactory.newSAXParser();
		URLConnection connection = new URL(url).openConnection();
		connection.setRequestProperty("User-Agent", "http://jadn.com/carcast");
		connection.setConnectTimeout(30 * 1000);
		connection.setReadTimeout(20 * 1000);
		String charset = getCharset(connection.getContentType());

		// We want to get the encoding of the xml document and take a peek so we can properly decode the entire stream
		// especially important for non-UTF8 feeds
		PushbackInputStream pis = new PushbackInputStream(connection.getInputStream(), 1024);
		StringBuilder xmlHeader = new StringBuilder();
		byte[] bytes = new byte[1023];
		int i = 0;
		for (; i < bytes.length; i++) {
			int b = pis.read();
			bytes[i] = (byte) b;
			xmlHeader.append((char) b);
			if (b == '>') {
				break;
			}
		}
		pis.unread(bytes, 0, i + 1);
		Log.i("CarCast/Util", "xml start:" + xmlHeader);
		if (xmlHeader.toString().toLowerCase().indexOf("windows-1252") != -1) {
			charset = "ISO-8859-1";
		}
		if (xmlHeader.toString().toLowerCase().indexOf("iso-8859-1") != -1) {
			charset = "ISO-8859-1";
		}

		InputSource is = new InputSource(pis);
		Log.i("CarCast/Util", "parsing with encoding: " + charset);
		is.setEncoding(charset);

		sp.parse(is, encloseureHandler);
	}

	/*
	 * public static String getShortURL(String url) { String shortName = url.substring(url.lastIndexOf('/') + 1); if
	 * (shortName.indexOf('?') != -1) return shortName.substring(0, shortName.indexOf('?')); return shortName; }
	 */

	public static boolean isValidURL(String url) {
		try {
			new URL(url);
			return true;

		} catch (MalformedURLException ex) {
			return false;
		}
	}

	public static void toast(Activity activity, String string) {
		Toast.makeText(activity.getApplicationContext(), string, Toast.LENGTH_LONG).show();
	}

	private static SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

	private static HttpURLConnection connectToHttpURL(String url, int followRedirects) throws Exception, MalformedURLException {
		HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
		connection.setRequestProperty("User-Agent", "http://jadn.com/carcast");
		connection.setConnectTimeout(30 * 1000);
		connection.setReadTimeout(20 * 1000);
		// Android seems to handle redirects improperly:
		// the InputStream is of the redirect itself, not the redirected page
		connection.setInstanceFollowRedirects(false);

		// TODO: is there a better way to properly follow redirects?
		String redirectLocation = connection.getHeaderField("Location");
		if (redirectLocation == null || "".equals(redirectLocation)) {
			return connection;
		}

		if (followRedirects == 0) {
			throw new Exception("Maximum HTTP redirects reached");
		}

		Log.i("CarCast/Util", "following redirect: " + redirectLocation);
		return connectToHttpURL(redirectLocation, followRedirects - 1);
	}

	public static final int MAX_REDIRECTS = 10;

	// shared with SubscriptionEdit
	public static void downloadPodcast(String url, EnclosureHandler encloseureHandler) throws Exception {
		final int BUFFSIZE = 1024;

		Log.i("CarCast", "Processing URL: " + url);
		URLConnection connection = connectToHttpURL(url, MAX_REDIRECTS);
		String charset = getCharset(connection.getContentType());
		SAXParser sp = saxParserFactory.newSAXParser();

		// we want to get the encoding
		PushbackInputStream pis = new PushbackInputStream(connection.getInputStream(), BUFFSIZE);
		StringBuilder xmlHeader = new StringBuilder();
		byte[] bytes = new byte[BUFFSIZE];
		int i = 0;
		for (; i < bytes.length; i++) {
			int b = pis.read();
			if (b == -1) // very short or empty response body
				break;
			bytes[i] = (byte) b;
			xmlHeader.append((char) b);
			if (b == '>') {
				break;
			}
		}
		// above loop will leave i at bytes.length after the last (un-break'd) iteration!
		pis.unread(bytes, 0, Math.min(i + 1, BUFFSIZE));
		Log.i("CarCast/Util", "xml start:" + xmlHeader);
		if (xmlHeader.toString().toLowerCase().indexOf("windows-1252") != -1) {
			charset = "ISO-8859-1";
		}
		if (xmlHeader.toString().toLowerCase().indexOf("iso-8859-1") != -1) {
			charset = "ISO-8859-1";
		}

		InputSource is = new InputSource(pis);
		Log.i("CarCast/Util", "parsing with encoding: " + charset);
		is.setEncoding(charset);

		sp.parse(is, encloseureHandler);
	}

	private static final String CHARSET = "charset=";

	public static String getCharset(String contentType) {
		int dex = -1;
		if (contentType != null && (dex = contentType.indexOf(CHARSET)) != -1) {
			String charset = contentType.substring(dex + CHARSET.length());
			if (charset.length()!=0)
				return charset;
		}
		return "UTF-8";
	}

}
