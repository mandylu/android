package com.quanleimu.util;

import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;
/**
 * @author xuweiyan@baixing.com
 */
public class Log {
	
	private HashMap<String, String> map;
	
	//constructor
	public Log(HashMap<String, String> map)
	{
		this.map = map;
	}
	
	public HashMap<String, String> getMap() {
		return map;
	}
	
	public Log append(String key, String value)
	{
		this.map.put(key, value);
		return this;
	}
	
	public void end() {
		Tracker.getInstance().addLog(this);
	}
	
	public JSONObject toJsonObj() {
		JSONObject jobj = new JSONObject();
		try {
			for (Entry<String,String> entry : map.entrySet()) {
				jobj.put(entry.getKey(), entry.getValue());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}		
		return jobj;
	}
}
