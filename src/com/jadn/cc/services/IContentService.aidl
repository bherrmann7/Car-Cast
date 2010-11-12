
package com.jadn.cc.services;

import com.jadn.cc.core.Subscription;

interface IContentService {
    // player status:

	void moveTo(in double d);
	boolean pauseOrPlay();
	void bump(in int bump);
	void next();
	void previous();
	int getCount();
	void pause();
	void purgeAll();
	void purgeToCurrent();	
	void play(int position);
	String getCurrentTitle();	
	String getCurrentSubscriptionName();	
	String getMediaMode();
	String getLocationString();
	String getWhereString();
	String getDurationString();
	int currentProgress();
    boolean isPlaying();
    void setCurrentPaused(int position);
    
    // ??
    String startSearch(String search);

    // Podcasts:
	void deleteCurrentPodcast();
	void deletePodcast(int position);
    String getPodcastEmailSummary();

    // subscription management:
	void deleteSubscription(in Subscription toDelete);
	void deleteAllSubscriptions();
	void resetToDemoSubscriptions();
    List<Subscription> getSubscriptions();
    boolean editSubscription(in Subscription original, in Subscription modified);
    boolean addSubscription(in Subscription toAdd);
	void toggleSubscription(in Subscription toToggle);
	
	// download:
	void startDownloadingNewPodCasts(int max);
	String getDownloadProgress();
	String encodedDownloadStatus();
}
