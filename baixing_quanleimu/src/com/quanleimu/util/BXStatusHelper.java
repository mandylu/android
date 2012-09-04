package com.quanleimu.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class BXStatusHelper {

	public static final String TYPE_CALL = "call";
	public static final String TYPE_AD_VIEW = "vad";
	public static final String TYPE_ADD_CONTACT = "addcontact";
	public static final String TYPE_SMS_SEND = "sms";
	public static final String TYPE_SIXIN_SEND = "sixin";
	public static final String TYPE_WEIBO_SEND = "weibo";
	public static final String TYPE_WEIXIN_SEND = "weixin";
	
	
	public static final String SERIALIZABLE_PATH = "bx_status.ser";
	
	private List<BXStatus> statusList;
	
	public static final int SEND_POINT = 50;
	
	
	private BXStatusHelper()
	{
		BXStatus[] array = new BXStatus[] {
			new BXStatus(TYPE_CALL, 0), new AdViewStatus(TYPE_AD_VIEW, 0),
			new BXStatus(TYPE_ADD_CONTACT, 0), new BXStatus(TYPE_SMS_SEND, 0),
			new BXStatus(TYPE_SIXIN_SEND, 0), new BXStatus(TYPE_WEIBO_SEND, 0),
			new BXStatus(TYPE_WEIXIN_SEND, 0)
		};
		
		statusList = new ArrayList<BXStatus>();
		for (BXStatus s : array)
		{
			statusList.add(s);
		}
	}
	
	private static BXStatusHelper instance;

	public static BXStatusHelper getInstance()
	{
		if(instance == null)
		{
			instance = new BXStatusHelper();
		}
		
		return instance;
	}
	
	public void load(Context context)
	{
		Object data = Util.loadDataFromLocate(context, SERIALIZABLE_PATH);
		if (data != null)
		{
			ArrayList<BXStatus> listData = (ArrayList) data;
			
			if(statusList.size() == 0) //Assign
			{
				statusList = listData;
				return;
			}
			
			mergeList(listData);
		}
		Util.clearData(context, SERIALIZABLE_PATH);
	}
	
	public void clearData()
	{
		for (BXStatus s : statusList)
		{
			s.clear();
		}
	}
	
	public void store(Context context)
	{
		Util.saveDataToLocate(context, SERIALIZABLE_PATH, statusList);
	}
	
	public void increase(String type, Object eventObj)
	{
		BXStatus s = findStatus(type);
		if (s == null)
		{
			return;
		}
		
		s.increase(eventObj);
		
		if (totalCount() > SEND_POINT)
		{
			List<BXStatus> sendList = prepareSendData();
			send(sendList);
		}
	}
	
	private List<BXStatus> prepareSendData()
	{
		List<BXStatus> sending = new ArrayList<BXStatus>();
		for (BXStatus s : statusList)
		{
			sending.add((BXStatus) s.clone());
			s.clear();
		}
		
		return sending;
	}
	
	public BXStatus findStatus(String type)
	{
		for (BXStatus s : statusList)
		{
			if (s.getTypeName().equals(type))
			{
				return s;
			}
		}
		
		return null;
	}
	
	private int totalCount()
	{
		int t = 0;
		for (BXStatus s : statusList)
		{
			t+=s.getCount();
		}
		
		return t;
	}
	
	private void mergeList(List<BXStatus> listData)
	{
		for (int i=0; i<statusList.size(); i++)//Merge
		{
			BXStatus outer = statusList.get(i);
			for (int j=0; j<listData.size(); j++)
			{
				BXStatus inner = listData.get(j);
				if (outer.getTypeName().equals(inner.getTypeName()))
				{
					outer.merge(inner);
					listData.remove(j);
					break;
				}
			}
		}
	}
	
	private void send(final List<BXStatus> data)
	{
		ParameterHolder params = new ParameterHolder();
		for (BXStatus d : data)
		{
			params.addParameter(d.getTypeName(), d.description());
		}
		
		Communication.executeAsyncPostTask("stats", params, new Communication.CommandListener() {
			
			@Override
			public void onServerResponse(String serverMessage) {
				//Do nothing.
				System.out.println("do nothing.");
			}
			
			@Override
			public void onException(Exception ex) {
				mergeList(data);
			}
		});
		
	}
	
	
}
