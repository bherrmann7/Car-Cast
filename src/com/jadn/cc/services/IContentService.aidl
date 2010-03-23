
package com.jadn.cc.services;

interface IContentService {
	//String[] getPlaySet();
	void moveTo(in double d);
	boolean pauseOrPlay();
	void bump(in int bump);
	void next();
	void previous();
	int getCount();
	String getCurrentPlaySetName();
	void pause();
	void purgeAll();
	void purgeToCurrent();	
	void eraseHistory();	
	void play(int position);
	String getCurrentTitle();	
	String getCurrentSubscriptionName();	
	String getMediaMode();
	String getLocationString();
	String getWhereString();
	String getDurationString();
	int currentProgress();
	void deleteSite(int position);
	void deleteCurrentPodcast();
	void deleteAllSubscriptions();
	void deletePodcast(int position);
	void resetToDemoSubscriptions();
	
	void startDownloadingNewPodCasts(int max);
	String getDownloadProgress();
	String[] getSites();
	void saveSites(in String[] sites);	
	
	String getPodcastEmailSummary();
	boolean isPlaying();
	void setCurrentPaused(int position);
	
	String startSearch(String search);
	
	String encodedDownloadStatus();
	boolean addSubscription(String subscription);
}
