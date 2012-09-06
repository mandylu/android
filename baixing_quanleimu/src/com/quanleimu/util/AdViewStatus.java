package com.quanleimu.util;

public class AdViewStatus extends BXStatus {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1462792102773629750L;
	
	public AdViewStatus(String name, int initialCount) {
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
		BXStatus newObj = new AdViewStatus(statusType, count);
		if (this.events != null && this.events.size() > 0)
		{
			newObj.events.addAll(this.events);
		}
		
		return newObj;
	}
}
