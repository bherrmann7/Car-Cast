package com.jadn.cc.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import android.util.Log;

import com.jadn.cc.core.OrderingPreference;
import com.jadn.cc.core.Subscription;
import com.jadn.cc.core.Util;
import com.jadn.cc.trace.TraceUtil;

public class FileSubscriptionHelper implements SubscriptionHelper {

    private static final String CONCAT_DIVIDER = "\\;";
    private static final String REGEX_DIVIDER = "\\\\;";
    private final File legacyFile;
    private final File subscriptionFile;

    public FileSubscriptionHelper(File subscriptionFile, File legacyFile) {
        this.subscriptionFile = subscriptionFile;
        this.legacyFile = legacyFile;
    }

    @Override
    public boolean addSubscription(Subscription toAdd) {
        List<Subscription> subs = getSubscriptions();

        if (containsSubscriptionURL(subs, toAdd.url)) {
            // we already have the URL stored:
            return false;
        } // endif

        // test the url:
        if (Util.isValidURL(toAdd.url)) {
            // passed, put it in and save:
            subs.add(toAdd);
            saveSubscriptions(subs);

            return true;

        } else {
            Log.e("CarCast", "addSubscription: bad url: " + toAdd.url);
            return false;
        }
    }

    /**
     * Scan the list for a subscription by its URL
     * 
     * @param subs the list to scan
     * @param url the URL to look for
     * @return <code>true</code> if found in the list, <code>false</code>
     *         otherwise.
     */
    private boolean containsSubscriptionURL(List<Subscription> subs, String url) {
        return indexOfSubscriptionURL(subs, url) != -1;
    }

    /**
     * Insure that all properties are keyed properly, by URL. If a key is not a
     * url, but the value is, then the pair is removed, then placed back in
     * reverse.
     * 
     * @param props the properties to scan
     */
    List<Subscription> convertProperties(Properties props) {
        List<Subscription> subscriptions = new ArrayList<Subscription>();

        Set<Object> keys = props.keySet();
        for (Object key : keys) {
            String url = (String) key;
            String nameAndMore = props.getProperty(url, "");
            Subscription sub = convertProperty(url, nameAndMore);
            if (sub != null) {
                subscriptions.add(sub);
            } // endif
        } // endforeach

        return subscriptions;
    }

    private Subscription convertProperty(String url, String nameAndMore) {
        String[] split = nameAndMore.split(REGEX_DIVIDER);
 
        if (split.length == 4) {
	        // best case, we should have all properties:
	        try {
	            String name = split[0];
	            int maxCount = Integer.valueOf(split[1]);
	            OrderingPreference pref = OrderingPreference.valueOf(split[2]);
	            boolean enabled = Boolean.valueOf(split[3]);
	            return new Subscription(name, url, maxCount, pref, enabled);
	
	        } catch (Exception ex) {
	            Log.w("CarCast", "couldn't read subscription " + url + "=" + nameAndMore);
	        } // endtry
        } else if (split.length == 3) {
	        // second best case, we have all properties except enabled:
	        try {
	            String name = split[0];
	            int maxCount = Integer.valueOf(split[1]);
	            OrderingPreference pref = OrderingPreference.valueOf(split[2]);
	            return new Subscription(name, url, maxCount, pref);
	
	        } catch (Exception ex) {
	            Log.w("CarCast", "couldn't read subscription " + url + "=" + nameAndMore);
	        } // endtry
        } else if (split.length == 1) {
            String name = split[0];
            // oops, missing extra properties:
            return new Subscription(name, url);

        } else {
            Log.w("CarCast", "couldn't read subscription " + url + "=" + nameAndMore);
        } // endif
        
        return null;
    }

    @Override
    public void deleteAllSubscriptions() {
        List<Subscription> emptyList = Collections.emptyList();
        saveSubscriptions(emptyList);
    }

    @Override
    public boolean editSubscription(Subscription original, Subscription updated) {
        List<Subscription> subs = getSubscriptions();
        int idx = indexOfSubscriptionURL(subs, original.url);
        if (idx != -1) {
            subs.remove(idx);
            subs.add(updated);
            saveSubscriptions(subs);
            return true;
        } // endif

        return false;
    }

    List<Subscription> getLegacySitesFromFile() {
        if (!legacyFile.exists()) {
            return Collections.emptyList();
        }
        try {
            InputStream input = new FileInputStream(legacyFile);
            return readLegacySites(input);

        } catch (Exception e1) {
            TraceUtil.report(e1);
            return Collections.emptyList();
        }
    }

    @Override
    public List<Subscription> getSubscriptions() {
        if (legacyFile.exists()) {
            // we need to convert to the new format first:
            List<Subscription> legacy = getLegacySitesFromFile();
            saveSubscriptions(legacy);
            legacyFile.delete();
            // short-cut out:
            return legacy;
        }

        if (!subscriptionFile.exists()) {
            subscriptionFile.getParentFile().mkdirs();
            return resetToDemoSubscriptions();
        }
        if (!subscriptionFile.exists()) {
            return null;
        }
        try {
            InputStream dis = new BufferedInputStream(new FileInputStream(subscriptionFile));
            Properties props = new Properties();
            props.load(dis);

            return convertProperties(props);

        } catch (Exception e1) {
            TraceUtil.report(e1);
            return Collections.emptyList();
        }
    }

    /**
     * Scan the list for a subscription by its URL
     * 
     * @param subs the list to scan
     * @param url the URL to look for
     * @return the index in the list, or -1 if not found
     */
    private int indexOfSubscriptionURL(List<Subscription> subs, String url) {
        for (int i = 0; i < subs.size(); i++) {
            Subscription sub = subs.get(i);
            if (sub.url.equals(url)) {
                return i;
            } // endif
        } // endfor

        // not found:
        return -1;
    }

    List<Subscription> readLegacySites(InputStream input) throws IOException {
        List<Subscription> sites = new ArrayList<Subscription>();
        DataInputStream dis = new DataInputStream(input);
        String line = null;
        while ((line = dis.readLine()) != null) {
            int eq = line.indexOf('=');
            if (eq != -1) {
                String name = line.substring(0, eq);
                String url = line.substring(eq + 1);
                if (Util.isValidURL(url)) {
                    sites.add(new Subscription(name, url));
                } else {
                    TraceUtil.report(new RuntimeException("invalid URL in line: '" + line + "'; URL was: " + url));
                } // endif

            } else {
                TraceUtil.report(new RuntimeException("missing equals in line: " + line));
            }
        }

        return sites;
    }

    @Override
    public boolean removeSubscription(Subscription toRemove) {
        List<Subscription> subs = getSubscriptions();
        int idx = indexOfSubscriptionURL(subs, toRemove.url);
        if (idx != -1) {
            subs.remove(idx);
            saveSubscriptions(subs);
            return true;
        } // endif

        return false;
    }

    @Override
    public boolean toggleSubscription(Subscription toToggle) {
        List<Subscription> subs = getSubscriptions();
        int idx = indexOfSubscriptionURL(subs, toToggle.url);
        if (idx != -1) {
        	Subscription sub = subs.get(idx);
            sub.enabled = !sub.enabled;
            saveSubscriptions(subs);
            return true;
        } // endif

        return false;
    }
    
    @Override
    public List<Subscription> resetToDemoSubscriptions() {
        List<Subscription> subs = new ArrayList<Subscription>();
        subs.add(new Subscription("Science Channel", "http://www.discovery.com/radio/xml/sciencechannel.xml"));
        subs.add(new Subscription("Quirks and Quarks", "http://www.cbc.ca/podcasting/includes/quirks.xml"));
        subs.add(new Subscription("Cringely", "http://www.cringely.com/feed/podcast/"));
        subs.add(new Subscription("60 second science", "http://rss.sciam.com/sciam/60secsciencepodcast"));
        subs.add(new Subscription("60 second psych", "http://rss.sciam.com/sciam/60-second-psych"));
        subs.add(new Subscription("60 second earth", "http://rss.sciam.com/sciam/60-second-earth"));
        subs.add(new Subscription("New York Times Tech Talk",
                "http://nytimes.com/services/xml/rss/nyt/podcasts/techtalk.xml"));
        saveSubscriptions(subs);
        return subs;
    }
    
    private boolean saveSubscriptions(List<Subscription> subscriptions) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(subscriptionFile));
            Properties outSubs = new Properties();
            
            for (Subscription sub : subscriptions) {
                String valueStr = sub.name + CONCAT_DIVIDER + sub.maxDownloads + CONCAT_DIVIDER + sub.orderingPreference.name() + CONCAT_DIVIDER + sub.enabled;
                outSubs.put(sub.url, valueStr);
            } // endforeach
            
            outSubs.store(bos, "Carcast Subscription File v3");
            bos.close();

            // success:
            return true;

        } catch (IOException e) {
            TraceUtil.report(e);
            // failure:
            return false;
        }
    }
}
