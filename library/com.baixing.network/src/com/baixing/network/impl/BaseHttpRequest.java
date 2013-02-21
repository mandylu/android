package com.baixing.network.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


import android.util.Pair;


public abstract class BaseHttpRequest implements IHttpRequest {

	public static final String TAG = "BaseHttpRequest";
	
	protected String baseUrl;
	private Map<String, String> headers = new HashMap<String, String>();
	private boolean isCancel;
	
	protected BaseHttpRequest(String url) {
		this.baseUrl = url;
	}
	
	@Override
	public void addHeader(String name, String value) {
		headers.put(name, value);
	}
	
	@Override
	public String getUrl() {
		return baseUrl;
	}

	@Override
	public void removeHeader(String name) {
		headers.remove(name);
	}

	@Override
	public List<Pair<String, String>> getHeaders() {
		List<Pair<String, String>> list = new ArrayList<Pair<String, String>>();
		Iterator<String> it = headers.keySet().iterator();
		while (it.hasNext()) {
			String name = it.next();
			list.add(new Pair<String, String>(name, headers.get(name)));
		}
		
		return list;
	}

	@Override
	public void cancel() {
		this.isCancel = true;
	}

	@Override
	public boolean isCanceled() {
		return isCancel;
	}
	
	
}
