package com.quanleimu.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.quanleimu.activity.QuanleimuApplication;

//singleton
public class BxTracker {
	private static final String SERIALIZABLE_TRACKER_FILE = "bx_tracker.ser";//记录文件
	private List<BxTrackData> dataList = null;
	private static BxTracker instance = null;
	public static BxTracker getInstance()
	{
		if (instance == null)// && BxMobileConfig.getInstance().getLoggingFlag() == true
		{
			instance = new BxTracker();
		}
		return instance;
	}
	private BxTracker()//构造器
	{
		dataList = new ArrayList<BxTrackData>();
	}
	
	public void endLog(BxTrackData data) {
		addTrackData(data);
	}
	
	public BxTrackData createPageLogData(String key, String value) {
		BxTrackData data = new BxTrackData(new HashMap<String, String>());
		data.appendProperty("tracktype", "pageview");
		return data.appendProperty(key, value);
	}
	
	public BxTrackData createEventLogData(String key, String value) {
		BxTrackData data = new BxTrackData(new HashMap<String, String>());
		data.appendProperty("tracktype", "event");
		return data.appendProperty(key, value);
	}
	
	//添加记录
	private void addTrackData(BxTrackData trackobj)
	{
		dataList.add(trackobj);
		if (dataList.size()>100) {//设置100次,写一组记录
			BxSender.getInstance().addToQueue((ArrayList<BxTrackData>)dataList);
			clearDataList();//后台dataList存储
		}
	}
	
	public void save() {
		Util.saveDataToLocate(QuanleimuApplication.context, SERIALIZABLE_TRACKER_FILE, dataList);
	}
	
	public void load() {
		dataList = (ArrayList<BxTrackData>)Util.loadDataFromLocate(QuanleimuApplication.context, SERIALIZABLE_TRACKER_FILE);
	}
	
	public void clearDataList()
	{
		dataList.clear();
	}
}
