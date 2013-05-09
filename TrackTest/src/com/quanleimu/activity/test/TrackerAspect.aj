package com.quanleimu.activity.test;
import android.util.Log;

import com.baixing.tracking.*;
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
//		Log.d("hahhaha", "aspectJ hahahahah");
		final Object[] args = thisJoinPoint.getArgs();
		for(int i = 0; i < args.length; i++){
	        final Object arg = args[i];
	        if ( arg instanceof LogData) {
//	        	Log.d("hahhaha", "aspectJ" + ((LogData)arg).toJsonObj().toString());
//	        	TrackerLogSaver.getInstance().addLog((LogData)arg);
	        }
	    }
		Object result = proceed();
		return result;
	}
};