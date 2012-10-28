package com.quanleimu.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;

import com.quanleimu.activity.QuanleimuApplication;
/**
 * @author xuweiyan@baixing.com
 *Tracker的使用
 *统计pv:
 *try {	Tracker.getInstance().pv().append("xx","xx").append("xx","xx").end();} catch (NullPointerException e) {}
 *
 *统计event:
 *try {	Tracker.getInstance().event().append("xx","xx").append("xx","xx").end();} catch (NullPointerException e) {}
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
		return instance;
	}
	//constructor
	private Tracker()
	{
		context = QuanleimuApplication.context;
		dataList = new ArrayList<String>();
	}
	
	public Log pv() {
		Log data = new Log(new HashMap<String, String>());
		data.append("tracktype", "pageview");
		data.append("timestamp", Communication.getTimeStamp());
		return data;
	}
	
	public Log event() {
		Log data = new Log(new HashMap<String, String>());
		data.append("tracktype", "event");
		data.append("timestamp", Communication.getTimeStamp());
		return data;
	}
	
	public void addLog(Log log)
	{
		dataList.add(log.toJsonObj().toString());
		if (dataList.size()>100) {//100 items
			try {
				Sender.getInstance().addToQueue(dataList);//in case sender is null right now
				clear();
			} catch (Exception e) {}
		}
	}
	
	public void save() {//TODO:which callbacks to call?
		if (context != null)
			try {
				Util.saveDataToLocate(context, TRACKER_FILE, dataList);
			} catch (Exception e) {}
	}
	
	@SuppressWarnings("unchecked")
	public void load() {//TODO:which callbacks to call?
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