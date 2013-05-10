package com.quanleimu.activity.test;

import java.util.ArrayList;
import java.util.HashMap;

import com.baixing.tracking.LogData;

class TrackerLogSaver {
	private static TrackerLogSaver instance;
	public static TrackerLogSaver getInstance(){
		if(instance == null){
			instance = new TrackerLogSaver();
		}
		return instance;
	}
	
	private ArrayList<LogData> logs = new ArrayList<LogData>();
	
	public ArrayList<LogData> getLog(){
		ArrayList<LogData> ret = new ArrayList<LogData>();
		synchronized(this){
			ret.addAll(logs);
		}
		return ret;
	}
	
	public ArrayList<LogData> getLog(String trackType, String event){
		ArrayList<LogData> current = getLog();
		ArrayList<LogData> ret = new ArrayList<LogData>();
		boolean isPageView = trackType.equals("pageview") ? true : false;
		for(int i = current.size() - 1; i >= 0; -- i){
			LogData ld = current.get(i);
			HashMap<String, String> maps = ld.getMap();
			if(maps != null){
				if(maps.get("tracktype").equals(trackType) 
						&& ((isPageView && maps.get("url").equals(event))
						|| (!isPageView && maps.get("event").equals(event)))){
					ret.add(current.get(i));
				}
			}
		}
		return ret;
	}
	
	public void addLog(LogData data){
		synchronized(this){
			logs.add(data);
			if(logs.size() > 100){
				logs = (ArrayList<LogData>)logs.subList(logs.size() - 100, logs.size());
			}
		}
	}
	
	public void clearLog(){
		synchronized(this){
			logs.clear();
		}
	}
}