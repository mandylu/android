package com.quanleimu.util;

public class AdViewStats extends BXStats {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1462792102773629750L;
	
	public AdViewStats(String name, int initialCount) {
		super(name, initialCount);
	}
	
	public String description()
	{
		StringBuffer buf = new StringBuffer();
		for (Object d : events)
		{
			buf.append(d).append(",");
		}
		
		return buf.toString();
	}


	public Object clone()
	{
		BXStats newObj = new AdViewStats(statusType, count);
		if (this.events != null && this.events.size() > 0)
		{
			newObj.events.addAll(this.events);
		}
		
		return newObj;
	}
}
