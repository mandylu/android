package com.quanleimu.util;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class BXStatsHelper {

	public static final String TYPE_CALL = "call";
	public static final String TYPE_AD_VIEW = "vad";
	public static final String TYPE_ADD_CONTACT = "addcontact";
	public static final String TYPE_SMS_SEND = "sms";
	public static final String TYPE_SIXIN_SEND = "sixin";
	public static final String TYPE_WEIBO_SEND = "weibo";
	public static final String TYPE_WEIXIN_SEND = "weixin";

	private static final String TYPE_ADS_ID = "adIds";
	
	public static final String SERIALIZABLE_PATH = "bx_status.ser";
	
	private List<BXStats> statusList;
	
	public static final int SEND_POINT = 50;
	
	
	private BXStatsHelper()
	{
		BXStats[] array = new BXStats[] {
			new BXStats(TYPE_CALL, 0), new BXStats(TYPE_AD_VIEW, 0),
			new BXStats(TYPE_ADD_CONTACT, 0), new BXStats(TYPE_SMS_SEND, 0),
			new BXStats(TYPE_SIXIN_SEND, 0), new BXStats(TYPE_WEIBO_SEND, 0),
			new BXStats(TYPE_WEIXIN_SEND, 0), new AdViewStats(TYPE_ADS_ID, 0)
		};
		
		statusList = new ArrayList<BXStats>();
		for (BXStats s : array)
		{
			statusList.add(s);
		}
	}
	
	private static BXStatsHelper instance;

	public static BXStatsHelper getInstance()
	{
		if(instance == null)
		{
			instance = new BXStatsHelper();
		}
		
		return instance;
	}
	
	public void load(Context context)
	{
		Object data = Util.loadDataFromLocate(context, SERIALIZABLE_PATH);
		if (data != null)
		{
			ArrayList<BXStats> listData = (ArrayList) data;
			
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
		for (BXStats s : statusList)
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
		BXStats s = findStatus(type);
		if (s == null)
		{
			return;
		}
		
		if (TYPE_AD_VIEW.equals(type))
		{
			s.increase(null); //Do not need record event for vad count.
			BXStats ssub = findStatus(TYPE_ADS_ID);
			if (ssub != null)
			{
				ssub.increase(eventObj);
			}
		}
		else
		{
			s.increase(eventObj);
		}
		
		if (totalCount() > SEND_POINT)
		{
			send();
		}
	}
	
	private List<BXStats> prepareSendData()
	{
		List<BXStats> sending = new ArrayList<BXStats>();
		for (BXStats s : statusList)
		{
			sending.add((BXStats) s.clone());
			s.clear();
		}
		
		return sending;
	}
	
	public BXStats findStatus(String type)
	{
		for (BXStats s : statusList)
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
		BXStats s = findStatus(TYPE_AD_VIEW);
		if (s != null) {
			t += s.getCount();
		}
		
		return t;
	}
	
	private void mergeList(List<BXStats> listData)
	{
		for (int i=0; i<statusList.size(); i++)//Merge
		{
			BXStats outer = statusList.get(i);
			for (int j=0; j<listData.size(); j++)
			{
				BXStats inner = listData.get(j);
				if (outer.getTypeName().equals(inner.getTypeName()))
				{
					outer.merge(inner);
					listData.remove(j);
					break;
				}
			}
		}
	}
	
	public void send()
	{
		if (totalCount() < 0) //do not send if no log.
		{
			return;
		}
		
		final List<BXStats> data = prepareSendData();
		ParameterHolder params = new ParameterHolder();
		for (BXStats d : data)
		{
			params.addParameter(d.getTypeName(), d.description());
		}
		
		Communication.executeAsyncPostTask("stats", params, new Communication.CommandListener() {
			
			@Override
			public void onServerResponse(String serverMessage) {
				//Do nothing.
			}
			
			@Override
			public void onException(Exception ex) {
				mergeList(data);
			}
		});
		
	}
	
	
}
