package com.jadn.cc.core;

import android.os.Parcel;
import android.os.Parcelable;

public class Subscription implements Parcelable, Comparable<Subscription> {

    public static final Parcelable.Creator<Subscription> CREATOR = new Parcelable.Creator<Subscription>() {
         public Subscription createFromParcel(Parcel in) {
             return new Subscription(in.readString(),   // name
                                     in.readString(),   // URL
                                     in.readInt(),      // max count
                                     OrderingPreference.values()[in.readInt()]); // order pref
         }

         public Subscription[] newArray(int size) {
             return new Subscription[size];
         }
     };
    public final int                maxDownloads;
    public final String             name;
    public final OrderingPreference orderingPreference;

    public final String             url;

    public Subscription(String name, String url) {
        this(name, url, -1, OrderingPreference.FIFO);
    }

    public Subscription(String name, String url, int maxDownloads, OrderingPreference orderingPreference) {
        this.name = name;
        this.url = url;
        this.maxDownloads = maxDownloads;
        this.orderingPreference = orderingPreference;
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
        return "Subscription: url=" + url + " ; name="+ name + "; max=" + maxDownloads + " ; ordering=" + orderingPreference;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(url);
        dest.writeInt(maxDownloads);
        dest.writeInt(orderingPreference.ordinal());
    }

}
