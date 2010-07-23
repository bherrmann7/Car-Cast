package com.jadn.cc.services;

import java.io.*;
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

import com.jadn.cc.core.PlaySet;
import com.jadn.cc.core.Sayer;
import com.jadn.cc.core.Subscription;
import com.jadn.cc.ui.BaseActivity;

public class DownloadHelper implements Sayer {
	int max;

	public DownloadHelper(int max) {
		this.max = max;
	}

	private static File histFile = new File(PlaySet.PODCASTS.getRoot(),
			"history.prop");

	TextView tv;

	StringBuilder newText = new StringBuilder();

	int totalSites;
	int sitesScanned;
	int totalPodcasts;
	int podcastsTotalBytes;
	int podcastsDownloaded;
	int podcastsCurrentBytes;
	public String currentSubscription = " ";
	public String currentTitle = " ";

	public String getStatus() {
		if (sitesScanned != totalSites)
			return "Scanning Sites " + sitesScanned + "/" + totalSites;
		return "Fetching " + podcastsDownloaded + "/" + totalPodcasts + "\n"
				+ (podcastsCurrentBytes / 1024) + "k/"
				+ (podcastsTotalBytes / 1024) + "k";
	}

	protected void downloadNewPodCasts(ContentService contentService,
			String accounts) {
		say("loading podcast sites.");

		List<Subscription> sites = contentService.getSubscriptions();

		postSitesToJadn(accounts, sites);

		say("starting download of podcast " + sites.size()
				+ " site's rss feeds.");

		totalSites = sites.size();

		List<String> history = getHistory();
		say("History of downloads contains " + history.size() + " podcasts.");

		SAXParserFactory spf = SAXParserFactory.newInstance();
		EnclosureHandler encloseureHandler = new EnclosureHandler(max, history,
				this);

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

                String message = sitesScanned + "/" + sites.size() + ": "
                        + name + " "
                        + (encloseureHandler.metaNets.size() - foundStart)
                        + " podcasts";
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
			if (history.contains(metaNet.getUrlShortName()))
				continue;
			newPodcasts.add(metaNet);
		}
		say(newPodcasts.size() + " podcasts will be downloaded.");
		contentService.updateNotification(newPodcasts.size()
				+ " podcasts will be downloaded.");

		totalPodcasts = newPodcasts.size();
		for (MetaNet metaNet : newPodcasts) {
			podcastsTotalBytes += metaNet.getSize();
		}

		System.setProperty("http.maxRedirects", "50");

		int got = 0;
		for (int i = 0; i < newPodcasts.size(); i++) {
			String shortName = newPodcasts.get(i).getUrlShortName();
			say((i + 1) + "/" + newPodcasts.size() + " " + shortName);
			contentService.updateNotification((i + 1) + "/"
					+ newPodcasts.size() + " " + shortName);
			podcastsDownloaded = i + 1;

			try {
				File castFile = new File(PlaySet.PODCASTS.getRoot(), Long
						.toString(System.currentTimeMillis())
						+ ".mp3");
				if (encloseureHandler.isVideo(newPodcasts.get(i).getUrl()
						.toString())) {
					castFile = new File(android.os.Environment
							.getExternalStorageDirectory(), "dcim/Camera/"
							+ Long.toString(System.currentTimeMillis())
							+ ".mp4");
				}
				// This logic used to ensure we don't download the same file more than once
				// by using the filename to verify if we have it or not, but now we dont
				// trust the filename, so it could use some reworking
				if (castFile.exists()) {
					say("Skipping already have: " + shortName);
					history.add(shortName);
				} else {
					currentSubscription = newPodcasts.get(i).getSubscription();
					currentTitle = newPodcasts.get(i).getTitle();
					File tempFile = new File(PlaySet.PODCASTS.getRoot(),
							"tempFile");
					InputStream is = getInputStream(new URL(newPodcasts.get(i).getUrl()));
					FileOutputStream fos = new FileOutputStream(tempFile);
					byte[] buf = new byte[2048];
					int amt = 0;
					while ((amt = is.read(buf)) > 0) {
						fos.write(buf, 0, amt);
						podcastsCurrentBytes += amt;
					}
					fos.close();
					is.close();
					// add before rename, so if rename fails, we remember
					// that we tried this file and skip it next time.
					history.add(shortName);
					tempFile.renameTo(castFile);
					new MetaFile(newPodcasts.get(i), castFile).save();
					got++;
					if(podcastsCurrentBytes != newPodcasts.get(i).getSize()){
						// subtract out wrong value
						podcastsTotalBytes -= newPodcasts.get(i).getSize();
						// add in correct value
						podcastsTotalBytes += podcastsCurrentBytes;
					}					
				}
			} catch (IOException e) {
				say("Problem downloading "
						+ newPodcasts.get(i).getUrlShortName() + " e:" + e);
			}
		}
		try {
			PrintWriter histOut = new PrintWriter(new FileWriter(histFile));
			for (String line : history) {
				histOut.println(line);
			}
			histOut.close();
		} catch (IOException e) {
			say("problem writting history file: " + histFile + " ex:" + e);
		}
		say("finished downloading. Got " + got + " new podcasts.");

		contentService.doDownloadCompletedNotification(got);
	}

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
			OutputStreamWriter wr = new OutputStreamWriter(conn
					.getOutputStream());
			wr.write("appVersion="
					+ URLEncoder.encode(BaseActivity.getVersion(), "UTF-8"));
			wr.write('&');
			wr.write("accounts=" + URLEncoder.encode(accounts, "UTF-8"));
			wr.write('&');
			wr.write("sites=" + URLEncoder.encode(data.toString(), "UTF-8"));
			wr.flush();

			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(conn
					.getInputStream()));
			//String line = null;
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

	StringBuilder sb = new StringBuilder();

	@Override
	public void say(String text) {
		sb.append(text);
		sb.append('\n');
	}

	public static void eraseHistory() {
		histFile.delete();
	}

	public static List<String> getHistory() {
		List<String> history = new ArrayList<String>();
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(
					histFile));
			String line = null;
			while ((line = dis.readLine()) != null) {
				history.add(line);
			}
		} catch (Exception e) {
			Log.e(DownloadHelper.class.getName(), e.toString());
		}
		return history;
		
	}

}