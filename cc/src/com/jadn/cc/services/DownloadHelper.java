package com.jadn.cc.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import android.util.Log;
import android.widget.TextView;

import com.jadn.cc.core.CarCastApplication;
import com.jadn.cc.core.Config;
import com.jadn.cc.core.OrderingPreference;
import com.jadn.cc.core.Sayer;
import com.jadn.cc.core.Subscription;
import com.jadn.cc.core.Util;

public class DownloadHelper implements Sayer {
    private String currentSubscription = " ";
    private String currentTitle = " ";
    private int globalMax;
    private int podcastsCurrentBytes;
    private int podcastsDownloaded;
    private int podcastsTotalBytes;
    private int sitesScanned;
    private int totalPodcasts;
	private int totalSites;
	private TextView tv;
	private boolean isRunning = true;
	StringBuilder sb = new StringBuilder("Getting ready to start downloads\n");

	public DownloadHelper(int globalMax) {
		this.globalMax = globalMax;
	}

    public boolean isRunning(){
        return isRunning;
    }

	SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd hh:mma");

	private String getLocalFileExtFromMimetype(String mimetype) {
		if ("audio/mp3".equals(mimetype)) {
			return ".mp3";
		}
		if ("audio/ogg".equals(mimetype)) {
			return ".ogg";
		}
		return ".bin";
	}

	protected void downloadNewPodCasts(ContentService contentService) {
        try {
            DownloadHistory history = new DownloadHistory(contentService);

            say("Starting find/download new podcasts. CarCast ver " + CarCastApplication.getVersion());
            say("Problems? please use Menu / Email Download Report - THANKS!");

            List<Subscription> sites = contentService.getSubscriptions();


            say("\nSearching " + sites.size() + " subscriptions. " + sdf.format(new Date()));

            totalSites = sites.size();

            say("History of downloads contains " + history.size() + " podcasts.");

            List<MetaNet> enclosures = new ArrayList<MetaNet>();

            SAXParserFactory spf = SAXParserFactory.newInstance();

            for (Subscription sub : sites) {
                EnclosureHandler encloseureHandler = new EnclosureHandler(history, sub.priority);

                if (sub.enabled) {
                    try {
                        say("\nScanning subscription/feed: " + sub.url);
                        URL url = new URL(sub.url);
                        int foundStart = encloseureHandler.metaNets.size();
                        if (sub.maxDownloads == Subscription.GLOBAL)
                            encloseureHandler.setMax(globalMax);
                        else
                            encloseureHandler.setMax(sub.maxDownloads);

                        String name = sub.name;
                        encloseureHandler.setFeedName(name);

                        Util.findAvailablePodcasts(sub.url, encloseureHandler);

                        String message = sitesScanned + "/" + sites.size() + ": " + name + ", "
                                + (encloseureHandler.metaNets.size() - foundStart) + " new";
                        say(message);
                        contentService.updateNotification(message);

                    } catch (Throwable e) {
					/* Display any Error to the GUI. */
                        say("Error ex:" + e.getMessage());
                        Log.e("BAH", "bad", e);
                    }
                } else {
                    say("\nSkipping subscription/feed: " + sub.url + " because it is not enabled.");
                }

                sitesScanned++;

                if (sub.orderingPreference == OrderingPreference.LIFO)
                    Collections.reverse(encloseureHandler.metaNets);

                enclosures.addAll(encloseureHandler.metaNets);

            } // endforeach

            say("\nTotal enclosures " + enclosures.size());

            List<MetaNet> newPodcasts = new ArrayList<MetaNet>();
            for (MetaNet metaNet : enclosures) {
                if (history.contains(metaNet))
                    continue;
                newPodcasts.add(metaNet);
            }
            say(newPodcasts.size() + " podcasts will be downloaded.");
            contentService.updateNotification(newPodcasts.size() + " podcasts will be downloaded.");

            totalPodcasts = newPodcasts.size();
            for (MetaNet metaNet : newPodcasts) {
                podcastsTotalBytes += metaNet.getSize();
            }

            System.setProperty("http.maxRedirects", "50");
            say("\n");
            byte[] buf = new byte[16383];

            int got = 0;
            for (int i = 0; i < newPodcasts.size(); i++) {
                String shortName = newPodcasts.get(i).getTitle();
                String localFileExt = getLocalFileExtFromMimetype(newPodcasts.get(i).getMimetype());
                say((i + 1) + "/" + newPodcasts.size() + " " + shortName);
                contentService.updateNotification((i + 1) + "/" + newPodcasts.size() + " " + shortName);
                podcastsDownloaded = i + 1;

                try {
                    Config config = new Config(contentService);
                    String prefix = "";

                                /*
                                 * Non-priority podcast files are named XXXX.mp3, where XXXX is the millisecond timestamp at
                                 * the time the podcast was downloaded.
                                 *
                                 * Priority podcast files are named YYYY:00:XXXX.mp3, where XXXX is as above, and YYYY is the timestamp of
                                 * file currently in the player.
                                 *
                                 * Notes:
                                 *    ":" is chosen as the separator because it sorts after the "." of the file name suffix.
                                 *    The "00" is incuded to make it possible to have multiple priority levels in the future.
                                 *
                                 * IMPORTANT:
                                 *    The naming scheme used here *must* match MetaHolder.isPriority().
                                 */

                    if (newPodcasts.get(i).getPriority())
                        if (contentService.currentMeta() != null)
                            prefix = contentService.currentMeta().getBaseFilename() + ":00:";

                    String castFileName = prefix + System.currentTimeMillis() + localFileExt;
                    File castFile = config.getPodcastRootPath(castFileName);


                    Log.d("CarCast", "New podcast file: " + castFileName);

                    currentSubscription = newPodcasts.get(i).getSubscription();
                    currentTitle = newPodcasts.get(i).getTitle();
                    File tempFile = config.getPodcastRootPath("tempFile");
                    say("Subscription: " + currentSubscription);
                    say("Title: " + currentTitle);
                    say("enclosure url: " + new URL(newPodcasts.get(i).getUrl()));
                    InputStream is = getInputStream(new URL(newPodcasts.get(i).getUrl()));
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    int amt = 0;
                    int expectedSizeKilo = newPodcasts.get(i).getSize() / 1024;
                    String preDownload = sb.toString();
                    int totalForThisPodcast = 0;
                    say(String.format("%dk/%dk 0", totalForThisPodcast / 1024, expectedSizeKilo) + "%\n");
                    while ((amt = is.read(buf)) >= 0) {
                        fos.write(buf, 0, amt);
                        podcastsCurrentBytes += amt;
                        totalForThisPodcast += amt;
                        sb = new StringBuilder(preDownload
                                + String.format("%dk/%dk  %d", totalForThisPodcast / 1024, expectedSizeKilo,
                                (int) ((totalForThisPodcast / 10.24) / expectedSizeKilo)) + "%\n");
                    }
                    say("download finished.");
                    fos.close();
                    is.close();
                    // add before rename, so if rename fails, we remember
                    // that we tried this file and skip it next time.
                    history.add(newPodcasts.get(i));

                    tempFile.renameTo(castFile);
                    new MetaFile(newPodcasts.get(i), castFile).save();

                    got++;
                    if (totalForThisPodcast != newPodcasts.get(i).getSize()) {
                        say("Note: reported size (in feed) doesnt match actual size (downloaded file)");
                        // subtract out wrong value
                        podcastsTotalBytes -= newPodcasts.get(i).getSize();
                        // add in correct value
                        podcastsTotalBytes += totalForThisPodcast;

                    }
                    say("-");
                    // update progress for player
                    contentService.newContentAdded();

                } catch (Throwable e) {
                    say("Problem downloading " + newPodcasts.get(i).getUrl() + " e:" + e);
                }
            }
            say("Finished. Downloaded " + got + " new podcasts. " + sdf.format(new Date()));

            contentService.doDownloadCompletedNotification(got);
        } finally {
            isRunning = false;
        }
	}

	// Deal with servers with "location" instead of "Location" in redirect
	// headers
	private InputStream getInputStream(URL url) throws IOException {
		int redirectLimit = 15;
		while (redirectLimit-- > 0) {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setInstanceFollowRedirects(false);
			con.setConnectTimeout(20 * 1000);
			con.setReadTimeout(30 * 1000);
			con.connect();
			if (con.getResponseCode() == 200) {
				return con.getInputStream();
			}
			if (con.getResponseCode() == 404 && url.getPath().contains(" ")){
				String newURL = url.getProtocol()+"://"+url.getHost()+(url.getPort()==-1?"":(":"+url.getPort()))+
						url.getPath().replaceAll(" ", "%20"); 
				return getInputStream(new URL(newURL));
			}
			if (con.getResponseCode() > 300 && con.getResponseCode() > 399) {
				say(url + " gave resposneCode " + con.getResponseCode());
				throw new IOException();
			}
			url = null;
			for (int i = 0; i < 50; i++) {
				if (con.getHeaderFieldKey(i) == null)
					continue;
				if (con.getHeaderFieldKey(i).toLowerCase().equals("location")) {
					url = new URL(con.getHeaderField(i));
				}
			}
			if (url == null) {
				say("Got 302 without Location");
			}
		}
		throw new IOException(CarCastApplication.getAppTitle() + " redirect limit reached");
	}

	public String getStatus() {
		if (sitesScanned != totalSites)
			return "Scanning Sites " + sitesScanned + "/" + totalSites;
		return "Fetching " + podcastsDownloaded + "/" + totalPodcasts + "\n" + (podcastsCurrentBytes / 1024) + "k/"
				+ (podcastsTotalBytes / 1024) + "k";
	}

    public String getEncodedStatus(){
        String status = (isRunning() ? "running" : "done") + "," + sitesScanned + "," + totalSites + ","
                + podcastsDownloaded + "," + totalPodcasts + "," + podcastsCurrentBytes + ","
                + podcastsTotalBytes + "," + currentSubscription + "," + currentTitle;
        return status;

    }

	@Override
	public void say(String text) {
		sb.append(text);
		sb.append('\n');
		Log.i("CarCast/Download", text);
	}

}
