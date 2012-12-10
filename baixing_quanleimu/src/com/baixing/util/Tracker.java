package com.baixing.util;

import java.io.File;
import java.util.HashMap;

import org.json.JSONArray;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.baixing.util.TrackConfig.TrackMobile.BxEvent;
import com.baixing.util.TrackConfig.TrackMobile.Key;
import com.baixing.util.TrackConfig.TrackMobile.PV;
import com.quanleimu.activity.QuanleimuApplication;
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
//	private static final String TRACKER_FILE = "bx_tracker.log";//saved file
	private Context context = null;
//	private static final String TRACKER_DIR = "tracker_dir";
	private static final String SENDER_DIR = "sender_dir";
	private static final String SENDER_FILE_SUFFIX = ".log";//记录文件

	private int size;
	private int threshold;
	private JSONArray dataArray = null;
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
		dataArray = new JSONArray();
		size = 0;
		threshold = 100;//5 for testing
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
		if (new File(Environment.getExternalStorageDirectory()
				+ "/baixing_debug_log_crl.dat").exists()) {
			try {
				if (log != null) {
					Util.saveDataToSdCard("baixing", "tracker_addlog", log.toJsonObj().toString()
							+ "\n", true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		Log.d("sendlistfunction","tracker addLog");
		dataArray.put(log.toJsonObj());
		size++;
		if (size > threshold) {
			try {
				Sender.getInstance().addToQueue(dataArray.toString());//in case sender is null right now
				clear();
			} catch (Exception e) {}
		}
	}
	
	public void save() {
		Log.d("sendlistfunction","tracker save");
		if (context != null && dataArray.length() > 0) {
			String fileName = "tracker" + System.currentTimeMillis() + SENDER_FILE_SUFFIX;
			try {
				Util.saveDataToFile(context, SENDER_DIR, fileName, dataArray.toString().getBytes());
				clear();
				Sender.getInstance().notifySendMutex();
			} catch (Exception e) {}
		}
	}
	
	public void clear()
	{
		dataArray = new JSONArray();
		size = 0;
	}
}