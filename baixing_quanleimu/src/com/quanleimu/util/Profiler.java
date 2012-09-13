package com.quanleimu.util;

import java.util.Enumeration;
import java.util.Hashtable;

import android.util.Log;

/**
 * 
 * @author liuchong
 *
 */
public class Profiler {

	static class TimeRange
	{
		private long start;
		private long end;
		
		public long duration()
		{
			return end - start;
		}
	}
	
	private static Hashtable<String, TimeRange> eventMapper = new Hashtable<String, TimeRange>();
	
	public static void markStart(String tag)
	{
		if (!eventMapper.containsKey(tag))
		{
			eventMapper.put(tag, new TimeRange());
		}
		
		eventMapper.get(tag).start = System.currentTimeMillis();
		
	}
	
	public static void markEnd(String tag)
	{
		if (eventMapper.containsKey(tag))
		{
			eventMapper.get(tag).end = System.currentTimeMillis();
		}
	}
	
	public static void clear()
	{
		eventMapper.clear();
	}
	
	public static String dump()
	{
		StringBuffer buf = new StringBuffer();
		
		Enumeration<String> keys = eventMapper.keys();
		while (keys.hasMoreElements())
		{
			String key = keys.nextElement();
			buf.append(key).append(":").append(eventMapper.get(key).duration()).append(" | ");
		}
		
		Log.d("PROGILE", buf.toString());
		
		return buf.toString();
	}
	
	
}
