package com.quanleimu.activity.test;

import java.util.ArrayList;

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