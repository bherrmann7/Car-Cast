package com.jadn.cc.services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.jadn.cc.core.PlaySet;
import com.jadn.cc.core.Sayer;

/**
 * The history of all downloaded episodes the data is backed into a file on the SD-card
 * 
 */
public class DownloadHistory implements Sayer {
	private static File historyFile = new File(PlaySet.PODCASTS.getRoot(), "history.prop");
	private final static String HISTORY_TWO_HEADER = "history version 2";
	private static DownloadHistory instance = null;
	private List<HistoryEntry> history = new ArrayList<HistoryEntry>();
	StringBuilder sb = new StringBuilder();

	/**
	 * Get the history singleton
	 * 
	 * @return the history
	 */
	public static DownloadHistory getInstance() {
		if (instance == null) {
			instance = new DownloadHistory();
		}
		return instance;
	}

	/**
	 * Create a object that represents the download history. It is backed to a file.
	 */
	@SuppressWarnings("unchecked")
	private DownloadHistory() {
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(historyFile));
			String line = dis.readLine();
			if (!line.startsWith(HISTORY_TWO_HEADER)) {
				// load old format.
				history.add(new HistoryEntry("unknown source", line));
				while ((line = dis.readLine()) != null) {
					history.add(new HistoryEntry("unknown source", line));
				}
			} else {
				ObjectInputStream ois = new ObjectInputStream(dis);
				history =  (List<HistoryEntry>) ois.readObject();
				ois.close();
			}
		} catch (Throwable e) {
			// would be nice to ask the user if we can submit his history file
			// to the devs for review
			Log.e(DownloadHelper.class.getName(), "error reading history file " + historyFile.toString(), e);
		}
	}

	/**
	 * Add a item to the history
	 * 
	 * @param metaNet podcast metadata
	 */
	public void add(MetaNet metaNet) {
		history.add(new HistoryEntry(metaNet.getSubscription(), metaNet.getUrl()));
		save();
	}

	/**
	 * Check if a item is in the history
	 * 
	 * @param url the item to check for
	 * @return true it the item is in the history
	 */
	public boolean contains(MetaNet url) {
		if (history.contains(url.getUrlShortName())) {
			this.add(url);
			this.remove(url.getUrlShortName());
		}
		return history.contains(url.getUrl());
	}

	/**
	 * Remove history of all downloaded podcasts
	 * 
	 * @return number of history items deleted
	 */
	public int eraseHistory() {
		int size = instance.history.size();
		instance.history = new ArrayList<HistoryEntry>();
		save();
		return size;
	}

	/**
	 * Remove history of all downloaded podcasts for the specified subscription
	 * 
	 * @return number of history items deleted
	 */
	public int eraseHistory(String subscription) {
		int size = instance.history.size();
		List<HistoryEntry> nh = new ArrayList<HistoryEntry>();
		for (HistoryEntry he : instance.history) {
			if (!he.subscription.equals(subscription))
				nh.add(he);
		}
		instance.history = nh;
		return size - nh.size();
	}

	/**
	 * Removes a string from the history
	 * 
	 * @param s
	 */
	private void remove(String s) {
		history.remove(s);
		save();
	}

	private void save() {
		try {
			DataOutputStream dosDataOutputStream = new DataOutputStream(new FileOutputStream(historyFile));
			dosDataOutputStream.write(HISTORY_TWO_HEADER.getBytes());
			dosDataOutputStream.write('\n');
			ObjectOutputStream oos = new ObjectOutputStream(dosDataOutputStream);
			oos.writeObject(history);
			oos.close();
		} catch (IOException e) {
			say("problem writing history file: " + historyFile + " ex:" + e);
		}
	}

	@Override
	public void say(String text) {
		sb.append(text);
		sb.append('\n');
	}

	/**
	 * Get the current size of the download history
	 * 
	 * @return the size
	 */
	public int size() {
		return history.size();
	}
}
