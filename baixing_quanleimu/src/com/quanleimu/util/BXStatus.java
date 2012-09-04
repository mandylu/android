package com.quanleimu.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BXStatus implements Serializable, Cloneable {

//	public static enum BX_STATUS_TYPE {
//		STATUS_AD_VIEW("vad"),
//		STATUS_CALL("call"),
//		STATUS_CONTACT_ADD("addcontact"),
//		STATUS_SMS_SEND("sms"),
//		STATUS_TALK_SEND("sixin"),
//		STATUS_WEIBO_SEND("weibo"),
//		STATUS_WEIXIN_SEND("weixin"),
//		STATUS_TALK_SEND_ERROR("");
//		
//		
//		private String name;
//		BX_STATUS_TYPE(String name) {
//			this.name = name;
//		}
//		
//		public String getName() {
//			return name;
//		}
//	};
	
	private String statusType;
	private int count;
	protected List events = new ArrayList();
	
	public BXStatus(String name, int initialCount)
	{
		this.statusType = name;
		if (initialCount > 0)
		{
			this.count = initialCount;
		}
	}
	
	public void increase(Object event)
	{
		count++;
		if (event != null)
		{
			this.events.add(event);
		}
	}
	
	public String getTypeName()
	{
		return this.statusType;
	}
	
	public int getCount()
	{
		return this.count;
	}
	
	public List getEventObject()
	{
		return this.events;
	}
	
	public void clear()
	{
		this.count = 0;
		this.events.clear();
	}
	
	public void merge(BXStatus outer)
	{
		if (outer != null && getTypeName().equals(outer.getTypeName()))
		{
			this.count += outer.getCount();
			if (outer.getEventObject() != null)
			{
				this.events.addAll(outer.getEventObject());
			}
		}
	}
	
	public String description()
	{
		return this.count + "";
	}
	
	public Object clone()
	{
		BXStatus newObj = new BXStatus(this.statusType, this.count);
		if (this.events != null && this.events.size() > 0)
		{
			newObj.events.addAll(this.events);
		}
		
		return newObj;
	}
}
