package com.jadn.cc.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import android.media.MediaPlayer;
import android.util.Log;

import com.jadn.cc.trace.TraceUtil;

/**
 * Meta information about a podcast. From rss metadata (hopefully someday from id3tags as well.)
 */
public class MetaFile {

	File file;
	Properties properties = new Properties();
        private static final String defaultBaseFilename = "0";
	
	String getFilename(){
		return file.getName();
	}

	MetaFile(File file) {
		this.file = file;

		File metaFile = getMetaPropertiesFile();
		if (metaFile.exists()) {
			try {
				properties.load(new FileInputStream(metaFile));
				// Log.i("metafile", properties.toString());
			} catch (Exception e) {
				Log.e("Meta", "Can't load properties");
			}
		} else {
			properties.setProperty("title", file.getName());
			properties.setProperty("feedName", "unknown feed");
			properties.setProperty("currentPos", "0");
			computeDuration();
			save();
		}
	}

        // It would be better to just store this in the metadata!
        //
        // Take a filename of either the form
        //    "...../XXXX:00:YYYY.mp3"
        //    "...../XXXX.mp3"
        // and return just "XXXX".
        //
        public String getBaseFilename()
        {
           String name = getFilename();
           if ( name == null ) return defaultBaseFilename;
	   Log.d("CarCast", "getBaseFilename " + name);

           // Find start of base file name.
           int slashIndex = name.lastIndexOf('/');
           // slashIndex is -1 if no slash is present, which works perfactly below!
	   Log.d("CarCast", "getBaseFilename " + name.substring(slashIndex + 1));

           // Find end of base file name.
           int i = slashIndex + 1;
           while ( i < name.length() && Character.isDigit(name.charAt(i)) )
              i += 1;
	   Log.d("CarCast", "getBaseFilename " + i);
           if ( name.length()  <= i ) return defaultBaseFilename;
           if ( slashIndex + 1 == i ) return defaultBaseFilename;

	   Log.d("CarCast", "getBaseFilename " + name.substring(slashIndex + 1, i));
           return name.substring(slashIndex + 1, i);
        }

	public void computeDuration() {
		// ask media player
		MediaPlayer mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(file.toString());
			mediaPlayer.prepare();
			setDuration(mediaPlayer.getDuration());
		} catch (Exception e) {
			TraceUtil.report(new RuntimeException("on file " + file, e));
			setDuration(0);
		} finally {
			mediaPlayer.reset();
			mediaPlayer.release();
		}

	}

	public MetaFile(MetaNet metaNet, File castFile) {
		file = castFile;
		properties = metaNet.properties;
		computeDuration();
	}

	public void delete() {
		file.delete();
		getMetaPropertiesFile().delete();
	}

	public int getCurrentPos() {
		if (properties.getProperty("currentPos") == null)
			return 0;
		return Integer.parseInt(properties.getProperty("currentPos"));
	}

	public int getDuration() {
		if (properties.get("duration") == null)
			return -1;
		return Integer.parseInt(properties.getProperty("duration"));
	}

	public String getFeedName() {
		if (properties.get("feedName") == null)
			return "unknown";
		return properties.get("feedName").toString();
	}

	private File getMetaPropertiesFile() {
		// check for metadata
		String name = file.getName();
		int lastDot = name.lastIndexOf('.');
		if (lastDot != -1) {
			name = name.substring(0, lastDot);
		}
		name += ".meta";
		return new File(file.getParent(), name);
	}

	public String getTitle() {
		if (properties.get("title") == null) {
			String title = file.getName();
			int lastDot = title.lastIndexOf('.');
			return title.substring(0, lastDot);
		}
		return properties.get("title").toString();
	}

	public String getUrl() {
		return properties.getProperty("url");
	}

	public void save() {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(getMetaPropertiesFile());
			properties.save(fos, "");
			fos.close();
		} catch (Throwable e) {
			Log.e("MetaFile", "saving meta data", e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException io) {
				}
			}
		}

	}

	public void setCurrentPos(int i) {
		properties.setProperty("currentPos", Integer.toString(i));
		if (getDuration() == -1)
			return;
		if (i > getDuration() * .9) {
			setListenedTo();
		}
	}

	public void setDuration(int duration) {
		properties.setProperty("duration", Integer.toString(duration));
	}

	public void setListenedTo() {
		properties.setProperty("listenedTo", "true");
	}

	public boolean isListenedTo() {
		return properties.getProperty("listenedTo" ) != null;
	}
	
	public String getDescription(){
		return properties.getProperty("description");
	}

}
