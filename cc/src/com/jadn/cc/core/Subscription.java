package com.jadn.cc.core;

import android.os.Parcel;
import android.os.Parcelable;

public class Subscription implements Parcelable, Comparable<Subscription> {

    public static final Parcelable.Creator<Subscription> CREATOR = new Parcelable.Creator<Subscription>() {
         public Subscription createFromParcel(Parcel in) {        	 
             return new Subscription(in.readString(),   // name
                                     in.readString(),   // URL
                                     in.readInt(),      // max downloads
                                     OrderingPreference.values()[in.readInt()],// order pref
                                     Boolean.parseBoolean(in.readString())); //enabled 
         }

         public Subscription[] newArray(int size) {
             return new Subscription[size];
         }
     };
	public static int GLOBAL = 0;
    public int                maxDownloads = GLOBAL;
    public String             name;
    public OrderingPreference orderingPreference;
	public String             url;
    public boolean enabled;

    public Subscription(String name, String url) {
        this(name, url, GLOBAL, OrderingPreference.FIFO, true);
    }

    public Subscription(String name, String url, int maxDownloads, OrderingPreference orderingPreference) {
        this(name, url, maxDownloads, orderingPreference, true);
    }
    
    public Subscription(String name, String url, int maxDownloads, OrderingPreference orderingPreference, boolean enabled) {
        this.name = name;
        this.url = url;
        this.maxDownloads = maxDownloads;
        this.orderingPreference = orderingPreference;
        this.enabled = enabled;
    }

    @Override
    public int compareTo(Subscription another) {
        return name.compareTo(another.name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "Subscription: url=" + url + " ; name="+ name + "; max=" + maxDownloads + " ; ordering=" + orderingPreference + " ; enabled=" + enabled;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(url);
        dest.writeInt(maxDownloads);
        dest.writeInt(orderingPreference.ordinal());
        dest.writeString(Boolean.toString(enabled));
    }

}
