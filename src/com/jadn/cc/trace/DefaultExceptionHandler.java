
package com.jadn.cc.trace;

import java.lang.Thread.UncaughtExceptionHandler;

public class DefaultExceptionHandler implements UncaughtExceptionHandler {

	private UncaughtExceptionHandler defaultExceptionHandler;
	
	public DefaultExceptionHandler(UncaughtExceptionHandler pDefaultExceptionHandler)
	{
		defaultExceptionHandler = pDefaultExceptionHandler;
	}
	 
	// Default exception handler
	public void uncaughtException(Thread thread, Throwable throwable) {		
		TraceUtil.saveTrace(throwable);
    	defaultExceptionHandler.uncaughtException(thread, throwable);        
	}
	
	
}