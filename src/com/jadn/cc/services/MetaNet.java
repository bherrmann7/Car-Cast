package com.jadn.cc.services;

import java.net.URL;
import java.util.Properties;

public class MetaNet {

	 Properties properties = new Properties();

	public MetaNet(String feedName, URL url, int size) {	
		properties.setProperty("feedName", feedName);
		properties.setProperty("url", url.toString());
		properties.setProperty("size", Integer.toString(size));
	}

	String getUrl() {
		return properties.getProperty("url");
	}

	String getUrlShortName() {
		String url = properties.getProperty("url");
		String shortName = url.substring(url.lastIndexOf('/') + 1);
		if (shortName.indexOf('?') != -1)
			return shortName.substring(0, shortName.indexOf('?'));
		return shortName;
	}

	public void setTitle(String value) {
		properties.setProperty("title", value);		
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
}
