package com.quanleimu.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.util.TrackConfig.TrackMobile.BxEvent;
import com.quanleimu.util.TrackConfig.TrackMobile.Key;
import com.quanleimu.util.TrackConfig.TrackMobile.PV;
/**
 * @author xuweiyan@baixing.com
 *Tracker的使用
 *append中键/值在TrackConfig的enum中
 *统计pv示例:
 *Tracker.getInstance().pv(Url.BUZZ).append(Key.ADID, adId).end();
 *统计event示例:
 *Tracker.getInstance().event(Event.DELETED_DELETE).append(Key.ADID, adId).end();
 */
//singleton
public class Tracker {
	private static final String TRACKER_FILE = "bx_tracker.log";//saved file
	private Context context = null;
	private List<String> dataList = null;
	private static Tracker instance = null;
	public static Tracker getInstance()
	{
		Log.d("tracker", "flag"+TrackConfig.getInstance().getLoggingFlag());
		if (instance == null && TrackConfig.getInstance().getLoggingFlag())
		{
			instance = new Tracker();
		}
		return instance;
	}
	//constructor
	private Tracker()
	{
		context = QuanleimuApplication.getApplication().getApplicationContext();
		dataList = new ArrayList<String>();
		load();
	}
	
	public LogData pv(PV url) {
		Log.d("trackmobile",url.getName());
		LogData data = new LogData(new HashMap<String, String>());
		data.append(Key.TRACKTYPE, "pageview");
		data.append(Key.TIMESTAMP, Communication.getTimeStamp());
		data.append(Key.URL, url.getName());
		return data;
	}
	
	public LogData event(BxEvent event) {
		Log.d("trackmobile",event.getName());
		LogData data = new LogData(new HashMap<String, String>());
		data.append(Key.TRACKTYPE, "event");
		data.append(Key.TIMESTAMP, Communication.getTimeStamp());
		data.append(Key.EVENT, event.getName());
		return data;
	}
	
	public void addLog(LogData log)
	{
		dataList.add(log.toJsonObj().toString());
		Log.d("tracker", "addLog->dataList.size:"+dataList.size());
		if (dataList.size()>0) {//100 items
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
				List<String> oldList = (ArrayList<String>)Util.loadDataFromLocate(context, TRACKER_FILE);
				if (oldList != null) {					
					dataList.addAll(oldList);//添加记录到内存
					Util.clearData(context, TRACKER_FILE);
				}
				Log.d("tracker","Load->dataList.size:"+dataList.size());
			} catch (Exception e) {}
	}
	
	public void clear()
	{
		dataList.clear();
	}
}