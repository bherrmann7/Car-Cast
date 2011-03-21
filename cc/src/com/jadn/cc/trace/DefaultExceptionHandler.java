
package com.jadn.cc.trace;

import java.lang.Thread.UncaughtExceptionHandler;

import android.util.Log;

public class DefaultExceptionHandler implements UncaughtExceptionHandler {

	private UncaughtExceptionHandler defaultExceptionHandler;
	
	public DefaultExceptionHandler(UncaughtExceptionHandler pDefaultExceptionHandler)
	{
		defaultExceptionHandler = pDefaultExceptionHandler;
	}
	 
	// Default exception handler
	public void uncaughtException(Thread thread, Throwable throwable) {		
		TraceUtil.saveTrace(throwable);
		Log.e("CarCast", "DefaultExceptionHandler unpleasantness", throwable);
    	defaultExceptionHandler.uncaughtException(thread, throwable);
	}
	
	
}