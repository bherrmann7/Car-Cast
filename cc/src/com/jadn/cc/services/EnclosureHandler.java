package com.jadn.cc.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import com.jadn.cc.core.Sayer;

public class EnclosureHandler extends DefaultHandler {

	private static final int STOP = -2;
	private static final int UNLIMITED = -1;
	
	String feedName;
	private boolean grabTitle;
	DownloadHistory history;
	private String lastTitle = "";

	public int max;

	public List<MetaNet> metaNets = new ArrayList<MetaNet>();

	private boolean needTitle = true;
	Sayer sayer;
	private boolean startTitle;
	public String title = "";

	public EnclosureHandler(int max, DownloadHistory history, Sayer sayer) {
		this.max = max;
		this.history = history;
		this.sayer = sayer;
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (needTitle && startTitle) {
			title += new String(ch, start, length);
			//Log.i("carcast.feedTitle", lastTitle);
		}

		if (grabTitle) {
			lastTitle += new String(ch, start, length);
			//Log.i("carcast.itemTitle", lastTitle);
		}
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		super.endElement(uri, localName, name);

		if(needTitle && startTitle){
			//Log.i("title", title);
			needTitle = false;
		}
		grabTitle = false;
	}

	public String getTitle() {
		return title;
	}

	private boolean isAudio(String url) {
		// for http://feeds.feedburner.com/dailyaudiobible
		// which always has the same intro at the top.		
		if (url.endsWith("/Intro_to_DAB.mp3")){
			return false;
		}
		if (url.toLowerCase().endsWith(".mp3"))
			return true;
		if (url.toLowerCase().endsWith(".m4a"))
			return true;
		if (url.indexOf(".mp3?") != -1)
			return true;
		return false;
	}

	boolean isVideo(String url) {
		if (url.toLowerCase().endsWith(".mp4"))
			return true;
		if (url.indexOf(".mp4?") != -1)
			return true;
		return false;
	}

	public void setFeedName(String feedName) {
		this.feedName = feedName;
	}

	@Override
	public void startElement(String namespaceURI, String localName,
			String qName, Attributes atts) throws SAXException {

		// This grabs the first title and uses it as the feed title
		if (needTitle && localName.equals("title")) {
			startTitle = true;
		}

		if (localName.equals("title")) {
			grabTitle = true;
		} else if (localName.equals("item")) {
			lastTitle = "";
		}

		if (localName.equals("enclosure") && atts.getValue("url") != null) {
			if (!isAudio(atts.getValue("url")) && !isVideo(atts.getValue("url")) ) {
				//Log.i("content", "url doesn't end right type... "
				//		+ atts.getValue("url"));
				return;
			}
			//Log.i("content", localName + " " + atts.getValue("url"));
			try {
				if (max != STOP && ( max == UNLIMITED || max > 0))  {
					if(max>0)
						max--;
					if (feedName == null) {
						if (title != null) {
							feedName = title;
						} else {
							feedName = "No Title";
						}
					}
					int length=0;
					if (atts.getValue("length")!=null  && atts.getValue("length").length() != 0)
					{
						try {
							length = Integer.parseInt(atts.getValue("length").trim());
						} catch (NumberFormatException nfe){
							// some feeds have bad lengths
						}
					}
					MetaNet metaNet = new MetaNet(feedName, new URL(atts
							.getValue("url")),length);
					metaNet.setTitle(lastTitle);
					if (history.contains(metaNet)) {
						// stop getting podcasts after we find one in our
						// history.
						max = STOP;
					} else {
						metaNets.add(metaNet);
					}
				}
			} catch (MalformedURLException e) {
				Log.e("CarCast", this.getClass().getSimpleName(),e);
			}
		}
	}
}
