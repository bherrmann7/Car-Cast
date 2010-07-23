package com.jadn.cc.services;

import java.util.List;

import android.os.RemoteException;

import com.jadn.cc.core.Subscription;

public class ContentServiceStub extends IContentService.Stub {

	ContentService contentService;

	ContentServiceStub(ContentService contentService) {
		this.contentService = contentService;
	}

	@Override
	public String getCurrentTitle() throws RemoteException {
		return contentService.currentTitle();
	}

	@Override
	public void bump(int bump) throws RemoteException {
		contentService.bump(bump);
	}

	@Override
	public int currentProgress() throws RemoteException {
		return contentService.currentProgress();
	}

	@Override
	public void eraseHistory() throws RemoteException {
		contentService.eraseHistory();
	}

	@Override
	public int getCount() throws RemoteException {
		return contentService.getCount();
	}

	@Override
	public String getCurrentPlaySetName() throws RemoteException {
		return contentService.currentSet.toString();
	}

	@Override
	public String getDurationString() throws RemoteException {
		return contentService.getDurationString();
	}

	@Override
	public String getLocationString() throws RemoteException {
		return contentService.getLocationString();
	}

	@Override
	public String getMediaMode() throws RemoteException {
		return contentService.getMediaMode().toString();
	}

	// @Override
	// public String[] getPlaySet() throws RemoteException {
	// String[] files = new String[contentService.met().length];
	// int i = 0;
	// for (File file : contentService.getPlaySetCache()) {
	// files[i++] = file.toString();
	// }
	// return files;
	// }

	@Override
	public String getWhereString() throws RemoteException {
		return contentService.getWhereString();
	}

	@Override
	public void moveTo(double d) throws RemoteException {
		contentService.moveTo(d);
	}

	@Override
	public void next() throws RemoteException {
		contentService.next();
	}

	@Override
	public void pause() throws RemoteException {
		contentService.pauseNow();

	}

	@Override
	public boolean pauseOrPlay() throws RemoteException {
		return contentService.pauseOrPlay();
	}

	@Override
	public void previous() throws RemoteException {
		contentService.previous();
	}

	@Override
	public void purgeAll() throws RemoteException {
		contentService.deleteUpTo(-1);
	}

	@Override
	public void purgeToCurrent() throws RemoteException {
		contentService.deleteUpTo(contentService.current);
	}

	@Override
	public void deleteSubscription(Subscription toDelete) throws RemoteException {
	    contentService.deleteSubscription(toDelete);
	}

	@Override
	public void startDownloadingNewPodCasts(int max) throws RemoteException {
		contentService.startDownloadingNewPodCasts(max);
	}

	@Override
	public String getDownloadProgress() throws RemoteException {
		return contentService.downloadHelper.sb.toString();
	}

	@Override
	public String getPodcastEmailSummary() throws RemoteException {
		return contentService.getPodcastEmailSummary();
	}

	@Override
	public void deleteCurrentPodcast() throws RemoteException {
		contentService.deleteCurrentPodcast();
	}

	@Override
	public void deleteAllSubscriptions() throws RemoteException {
		contentService.deleteAllSubscriptions();

	}

	@Override
	public void resetToDemoSubscriptions() throws RemoteException {
		contentService.resetToDemoSubscriptions();
	}

	@Override
	public void deletePodcast(int position) throws RemoteException {
		contentService.deletePodcast(position);
	}

	@Override
	public void play(int position) throws RemoteException {
		contentService.play(position);
	}

	@Override
	public boolean isPlaying() throws RemoteException {
		return contentService.mediaPlayer.isPlaying();
	}

	@Override
	public void setCurrentPaused(int position) throws RemoteException {
		contentService.setCurrentPaused(position);
	}

	@Override
	public String getCurrentSubscriptionName() throws RemoteException {
		return contentService.getCurrentSubscriptionName();
	}

	@Override
	public String startSearch(String search) throws RemoteException {
		return contentService.startSearch(search);
	}

	@Override
	public String encodedDownloadStatus() throws RemoteException {
		return contentService.encodedDownloadStatus();
	}

	@Override
	public boolean addSubscription(Subscription toAdd) throws RemoteException {
	    return contentService.addSubscription(toAdd);
	}

	@Override
	public boolean editSubscription(Subscription original, Subscription modified) throws RemoteException {
	    return contentService.editSubscription(original, modified);
	}

    @Override
    public List<Subscription> getSubscriptions() throws RemoteException {
       List<Subscription> subscriptions = contentService.getSubscriptions();
       return subscriptions;
    }

	

}
