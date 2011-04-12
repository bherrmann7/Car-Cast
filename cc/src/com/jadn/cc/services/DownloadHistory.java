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

import com.jadn.cc.core.Config;
import com.jadn.cc.core.Sayer;
import com.jadn.cc.core.Util;

/**
 * The history of all downloaded episodes the data is backed into a file on the SD-card
 * 
 */
public class DownloadHistory implements Sayer {
	private static final String UNKNOWN_SUBSCRIPTION = "unknown";
	private static File historyFile = new File(Config.PodcastsRoot, "history.prop");
	private final static String HISTORY_TWO_HEADER = "history version 2";
	private static DownloadHistory instance = null;
	private List<HistoryEntry> historyEntries = new ArrayList<HistoryEntry>();
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
				historyEntries.add(new HistoryEntry(UNKNOWN_SUBSCRIPTION, line));
				while ((line = dis.readLine()) != null) {
					historyEntries.add(new HistoryEntry(UNKNOWN_SUBSCRIPTION, line));
				}
			} else {
				ObjectInputStream ois = new ObjectInputStream(dis);
				historyEntries =  (List<HistoryEntry>) ois.readObject();
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
		historyEntries.add(new HistoryEntry(metaNet.getSubscription(), metaNet.getUrl()));
		save();
	}

	/**
	 * Check if a item is in the history
	 * 
	 * @param metaNet the item to check for
	 * @return true it the item is in the history
	 */
	public boolean contains(MetaNet metaNet) {	
		for(HistoryEntry historyEntry: historyEntries){
			if(!historyEntry.subscription.equals(UNKNOWN_SUBSCRIPTION) &&
					!historyEntry.subscription.equals(metaNet.getSubscription())){
					continue;				
			}
			if(historyEntry.podcastURL.equals(metaNet.getUrl())){
				return true;
			}
		}
		return false;
	}

	/**
	 * Remove history of all downloaded podcasts
	 * 
	 * @return number of history items deleted
	 */
	public int eraseHistory() {
		int size = instance.historyEntries.size();
		instance.historyEntries = new ArrayList<HistoryEntry>();
		save();
		return size;
	}

	/**
	 * Remove history of all downloaded podcasts for the specified subscription
	 * 
	 * @return number of history items deleted
	 */
	public int eraseHistory(String subscription) {
		int size = instance.historyEntries.size();
		List<HistoryEntry> nh = new ArrayList<HistoryEntry>();
		for (HistoryEntry he : instance.historyEntries) {
			if (!he.subscription.equals(subscription))
				nh.add(he);
		}
		instance.historyEntries = nh;
		return size - nh.size();
	}


	private void save() {
		try {
			DataOutputStream dosDataOutputStream = new DataOutputStream(new FileOutputStream(historyFile));
			dosDataOutputStream.write(HISTORY_TWO_HEADER.getBytes());
			dosDataOutputStream.write('\n');
			ObjectOutputStream oos = new ObjectOutputStream(dosDataOutputStream);
			oos.writeObject(historyEntries);
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
		return historyEntries.size();
	}
}
