package com.quanleimu.view;

import java.util.HashMap;

public class AdViewHistory {

	private static final Integer R = new Integer(1);
	
	private static AdViewHistory instance;
	private HashMap<String, Integer> mapper;
	
	private AdViewHistory() {
		mapper = new HashMap<String, Integer>();
	}
	
	public static AdViewHistory getInstance() {
		if (instance == null)
		{
			instance = new AdViewHistory();
		}
		
		return instance;
	}
	
	public void markRead(String vadId) {
		mapper.put(vadId, R);
	}
	
	public boolean isReaded(String vadId) {
		return mapper.containsKey(vadId);
	}
	
	public void clearHistory() {
		mapper.clear();
	}
}
