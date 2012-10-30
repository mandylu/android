package com.quanleimu.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.quanleimu.activity.QuanleimuApplication;
/**
 * @author xuweiyan@baixing.com
 *Tracker的使用
 *append中键/值在TrackConfig的enum中
 *统计pv:
 *try {Tracker.getInstance().pv().append("xx","xx").append("xx","xx").end();} catch (NullPointerException e) {}
 *
 *统计event:
 *try {Tracker.getInstance().event().append("xx","xx").append("xx","xx").end();} catch (NullPointerException e) {}
 */
//singleton
public class Tracker {
	private static final String TRACKER_FILE = "bx_tracker.log";//saved file
	private Context context = null;
	private List<String> dataList = null;
	private static Tracker instance = null;
	public static Tracker getInstance()
	{
		if (instance == null && TrackConfig.getInstance().getLoggingFlag())
		{
			instance = new Tracker();
		}
		if (TrackConfig.getInstance().getLoggingFlag() == false) return null;
		return instance;
	}
	//constructor
	private Tracker()
	{
		context = QuanleimuApplication.context;
		dataList = new ArrayList<String>();
		load();
	}
	
	public LogData pv() {
		LogData data = new LogData(new HashMap<String, String>());
		data.append("tracktype", "pageview");
		data.append("timestamp", Communication.getTimeStamp());
		return data;
	}
	
	public LogData event() {
		LogData data = new LogData(new HashMap<String, String>());
		data.append("tracktype", "event");
		data.append("timestamp", Communication.getTimeStamp());
		return data;
	}
	
	public void addLog(LogData log)
	{
		dataList.add(log.toJsonObj().toString());
		if (dataList.size()>0) {//100 items
			Log.d("sender", "dataList.size>0");
			try {
				Log.d("sender", "try to addLog");
				Sender.getInstance().addToQueue(dataList);//in case sender is null right now
				clear();
			} catch (Exception e) {Log.d("sender", "sender is null when track.addLog");}
		}
	}
	
	public void save() {
		if (context != null)
			try {
				Util.saveDataToLocate(context, TRACKER_FILE, dataList);
			} catch (Exception e) {}
	}
	
	@SuppressWarnings("unchecked")
	private void load() {
		if (context != null)
			try {
				dataList = (ArrayList<String>)Util.loadDataFromLocate(context, TRACKER_FILE);
			} catch (Exception e) {}
	}
	
	public void clear()
	{
		dataList.clear();
	}
}