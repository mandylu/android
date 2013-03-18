package com.baixing.network.impl;

import java.io.OutputStream;
import java.util.List;

import android.util.Pair;

/**
 * 
 * @author liuchong
 *
 * @date 2013-2-6
 */
public interface IHttpRequest {
	
	public static enum CACHE_POLICY {
		CACHE_PREF_CACHE, //get data from cache if it's already there.
		CACHE_ONLY_NEW, //Do not use cache.
		CACHE_NOT_CACHEABLE //Do not cache data for this request.
	}
	public String getUrl();
	public void addHeader(String name, String value);
	public void removeHeader(String name);
	public List<Pair<String, String>> getHeaders();
	public String getContentType();
	public boolean isGetRequest();
	/**
	 * write the http request content to the target output stream and return the content length.
	 * 
	 * @param out target output stream
	 * @return
	 */
	public int writeContent(OutputStream out);
	public CACHE_POLICY getCachePolicy();
	public void cancel();
	public boolean isCanceled();
}
