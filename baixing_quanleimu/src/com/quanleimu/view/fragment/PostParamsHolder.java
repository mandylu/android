package com.quanleimu.view.fragment;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;

import android.os.Bundle;

public final class PostParamsHolder implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2515425925056832882L;
	private LinkedHashMap<String, String> uiMap;
	private LinkedHashMap<String, String> postValuemap;		
	
	public PostParamsHolder()
	{
		postValuemap = new LinkedHashMap<String, String>();
		uiMap = new LinkedHashMap<String, String>();
	}
	
	public void clear()
	{
		this.uiMap.clear();
		this.postValuemap.clear();
	}
	
	public void put(String key, String uiValue, String data)
	{
		this.uiMap.put(key, uiValue);
		this.postValuemap.put(key, data);
	}
	
	public void remove(String key)
	{
		this.uiMap.remove(key);
		this.postValuemap.remove(key);
	}
	
	public LinkedHashMap<String, String> getData()
	{
		return this.postValuemap;
	}
	
	public LinkedHashMap<String, String> getUiData()
	{
		return this.uiMap;
	}
	
	public boolean containsKey(String key)
	{
		return postValuemap.containsKey(key);
	}
	
	public Iterator keyIterator()
	{
		return postValuemap.keySet().iterator();
	}
	
	public String getData(String key)
	{
		return postValuemap.get(key);
	}
	
	public String getUiData(String key)
	{
		return uiMap.get(key);
	}
	
	public void merge(PostParamsHolder params)
	{
		if (params == null || params == this)
		{
			return;
		}
		
		this.uiMap.putAll(params.uiMap);
		this.postValuemap.putAll(params.postValuemap);
		
	}
	
}