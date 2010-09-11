package com.jadn.cc.services;

import java.net.URL;
import java.util.Properties;

import com.jadn.cc.core.Util;

/** Meta information about a podcast.  Loosely defined so things can be added later (like time) */
public class MetaNet {

	 Properties properties = new Properties();

	public MetaNet(String feedName, URL url, int size) {	
		properties.setProperty("feedName", feedName);
		properties.setProperty("url", url.toString());
		properties.setProperty("size", Integer.toString(size));
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

	String getUrl() {
		return properties.getProperty("url");
	}

	String getUrlShortName() {
		return Util.getShortURL(getUrl());
	}

	public void setTitle(String value) {
		properties.setProperty("title", value);		
	}
}
