package com.baixing.network.test.unit;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import com.baixing.network.impl.PostRequest;
import com.baixing.network.impl.IHttpRequest.CACHE_POLICY;

import android.test.AndroidTestCase;

public class PostRequstTest extends AndroidTestCase {
	public void testPostBlankUrl() {
		final String baseUrl = "http://www.baixing.com/mobile.user_login";
		Map<String,String> params = new HashMap<String, String>();
		PostRequest post = new PostRequest(baseUrl, params);
		
		assertEquals(baseUrl, post.getUrl());
		assertEquals(CACHE_POLICY.CACHE_NOT_CACHEABLE, post.getCachePolicy());
		assertEquals("application/x-www-form-urlencoded", post.getContentType());
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		post.writeContent(out);
		assertEquals(0,  out.size());
	}
	
	public void testPostWithParams() {
		final String baseUrl = "http://www.baixing.com/mobile.user_login";
		Map<String,String> params = new HashMap<String, String>();
		params.put("key1", "value1");
		params.put("key2", "value2");
		
		PostRequest post = new PostRequest(baseUrl, params);
		assertEquals(baseUrl, post.getUrl());
		assertEquals(CACHE_POLICY.CACHE_NOT_CACHEABLE, post.getCachePolicy());
		assertEquals("application/x-www-form-urlencoded", post.getContentType());
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		post.writeContent(out);
		String outBuf = out.toString();
		assertTrue(outBuf.contains("key1=value1"));
		assertTrue(outBuf.contains("key2=value2"));
		assertTrue(outBuf.contains("&"));
	}
}
