package com.quanleimu.activity.test;

import java.io.ByteArrayOutputStream;

import com.baixing.network.NetworkProfiler;

import android.test.AndroidTestCase;

public class NetworkProfilerTest extends AndroidTestCase {

	public void testProfile() throws InterruptedException {
		NetworkProfiler.endable("");
		String url = "http://www.baixing.com/testurl";
		NetworkProfiler.startUrl(url);
		
		Thread.sleep(1000);
		
		NetworkProfiler.endUrl(url, 10, 1000, null);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		NetworkProfiler.writeTo(os, true);
		
		String result = os.toString();
		assertTrue(result.startsWith(url));
		assertTrue(result.endsWith("    \r\n"));
		assertTrue(result.contains("    1000    "));
	}
	
}
