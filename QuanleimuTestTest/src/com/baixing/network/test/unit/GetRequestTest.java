package com.baixing.network.test.unit;

import java.util.HashMap;
import java.util.Map;

import com.baixing.network.impl.GetRequest;
import com.baixing.network.impl.IHttpRequest.CACHE_POLICY;

import android.test.AndroidTestCase;

public class GetRequestTest extends AndroidTestCase {

	
	public void testGetNoParam() {
		final String baseUrl = "http://www.baixing.com/mobile.ad_list/";
		Map<String, String> params = new HashMap<String, String>();
		GetRequest get  = new GetRequest(baseUrl, params, false);
		
		assertTrue(get.getUrl().startsWith(baseUrl));
		assertEquals(CACHE_POLICY.CACHE_ONLY_NEW, get.getCachePolicy());
		assertEquals("application/x-www-form-urlencoded", get.getContentType());
		
		assertEquals(get.getUrl(), baseUrl);
	}
	
	public void testGetWithParams() {
		final String baseUrl = "http://www.baixing.com/mobile.ad_list/";
		Map<String, String> params = new HashMap<String, String>();
		GetRequest get  = new GetRequest(baseUrl, params, false);
		
		params.put("key1", "value1");
		params.put("key2", "value2");
		get  = new GetRequest(baseUrl, params, false);
		assertTrue(get.getUrl().startsWith(baseUrl + "?"));
		assertEquals(CACHE_POLICY.CACHE_ONLY_NEW, get.getCachePolicy());
		assertEquals("application/x-www-form-urlencoded", get.getContentType());
		assertTrue(get.getUrl().contains("key1=value1"));
		assertTrue(get.getUrl().contains("key2=value2"));
	}
	
	public void testUseCache() {
		final String baseUrl = "http://www.baixing.com/mobile.ad_list/";
		Map<String, String> params = new HashMap<String, String>();
		GetRequest get  = new GetRequest(baseUrl, params, true);
		
		assertEquals(CACHE_POLICY.CACHE_PREF_CACHE, get.getCachePolicy());
	}
}
