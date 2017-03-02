
package com.jadn.cc.trace;

public class TraceData {
	public static String ANDROID_VERSION = "unknown";
	public static String APP_PACKAGE = "unknown";
	public static String APP_VERSION = "unknown";
	// This must be set by the application - it used to automatically
	// transmit exceptions to the trace server
	public static String FILES_PATH = null;
	public static String PHONE_MODEL = "unknown";

	public static String TraceVersion = "0.3.0";

	// Where are the stack traces posted?
	public static String URL = "http://jadn.com/carcast/remoteError/save";
	static {
		if (false) {
			URL = "http://192.168.0.128:9090/carcast/remoteError/save";
		}
	}

}