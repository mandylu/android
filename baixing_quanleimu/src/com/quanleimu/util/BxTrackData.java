package com.quanleimu.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;


public class BxTrackData implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
//	
	private HashMap<String, String> map;
	
	//constructor
	public BxTrackData(HashMap<String, String> map)
	{
		this.map = map;
	}

	
	public HashMap<String, String> getMap() {
		return map;
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
