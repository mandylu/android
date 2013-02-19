//liuchong@baixing.com
package com.baixing.util;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 * 
 * @author liuchong
 *
 */
public class ParameterHolder {

	private Hashtable<String, String> params = new Hashtable<String, String>();
	public ParameterHolder()
	{
		
	}
	
	public void addParamter(String key, String value)
	{
		params.put(key, value);
	}
	
	public void addParameter(String key, Object value)
	{
		if (value != null)
		{
			params.put(key, value.toString());
		}
	}
	
	public void addParameter(String key, int value)
	{
		params.put(key, value + "");
	}
	
	public void addParameter(String key, double value)
	{
		params.put(key, value + "");
	}
	
	public void addParameter(String key, long value)
	{
		params.put(key, value + "");
	}
	
	public void addParameter(String key, float value)
	{
		params.put(key, value + "");
	}
	
	public void addParameter(String key, short value)
	{
		params.put(key, value + "");
	}
	
	public void addParameter(String key, boolean value)
	{
		this.addParameter(key, Boolean.valueOf(value));
	}
	
	public List<String> toParameterList()
	{
		List<String> paramAsList = new ArrayList<String>();
		Enumeration<String> keys = params.keys();
		while (keys.hasMoreElements())
		{
			String key = keys.nextElement();
			paramAsList.add(key + "=" + URLEncoder.encode(params.get(key)));
		}
		
		return paramAsList;
	}
	
	

}
