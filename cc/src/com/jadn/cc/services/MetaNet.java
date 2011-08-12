package com.jadn.cc.services;

import java.net.URL;
import java.util.Properties;

import com.jadn.cc.core.Util;

/** Meta information about a podcast.  Loosely defined so things can be added later (like time) */
public class MetaNet {

	 Properties properties = new Properties();

	public MetaNet(String feedName, URL url, int size, String mimetype) {	
		properties.setProperty("feedName", feedName);
		properties.setProperty("url", url.toString());
		properties.setProperty("size", Integer.toString(size));
		properties.setProperty("mimetype", mimetype);
	}

	public int getSize() {
		if( properties.getProperty("size") != null ){
			return Integer.parseInt(properties.getProperty("size"));
		}
		return 0;

	}

	public String getSubscription() {
		return properties.getProperty("feedName");
	}

	public String getTitle() {
		return properties.getProperty("title");
	}

	public String getUrl() {
		return properties.getProperty("url");
	}


	public void setTitle(String value) {
		properties.setProperty("title", value);		
	}

	public void setMimetype(String value) {
		properties.setProperty("mimetype", value);
	}

	public String getMimetype() {
		String value = properties.getProperty("mimetype");
		if( value == null || "".equals(value) ) {
			// backwards compatibility to versions not yet having this property
			return ".mp3";
		}
		return value;
	}
}
