package com.baixing.network.test.util;

import java.util.ArrayList;
import java.util.List;

import android.util.Pair;

import com.baixing.network.ICacheProxy;
import com.baixing.util.Util;

public class MemoCacheStub implements ICacheProxy {
	protected final List<Pair<String, String>> storeList = new ArrayList<Pair<String,String>>();
	
	public int size() {
		return storeList.size();
	}
	
	public boolean hasValue(String value) {
		for (Pair<String, String> p : storeList) {
			if (value.equals(p.second)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void onSave(String url, String data) {
		url = Util.extractUrlWithoutSecret(url);
		storeList.add(Pair.create(url, data));
	}
	
	public void clear() {
		storeList.clear();
	}

	@Override
	public String onLoad(String url) {
		url = Util.extractUrlWithoutSecret(url);
		
		for (Pair<String, String> p : storeList) {
			if (url.equals(p.first)) {
				return p.second;
			}
		}
		
		return null;
	}

}
