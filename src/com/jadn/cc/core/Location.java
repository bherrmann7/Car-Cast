package com.jadn.cc.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Location {

	public Location(String title, int pos) {
		super();
		this.title = title;
		this.pos = pos;
	}

	public String title;
	public int pos;

	public static Location load(File stateFile) throws IOException {
		try {
			Properties prop = new Properties();
			FileInputStream fis = new FileInputStream(stateFile);
			prop.load(fis);
			fis.close();

			return new Location(prop.get("title").toString(),
					atoi(prop, "pos"));
		} catch (Throwable t) {
			stateFile.delete();
			return null;
		}
	}

	private static int atoi(Properties prop, String string) {
		return Integer.parseInt(prop.get(string).toString());
	}

	public static Location save(File stateFile, String title, int pos, int duration)
			throws IOException {
		Properties prop = new Properties();
		prop.setProperty("title", title);
		prop.setProperty("pos", Integer.toString(pos));
		FileOutputStream fos = new FileOutputStream(stateFile);
		prop.save(fos, "");
		fos.close();
		return new Location(title,pos);
	}

}
