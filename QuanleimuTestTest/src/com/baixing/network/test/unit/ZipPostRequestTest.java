package com.baixing.network.test.unit;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.baixing.network.impl.PostRequest;
import com.baixing.network.impl.IHttpRequest.CACHE_POLICY;
import com.baixing.network.impl.ZippedPostRequest;

import android.test.AndroidTestCase;

public class ZipPostRequestTest extends AndroidTestCase {
	public void testPostBlankUrl() {
		final String baseUrl = "http://www.baixing.com/mobile.user_login";
		Map<String,String> params = new HashMap<String, String>();
		ZippedPostRequest post = new ZippedPostRequest(baseUrl, params);
		
		assertEquals(baseUrl, post.getUrl());
		assertEquals(CACHE_POLICY.CACHE_NOT_CACHEABLE, post.getCachePolicy());
		assertEquals("application/zip", post.getContentType());
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		post.writeContent(out);
		assertEquals(0,  out.size());
	}
	
	public void testPostWithParams() throws IOException {
		final String baseUrl = "http://www.baixing.com/mobile.user_login";
		Map<String,String> params = new HashMap<String, String>();
		params.put("key1", "value1");
		params.put("key2", "value2");
		
		ZippedPostRequest post = new ZippedPostRequest(baseUrl, params);
		assertEquals(baseUrl, post.getUrl());
		assertEquals(CACHE_POLICY.CACHE_NOT_CACHEABLE, post.getCachePolicy());
		assertEquals("application/zip", post.getContentType());
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		post.writeContent(out);
		
		byte[] unzipBuffer = new byte[2048];
		ByteArrayOutputStream unzipOut = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());   
		GZIPInputStream gin = new GZIPInputStream(in);
		int count =  gin.read(unzipBuffer);
		unzipOut.write(unzipBuffer, 0, count);
		
		String outBuf = unzipOut.toString("ISO-8859-1"); //API1.0......use this enconding.
		
		assertTrue(outBuf.contains("key1=value1"));
		assertTrue(outBuf.contains("key2=value2"));
		assertTrue(outBuf.contains("&"));
	}
}
