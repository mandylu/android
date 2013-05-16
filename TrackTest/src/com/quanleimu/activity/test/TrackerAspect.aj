package com.quanleimu.activity.test;
import android.content.Context;
import android.util.Log;

import com.baixing.network.NetworkUtil;
import com.baixing.tracking.LogData;
import com.baixing.tracking.Tracker;
aspect TrackerAspect
{
	pointcut saveTrackerLog() : execution(* Tracker.addLog(..));
	after() returning : saveTrackerLog(){
		Log.d("hahhaha", "aspectJ hahahahah");
		final Object[] args = thisJoinPoint.getArgs();
		for(int i = 0; i < args.length; i++){
	        final Object arg = args[i];
	        if ( arg instanceof LogData) {
	        	Log.d("hahhaha", "aspectJ" + ((LogData)arg).toJsonObj().toString());
	        	TrackerLogSaver.getInstance().addLog((LogData)arg);
	        }
	    }
	}

	Object around(): saveTrackerLog()
	{
		final Object[] args = thisJoinPoint.getArgs();
		for(int i = 0; i < args.length; i++){
	        final Object arg = args[i];
	        if ( arg instanceof LogData) {
	        }
	    }
		Object result = proceed();
		return result;
	}
		
	Object around() : call(static boolean NetworkUtil.isWifiConnection(Context)){
		return isWifiConnected();
	}
	
	private static boolean wifiConnected = true; 
	
	public static boolean isWifiConnected(){
		return wifiConnected;
	}
	
	public static void setWifiConnected(boolean connected){
		wifiConnected = connected;
	}
};