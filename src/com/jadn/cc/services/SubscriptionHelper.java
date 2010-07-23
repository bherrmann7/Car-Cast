package com.jadn.cc.services;

import java.util.List;

import com.jadn.cc.core.Subscription;

public interface SubscriptionHelper {
    public List<Subscription> getSubscriptions();
    public boolean addSubscription(Subscription toAdd);
    public boolean removeSubscription(Subscription toRemove);
    public boolean editSubscription(Subscription original, Subscription updated);
    public List<Subscription> resetToDemoSubscriptions();
    public void deleteAllSubscriptions();
}
