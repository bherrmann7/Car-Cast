package com.jadn.cc.services;

import java.io.Serializable;

@SuppressWarnings("serial")
public class HistoryEntry implements Serializable {

	String subscription;
	String podcastURL;

	public HistoryEntry(String subscription, String podcastURL) {
		super();
		this.subscription = subscription;
		this.podcastURL = podcastURL;
	}
}
