package com.quanleimu.util;

import java.util.HashMap;

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
	private static final String TRACKER_DIR = "tracker_dir";

	private int size;
	private String dataString = null;
	private static Tracker instance = null;
	public static Tracker getInstance()
	{
		if (instance == null)
		{
			instance = new Tracker();
		}
		return instance;
	}
	//constructor
	private Tracker()
	{
		context = QuanleimuApplication.getApplication().getApplicationContext();
		dataString = "";
		size = 0;
		load();
	}
	
	public LogData pv(PV url) {
//		Log.d("trackmobile","@"+url.getName());
		LogData data = new LogData(new HashMap<String, String>());
		data.append(Key.TRACKTYPE, "pageview");
		data.append(Key.TIMESTAMP, Communication.getTimeStamp());
		data.append(Key.URL, url.getName());
		return data;
	}
	
	public LogData event(BxEvent event) {
//		Log.d("trackmobile","@"+event.getName());
		LogData data = new LogData(new HashMap<String, String>());
		data.append(Key.TRACKTYPE, "event");
		data.append(Key.TIMESTAMP, Communication.getTimeStamp());
		data.append(Key.EVENT, event.getName());
		return data;
	}
	
	public void addLog(LogData log)
	{
		dataString += log.toJsonObj().toString() + ",";
		size++;
		if (size > 10) {//100 items,10 for testing
			try {
				Log.d("sender", "try to addLog");
				Sender.getInstance().addToQueue(dataString.substring(0, dataString.length()-1));//in case sender is null right now
				clear();
			} catch (Exception e) {Log.d("sender", "sender is null when track.addLog");}
		}
	}
	
	public void save() {
		if (context != null)
			try {
				Util.saveDataToFile(context, TRACKER_DIR, TRACKER_FILE, dataString.getBytes());
			} catch (Exception e) {}
	}
	
	private void load() {
		if (context != null)
			try {
				String absolutePath = Util.listFiles(context, TRACKER_DIR).get(0);
				String oldString = new String(Util.loadData(absolutePath));
				if (oldString != null) {
					if (dataString.equals(""))
						dataString = oldString;
					else
						dataString += "," + oldString;
					Util.clearFile(absolutePath);
				}
			} catch (Exception e) {}
	}
	
	public void clear()
	{
		dataString = "";
		size = 0;
	}
}