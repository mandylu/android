package com.baixing.network.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import android.util.Log;

/**
 * 
 * @author liuchong
 *
 * @date Feb 18, 2013
 */
public class PostRequest extends RestHttpRequest {

	public PostRequest(String baseUrl, Map<String, String> parameters) {
		super(baseUrl, parameters);
	}

	@Override
	public CACHE_POLICY getCachePolicy() {
		return CACHE_POLICY.CACHE_NOT_CACHEABLE;
	}

	@Override
	public boolean isGetRequest() {
		return false;
	}

	@Override
	public void writeContent(OutputStream out) {
		String params = getFormatParams();
		
		if (params == null) {
			return;
		}
		
		try {
			out.write(params.getBytes());
			out.flush();
		} catch (IOException e) {
			Log.d(TAG, "exception when write data to outputstream." + e.getMessage());
		}
	}

}
