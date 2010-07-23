package com.jadn.cc.services;

import java.util.Map;

public interface SubscriptionHelper {
    public Map<String, String> getSubscriptions();
    public boolean addSubscription(String name, String url);
    public boolean removeSubscription(String url);
    public boolean editSubscription(String originalUrl, String name, String url);

    /**
     * 
     * @param subscriptions what to save
     * @return <code>true</code> if save succeeded, <code>false</code> otherwise
     */
    public boolean saveSubscriptions(Map<String, String> subscriptions);

    public void resetToDemoSubscriptions();
}
