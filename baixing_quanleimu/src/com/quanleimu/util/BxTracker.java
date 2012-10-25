package com.quanleimu.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.quanleimu.activity.QuanleimuApplication;

//singleton
public class BxTracker {
	private static final String SERIALIZABLE_TRACKER_FILE = "bx_tracker.ser";//记录文件
	private String tracktype = null;
	private List<BxTrackData> dataList = null;
	private HashMap<String,String> map = null;
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
		map = new HashMap<String,String>();
	}
	
	public void endLog() {
		map.put("tracktype", tracktype);
		map.put("timestamp", Communication.getTimeStamp());

		//addTrackData
		addTrackData(new BxTrackData(map));
		//clear map
		map.clear();
	}
	
	public BxTracker pageLog(String key, String value) {
		map.put(key, value);
		tracktype = "pageview";
		return getInstance();
	}
	
	public BxTracker eventLog(String key, String value) {
		map.put(key, value);
		tracktype = "event";
		return getInstance();
	}
	//添加记录
	private void addTrackData(BxTrackData trackobj)
	{
		dataList.add(trackobj);
		if (dataList.size()>100) {//设置100次,写一组记录
			synchronized (BxSender.getInstance().getQueue()) {
				BxSender.getInstance().addToQueue((ArrayList<BxTrackData>)dataList);
				BxSender.getInstance().notifyAll();
			}
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
