package com.quanleimu.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.json.JSONException;
import org.json.JSONObject;


public class BXTrackDataObject implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected String trackType;
	protected String city;
	protected String timeStamp;
	protected HashMap<String, String> others;
	
	public BXTrackDataObject(String trackType, String city, String timeStamp,HashMap<String, String> other)
	{
		this.trackType = trackType;
		this.city = city;
		this.timeStamp = timeStamp;
		this.others = other;
	}

	public String getTrackType() {
		return trackType;
	}

	public String getCity() {
		return city;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public HashMap<String, String> getOther() {
		return others;
	}
	
	public JSONObject toJsonObject()
	{
		JSONObject jObj = new JSONObject();
		try {
			jObj.put("trackType", this.trackType);
			jObj.put("city", this.city);
			jObj.put("timeStamp", this.timeStamp);
			for(Entry<String, String> entry : others.entrySet())
			{
				jObj.put(entry.getKey(), entry.getValue());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return jObj;
	}
	
}
