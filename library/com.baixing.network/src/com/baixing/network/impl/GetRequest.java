package com.baixing.network.impl;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import android.text.TextUtils;

/**
 * 
 * @author liuchong
 *
 * @date Feb 18, 2013
 */
public class GetRequest extends RestHttpRequest {
	private boolean useCache;
	public GetRequest(String baseUrl, Map<String, String> parameters, boolean useCache) {
		super(baseUrl, parameters);
		this.useCache = useCache;
	}

	
	public String getUrl() {
		String query = getFormatParams();
		if (TextUtils.isEmpty(query)) {
			return baseUrl;
		}
		
		String strUrl = baseUrl;
		URL url = null;
		try {
			url = new URL(strUrl);
		} catch (MalformedURLException e) {
			return baseUrl;
		}

		if (TextUtils.isEmpty(url.getQuery())) {
			if (strUrl.endsWith("?")) {
				strUrl = strUrl + query;
			} else {
				strUrl = strUrl + "?" + query;
			}
		} else {
			if (strUrl.endsWith("&")) {
				strUrl = strUrl + query;
			} else {
				strUrl = strUrl + "&" + query;
			}
		}
		
		return strUrl;
		
	}

	@Override
	public CACHE_POLICY getCachePolicy() {
		return useCache ? CACHE_POLICY.CACHE_PREF_CACHE : CACHE_POLICY.CACHE_ONLY_NEW;
	}

	@Override
	public void writeContent(OutputStream out) {
		//TODO: get method do not need write.
	}


	@Override
	public boolean isGetRequest() {
		return true;
	}
}
