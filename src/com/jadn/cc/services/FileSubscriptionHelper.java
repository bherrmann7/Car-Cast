package com.jadn.cc.services;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import android.util.Log;

import com.jadn.cc.core.Util;
import com.jadn.cc.trace.TraceUtil;

public class FileSubscriptionHelper implements SubscriptionHelper {

    private final File legacyFile;
    private final File subscriptionFile;

    public FileSubscriptionHelper(File subscriptionFile, File legacyFile) {
        this.subscriptionFile = subscriptionFile;
        this.legacyFile = legacyFile;
    }

    @Override
    public boolean addSubscription(String name, String url) {
        Map<String, String> subs = getSubscriptions();

        if (subs.containsKey(url)) {
            // we already have the URL stored:
            return false;
        } // endif

        // test the url:
        if (Util.isValidURL(url)) {
            // passed, put it in and save:
            subs.put(url, name);
            saveSubscriptions(subs);

            return true;

        } else {
            Log.e("CarCast", "addSubscription: bad url: "+url);
            return false;
        }
    }

    @Override
    public Map<String, String> getSubscriptions() {
        if (legacyFile.exists()) {
            // we need to convert to the new format first:
            Map<String, String> legacy = getLegacySitesFromFile();
            saveSubscriptions(legacy);
            legacyFile.delete();
            // short-cut out:
            return legacy;
        }
        if (!subscriptionFile.exists()) {
            subscriptionFile.getParentFile().mkdirs();
            resetToDemoSubscriptions();
        }
        if (!subscriptionFile.exists()) {
            return null;
        }
        try {
            InputStream dis = new BufferedInputStream(new FileInputStream(subscriptionFile));
            Properties props = new Properties();
            props.load(dis);

            return cleanupProperties(props);

        } catch (Exception e1) {
            TraceUtil.report(e1);
            return Collections.emptyMap();
        }
    }

    Map<String,String> getLegacySitesFromFile() {
        if (!legacyFile.exists()) {
            return Collections.emptyMap();
        }
        try {
            InputStream input = new FileInputStream(legacyFile);
            return readLegacySites(input);

        } catch (Exception e1) {
            TraceUtil.report(e1);
            return Collections.emptyMap();
        }
    }

    Map<String, String> readLegacySites(InputStream input) throws IOException {
        Map<String,String> sites = new HashMap<String, String>();
        DataInputStream dis = new DataInputStream(input);
        String line = null;
        while ((line = dis.readLine()) != null) {
            int eq = line.indexOf('=');
            if (eq != -1) {
                String name = line.substring(0, eq);
                String url = line.substring(eq + 1);
                if (Util.isValidURL(url)) {
                    sites.put(url, name);
                } else {
                    TraceUtil.report(new RuntimeException("invalid URL in line: '" + line + "'; URL was: " + url));
                } // endif

            } else {
                TraceUtil.report(new RuntimeException("missing equals in line: " + line));
            }
        }

        return sites;
    }
    
    /**
     * Insure that all properties are keyed properly, by URL. If a key is not a
     * url, but the value is, then the pair is removed, then placed back in
     * reverse.
     * 
     * @param props the properties to scan
     */
    Map<String, String> cleanupProperties(Properties props) {
        Map<String, String> urlToName = new HashMap<String, String>();

        Set<Object> keys = props.keySet();
        for (Object key : keys) {
            String keyStr = (String) key;
            if (Util.isValidURL(keyStr)) {
                urlToName.put(keyStr, props.getProperty(keyStr));
            } else {
                // see if the value was a URL, instead:
                String value = props.getProperty(keyStr);
                if (Util.isValidURL(value)) {
                    // if we are here, value worked but key didn't, so reverse
                    // the props:
                    urlToName.put(value, keyStr);
                } else {
                    // value wasn't a URL either, not much we can do here.
                    Log.w("CarCast", "couldn't read subscription " + key + "=" + value);
                } // endif

            } // endif
        } // endforeach

        return urlToName;
    }

    @Override
    public boolean removeSubscription(String url) {
        Map<String, String> subs = getSubscriptions();
        boolean wasPresent = subs.containsKey(url);
        subs.remove(url);
        saveSubscriptions(subs);

        return wasPresent;
    }

    @Override
    public boolean saveSubscriptions(Map<String, String> subscriptions) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(subscriptionFile));
            Properties subs = new Properties();
            subs.putAll(subscriptions);

            subs.store(bos, "Carcast Subscription File");
            bos.close();

            // success:
            return true;

        } catch (IOException e) {
            TraceUtil.report(e);
            // failure:
            return false;
        }
    }

    @Override
    public void resetToDemoSubscriptions() {
        Properties subs = new Properties();
        subs.setProperty("http://www.discovery.com/radio/xml/sciencechannel.xml", "Science Channel");
        subs.setProperty("http://www.cbc.ca/podcasting/includes/quirks.xml", "Quirks and Quarks");
        subs.setProperty("http://www.cringely.com/feed/podcast/", "Cringely");
        subs.setProperty("http://rss.sciam.com/sciam/60secsciencepodcast", "60 second science");
        subs.setProperty("http://rss.sciam.com/sciam/60-second-psych", "60 second psych");
        subs.setProperty("http://rss.sciam.com/sciam/60-second-earth", "60 second earth");
        subs.setProperty("http://nytimes.com/services/xml/rss/nyt/podcasts/techtalk.xml", "New York Times Tech Talk");
        Map<String, String> hack = Util.cast(subs);
        saveSubscriptions(hack);
    }

    public boolean editSubscription(String originalUrl, String name, String url) {
        Map<String, String> subs = getSubscriptions();
        try {
            if (!subs.containsKey(originalUrl)) {
                // we don't already have the URL stored:
                return false;
            } // endif

            // test the url:
            new URL(url);

            // update the entry:
            subs.remove(originalUrl);

            subs.put(url, name);
            saveSubscriptions(subs);

            return true;

        } catch (MalformedURLException e) {
            Log.e("CarCast", "addSubscription", e);
        }

        return false;
    }

}
