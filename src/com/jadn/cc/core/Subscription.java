package com.jadn.cc.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Subscription {

	public String name;
	public URL url;

	public static List<Subscription> fromStrings(String[] sites) {
		List<Subscription> sitesList = new ArrayList<Subscription>();
		for (String siteString : sites) {
			int pipe = siteString.indexOf('=');
			Subscription site = new Subscription();
			site.name = siteString.substring(0, pipe);
			try {
				site.url = new URL(siteString.substring(pipe + 1));
				// dont add it if the url is bad
				sitesList.add(site);
			} catch (MalformedURLException e) {
			}
		}
		return sitesList;
	}

	public static String[] toStrings(List<Subscription> sites) {
		String[] ss = new String[sites.size()];
		for (int i = 0; i < ss.length; i++) {
			ss[i] = sites.get(i).toString();
		}
		return ss;
	}

	@Override
	public String toString() {
		return name + "=" + url;
	}

	public static Subscription fromString(String line) throws MalformedURLException {
		int eq = line.indexOf('=');
		if (eq != -1) {
			Subscription site = new Subscription();
			site.name = line.substring(0, eq);
			site.url = new URL(line.substring(eq + 1));
			return site;
		}
		return null;
	}

}
