package com.quanleimu.util;

import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import com.quanleimu.util.TrackConfig.TrackMobile.Key;
/**
 * @author xuweiyan@baixing.com
 */
public class LogData {
	
	private HashMap<String, String> map;
	
	//constructor
	public LogData(HashMap<String, String> map)
	{
		this.map = map;
	}
	
	public HashMap<String, String> getMap() {
		return map;
	}
	
	public LogData append(Key key, int value)
	{
		String keyName = key.getName();
		this.map.put(keyName, value+"");
		return this;
	}
	
	public LogData append(Key key, String value)
	{
		String keyName = key.getName();
		this.map.put(keyName, value);
		return this;
	}
	
	public LogData append(Key key, float value)
	{
		String valueString;
		if (value == (int)value)
			valueString = (int)value + "";
		else
			valueString = value + "";
		String keyName = key.getName();
		this.map.put(keyName, valueString);
		return this;
	}
	
	public LogData append(Key key, double value)
	{
		String valueString;
		if (value == (int)value)
			valueString = (int)value + "";
		else
			valueString = value + "";
		String keyName = key.getName();
		this.map.put(keyName, valueString);
		return this;
	}
	
	public void end() {
		if (!TrackConfig.getInstance().getLoggingFlag()) return;
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
