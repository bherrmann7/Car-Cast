package com.jadn.cc.trace;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import android.util.Log;

public class TraceUtil {

	public static void report(Throwable e) {
		saveTrace(e);
		ExceptionHandler.clearStackTraceCache();
		ExceptionHandler.submitStackTraces();
	}

	public static void saveTrace(Throwable e) {
		try {
			final Writer result = new StringWriter();
			long traceTime = System.currentTimeMillis();
			e.printStackTrace(new PrintWriter(result));
			String filename = TraceData.APP_VERSION + "-" + traceTime;
			BufferedWriter bos = new BufferedWriter(new FileWriter(TraceData.FILES_PATH + "/" + filename + ".stacktrace"));
			bos.write(Long.toString(traceTime));
			bos.write('\n');
			bos.write(result.toString());
			bos.close();
		} catch (Exception ebos) {
			Log.e(TraceUtil.class.getName(), "Unable to save trace.", ebos);
		}

	}

}
