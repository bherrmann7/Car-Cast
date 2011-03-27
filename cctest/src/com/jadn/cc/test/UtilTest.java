package com.jadn.cc.test;


import junit.framework.TestCase;

import com.jadn.cc.core.Util;

public class UtilTest extends TestCase {

	public void testGetCharset() {
		assertEquals("UTF-8", Util.getCharset("html/plain"));
		assertEquals("ISO-8859-1", Util
				.getCharset("application/xml; charset=ISO-8859-1"));
		assertEquals("UTF-8", Util.getCharset(null));
	}
}
