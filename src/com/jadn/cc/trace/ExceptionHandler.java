
/** originally used the nullwire code, tweaked out for carcast. */

package com.jadn.cc.trace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class ExceptionHandler {

	private static boolean busySendingTraces;

	private static String[] stackTraceFileList = null;

	public static void clearStackTraceCache() {
		stackTraceFileList = null;
	}

	public static boolean register(Context context) {
		PackageManager pm = context.getPackageManager();
		try {
			PackageInfo pi;
			// Version
			pi = pm.getPackageInfo(context.getPackageName(), 0);
			TraceData.APP_VERSION = pi.versionName;
			// Package name
			TraceData.APP_PACKAGE = pi.packageName;
			// Files dir for storing the stack traces
			TraceData.FILES_PATH = context.getFilesDir().getAbsolutePath();
			// Device model
			TraceData.PHONE_MODEL = android.os.Build.MODEL;
			// Android version
			TraceData.ANDROID_VERSION = android.os.Build.VERSION.RELEASE;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		boolean stackTracesFound = false;
		// We'll return true if any stack traces were found
		if (searchForStackTraces().length > 0) {
			stackTracesFound = true;
		}

		// we register from our application and also from our service.
		// lets not send stacktraces twice.
		if (!busySendingTraces) {
			busySendingTraces = true;
			new Thread() {
				@Override
				public void run() {
					// First of all transmit any stack traces that may be lying
					// around
					submitStackTraces();
					UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
					if (currentHandler != null) {
						Log.d(ExceptionHandler.class.getName(), "current handler class=" + currentHandler.getClass().getName());
					}
					// don't register again if already registered
					if (!(currentHandler instanceof DefaultExceptionHandler)) {
						// Register default exceptions handler
						Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(currentHandler));
					}
					busySendingTraces = false;
				}
			}.start();
		}

		return stackTracesFound;
	}


	private static String[] searchForStackTraces() {
		if (stackTraceFileList != null) {
			return stackTraceFileList;
		}
		File dir = new File(TraceData.FILES_PATH + "/");
		// Try to create the files folder if it doesn't exist
		dir.mkdir();
		// Filter for ".stacktrace" files
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".stacktrace");
			}
		};
		return (stackTraceFileList = dir.list(filter));
	}

	public static void submitStackTraces() {
		try {
			String[] list = searchForStackTraces();
			if (list != null && list.length > 0) {
				for (int i = 0; i < list.length; i++) {
					String filePath = TraceData.FILES_PATH + "/" + list[i];
					// Extract the version from the filename:
					// "packagename-version-...."
					String version = list[i].split("-")[0];
					// Read contents of stacktrace
					StringBuilder contents = new StringBuilder();
					BufferedReader input = new BufferedReader(new FileReader(filePath));
					String traceTime = input.readLine();
					String line = null;					
					while ((line = input.readLine()) != null) {
						contents.append(line);
						contents.append('\n');
					}
					input.close();
					String stacktrace;
					stacktrace = contents.toString();
					Log.d(ExceptionHandler.class.getName(), "Transmitting stack trace: " + stacktrace);
					// Transmit stack trace with POST request
					DefaultHttpClient httpClient = new DefaultHttpClient();
					HttpPost httpPost = new HttpPost(TraceData.URL);
					List<NameValuePair> nvps = new ArrayList<NameValuePair>();
					nvps.add(new BasicNameValuePair("traceTime", traceTime));
					nvps.add(new BasicNameValuePair("now", Long.toString(System.currentTimeMillis())));
					nvps.add(new BasicNameValuePair("package_name", TraceData.APP_PACKAGE));
					nvps.add(new BasicNameValuePair("package_version", version));
					nvps.add(new BasicNameValuePair("phone_model", TraceData.PHONE_MODEL));
					nvps.add(new BasicNameValuePair("android_version", TraceData.ANDROID_VERSION));
					nvps.add(new BasicNameValuePair("stacktrace", stacktrace));
					httpPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
					// We don't care about the response, so we just hope it went
					// well and on with it
					httpClient.execute(httpPost);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				String[] list = searchForStackTraces();
				for (int i = 0; i < list.length; i++) {
					File file = new File(TraceData.FILES_PATH + "/" + list[i]);
					file.delete();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
