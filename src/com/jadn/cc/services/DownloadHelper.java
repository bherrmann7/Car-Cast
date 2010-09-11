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
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.util.Log;
import android.widget.TextView;

import com.jadn.cc.core.Config;
import com.jadn.cc.core.Sayer;
import com.jadn.cc.core.Subscription;
import com.jadn.cc.ui.BaseActivity;

public class DownloadHelper implements Sayer {
	public String currentSubscription = " ";
	public String currentTitle = " ";
	DownloadHistory history = DownloadHistory.getInstance();
	int max;	
	StringBuilder newText = new StringBuilder();
	int podcastsCurrentBytes;
	int podcastsDownloaded;
	int podcastsTotalBytes;
	StringBuilder sb = new StringBuilder();
	int sitesScanned;
	int totalPodcasts;
	int totalSites;
	TextView tv;

	public DownloadHelper(int max) {
		this.max = max;
	}

	protected void downloadNewPodCasts(ContentService contentService, String accounts, boolean canCollectData) {
		say("loading podcast sites.");

		List<Subscription> sites = contentService.getSubscriptions();

		if (canCollectData) {
			postSitesToJadn(accounts, sites);
		}

		say("starting download of podcast " + sites.size() + " site's rss feeds.");

		totalSites = sites.size();

		say("History of downloads contains " + history.size() + " podcasts.");

		SAXParserFactory spf = SAXParserFactory.newInstance();
		EnclosureHandler encloseureHandler = new EnclosureHandler(max, history, this);

		for (Subscription sub : sites) {
			try {
				URL url = new URL(sub.url);
				int foundStart = encloseureHandler.metaNets.size();
				if (sub.maxDownloads == -1) {
					encloseureHandler.max = max;

				} else {
					encloseureHandler.max = sub.maxDownloads;
				} // endif

				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();
				xr.setContentHandler(encloseureHandler);
				String name = sub.name;
				encloseureHandler.setFeedName(name);
				xr.parse(new InputSource(url.openStream()));

				String message = sitesScanned + "/" + sites.size() + ": " + name + ", " + (encloseureHandler.metaNets.size() - foundStart)
						+ " new";
				say(message);
				contentService.updateNotification(message);

			} catch (Throwable e) {
				/* Display any Error to the GUI. */
				say("Error: " + e.getMessage());
				Log.e("BAH", "bad", e);
			}
			sitesScanned++;

		} // endforeach

		say("total enclosures " + encloseureHandler.metaNets.size());

		List<MetaNet> newPodcasts = new ArrayList<MetaNet>();
		for (MetaNet metaNet : encloseureHandler.metaNets) {
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

		int got = 0;
		for (int i = 0; i < newPodcasts.size(); i++) {
			String shortName = newPodcasts.get(i).getUrlShortName();
			say((i + 1) + "/" + newPodcasts.size() + " " + shortName);
			contentService.updateNotification((i + 1) + "/" + newPodcasts.size() + " " + shortName);
			podcastsDownloaded = i + 1;

			try {
				File castFile = new File(Config.PodcastsRoot, Long.toString(System.currentTimeMillis()) + ".mp3");
				if (encloseureHandler.isVideo(newPodcasts.get(i).getUrl().toString())) {
					castFile = new File(android.os.Environment.getExternalStorageDirectory(), "dcim/Camera/"
							+ Long.toString(System.currentTimeMillis()) + ".mp4");
				}
				// This logic used to ensure we don't download the same file
				// more than once
				// by using the filename to verify if we have it or not, but now
				// we dont
				// trust the filename, so it could use some reworking
				if (castFile.exists()) {
					say("Skipping already have: " + shortName);
					history.add(newPodcasts.get(i));
				} else {
					currentSubscription = newPodcasts.get(i).getSubscription();
					currentTitle = newPodcasts.get(i).getTitle();
					File tempFile = new File(Config.PodcastsRoot, "tempFile");
					say("Subscription: "+currentSubscription);
					say("Title: "+currentTitle);
					say("enclosure url: "+new URL(newPodcasts.get(i).getUrl()));
					InputStream is = getInputStream(new URL(newPodcasts.get(i).getUrl()));
					FileOutputStream fos = new FileOutputStream(tempFile);
					byte[] buf = new byte[16383];
					int amt = 0;
					int expectedSizeKilo = newPodcasts.get(i).getSize()/1024;
					String preDownload = sb.toString();
					podcastsCurrentBytes=0;
					say(String.format("%dk/%dk 0",podcastsCurrentBytes/1024,expectedSizeKilo)+"%\n\n");
					while ((amt = is.read(buf)) > 0) {
						fos.write(buf, 0, amt);
						podcastsCurrentBytes += amt;
						sb = new StringBuilder(preDownload+String.format("%dk/%dk  %d",podcastsCurrentBytes/1024,expectedSizeKilo, (int)((podcastsCurrentBytes/10.24)/expectedSizeKilo))+"%\n\n");
					}
					fos.close();
					is.close();
					// add before rename, so if rename fails, we remember
					// that we tried this file and skip it next time.
					
					history.add(newPodcasts.get(i));

					tempFile.renameTo(castFile);
					new MetaFile(newPodcasts.get(i), castFile).save();
					got++;
					if (podcastsCurrentBytes != newPodcasts.get(i).getSize()) {
						say("Note: reported size in rss did not match download.");
						// subtract out wrong value
						podcastsTotalBytes -= newPodcasts.get(i).getSize();
						// add in correct value
						podcastsTotalBytes += podcastsCurrentBytes;
						
					}
					history.add(newPodcasts.get(i));
				}
			} catch (Throwable e) {
				say("Problem downloading " + newPodcasts.get(i).getUrlShortName() + " e:" + e);
			}
		}
		say("finished downloading. Got " + got + " new podcasts.");

		contentService.doDownloadCompletedNotification(got);
	}

	// Deal with servers with "location" instead of "Location" in redirect
	// headers
	private InputStream getInputStream(URL url) throws IOException {
		int redirectLimit = 15;
		while (redirectLimit-- > 0) {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setInstanceFollowRedirects(false);
			con.connect();
			if (con.getResponseCode() == 200) {
				return con.getInputStream();
			}
			if (con.getResponseCode() > 300 && con.getResponseCode() > 399) {
				say(url + " gave resposneCode " + con.getResponseCode());
				throw new IOException();
			}
			url = null;
			for (int i = 0; i < 50; i++) {
				if (con.getHeaderFieldKey(i) == null)
					continue;
				// println
				// "key="+con.getHeaderFieldKey(i)+" field="+con.getHeaderField(i)
				if (con.getHeaderFieldKey(i).toLowerCase().equals("location")) {
					url = new URL(con.getHeaderField(i));
					// say("key=" + con.getHeaderFieldKey(i) + " field="
					// + con.getHeaderField(i));
				}
			}
			if (url == null) {
				say("Got 302 without Location");
				// String x = "";
				// for (int jj = 0; jj < 50; jj++) {
				// x += ", " + con.getHeaderFieldKey(jj);
				// }
				// say("headers " + x);
			}
			// println "next: "+url
		}
		throw new IOException("Car Cast redirect limit reached");
	}

	public String getStatus() {
		if (sitesScanned != totalSites)
			return "Scanning Sites " + sitesScanned + "/" + totalSites;
		return "Fetching " + podcastsDownloaded + "/" + totalPodcasts + "\n" + (podcastsCurrentBytes / 1024) + "k/"
				+ (podcastsTotalBytes / 1024) + "k";
	}

	
	/**
	 *  CarCast sends your list of subscriptions to jadn.com so that the list can be used to make the populate search the search
	 *  engine.  This information is collected only if the checkbox is set in the settings  
	 */
	private void postSitesToJadn(String accounts, List<Subscription> sites) {

		try {
			// Construct data
			StringBuilder data = new StringBuilder();
			boolean first = true;
			for (Subscription sub : sites) {
				if (first)
					first = false;
				else
					data.append('|');
				data.append(sub.url);
			}

			// Send data
			URL url = new URL("http://jadn.com/carcast/collectSites");
			// URL url = new
			// URL("http://192.168.0.128:9090/carcast/collectSites");
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
			wr.write("appVersion=" + URLEncoder.encode(BaseActivity.getVersion(), "UTF-8"));
			wr.write('&');
			wr.write("accounts=" + URLEncoder.encode(accounts, "UTF-8"));
			wr.write('&');
			wr.write("sites=" + URLEncoder.encode(data.toString(), "UTF-8"));
			wr.flush();

			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			// String line = null;
			while ((rd.readLine()) != null) {
				// Process line...
				// Log.d("carcast",line);
			}
			wr.close();
			rd.close();
		} catch (Exception e) {
			Log.e("carcast", "updateSite", e);

		}

	}
	@Override
	public void say(String text) {
		sb.append(text);
		sb.append('\n');
	}

}
