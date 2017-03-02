package com.jadn.cc.core;

import android.os.Environment;

public enum ExternalMediaStatus {
    readable, unavailable, writeable;

    public static ExternalMediaStatus getExternalMediaStatus() {
        // adapted from
        // http://developer.android.com/guide/topics/data/data-storage.html#filesExternal
        String state = Environment.getExternalStorageState();
    
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            return writeable;
    
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            return readable;
    
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            return unavailable;
        }
    }
}
