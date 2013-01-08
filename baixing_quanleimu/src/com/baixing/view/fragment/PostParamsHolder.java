package com.baixing.view.fragment;

import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedHashMap;

import android.os.Bundle;

public final class PostParamsHolder implements Serializable {
	
	public static final String INVALID_VALUE = "INVALID_VALUE_KEY";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2515425925056832882L;
	static class ValuePair implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -6486097669248525239L;
		String uiValue;
		String value;
		ValuePair(String ui, String v){
			uiValue = ui;
			value = v;
		}
	}
	private LinkedHashMap<String, ValuePair> map;
	
	public int size(){
		return map.size();
	}
	
	public PostParamsHolder(){
		map = new LinkedHashMap<String, ValuePair>();
	}
	
	public void clear(){
		map.clear();
	}
	
	public void put(String key, String uiValue, String data){
		map.put(key, new ValuePair(uiValue, data));
	}
	
	public void remove(String key){
		map.remove(key);
	}
	
	public boolean containsKey(String key){
		return map.containsKey(key);
	}
	
	public Iterator<String> keyIterator(){
		return map.keySet().iterator();
	}
	
	public String getData(String key){
		if(map.containsKey(key)){
			return map.get(key).value;
		}
		return null;
	}
	
	public String getUiData(String key){
		if(map.containsKey(key)){
			return map.get(key).uiValue;
		}
		return null;
	}
	
	public void merge(PostParamsHolder params){
		if (params == null || params == this)
		{
			return;
		}
		map.putAll(params.map);
	}
	
	public String toUrlString(){
		StringBuffer result = new StringBuffer();
		Iterator<String> keys = map.keySet().iterator();
		while(keys.hasNext())
		{
			String key = keys.next();
			if (INVALID_VALUE.equals(map.get(key).value))
			{
				continue;
			}
			
			// keyword 单独处理，放到ad_list的keyword参数里。
			if (!"".equals(key))
			{
//				result.append(" AND ")
//				.append(URLEncoder.encode(key)).append(":")
//				.append(URLEncoder.encode(map.get(key).value));
				result.append(" AND ")
				.append(key).append(":")
				.append(map.get(key).value);
			}
		}
		
		if (result.length() > 4)
		{
			result.replace(0, 4, "");
		}
		return result.toString();
		
	}
	
}
