package com.quanleimu.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.quanleimu.activity.QuanleimuApplication;

//singleton
public class Sender implements Runnable{
	private Context context = null;
	private static String apiName = "trackdata";
	private List<ArrayList<String>> queue = null;
	private static final String SENDER_DIR = "BxLogDir";
	private static final String SENDER_FILE_PREFIX = "bx_sender";//记录文件
	private static final String SENDER_FILE_SUFFIX = ".log";//记录文件
	
	private Object sendMutex = new Object();

	//singleton
	private static Sender instance = null;
	public static Sender getInstance() {
		if (instance == null &&  TrackConfig.getInstance().getLoggingFlag()) {
			instance = new Sender();
		}
		return instance;
	}
	//constructor
	private Sender() {
		context = QuanleimuApplication.context;
		queue = new ArrayList<ArrayList<String>>();
		startThread();
	}
	
	private void startThread() {
		new Thread(this).start();
	}
	
	public void notifyNetworkReady()
	{
		synchronized (sendMutex) {
			this.sendMutex.notifyAll();
		}
	}
	
	public void addToQueue(List<String> dataList) {
		//if there's no newList, queue's item would refer to a zero-sized list
		List<String> newList = new ArrayList<String>();
		newList.addAll(dataList);
		synchronized (queue) {
			queue.add((ArrayList<String>)newList);
			queue.notifyAll();
		}
		
		//Notify send thread to send data.
		synchronized (sendMutex) {
			sendMutex.notifyAll();
		}
	}
	
	public List<ArrayList<String>> getQueue() {
		return queue;
	}

	//check if queue has data or files exist.
	private boolean hasDataToSend() {
		int size = 0;
		synchronized (this.queue) {
			size = queue.size();
		}
		return (size > 0) || loadRecord() != null;
	}
	
	private boolean isSendingReady() {
		return Communication.isNetworkActive();
	}
	
	//save queue into files.
	public void save() {//TODO:which callbacks to call?
		List<ArrayList<String>> newQueue = new ArrayList<ArrayList<String>>();
		//in locker,addall is lightweight operation, not write file operation
		synchronized (queue) {
			newQueue.addAll(queue);
			queue.clear();
		}
		for (ArrayList<String> data : newQueue) {
			saveListToFile(data);
		}
		newQueue.clear();
	}
	
	private void saveListToFile(ArrayList<String> data)
	{
		if (context != null) {
			String fileName = SENDER_FILE_PREFIX + System.currentTimeMillis()/1000 + SENDER_FILE_SUFFIX;
			try {
				Util.saveSerializableToPath(context, SENDER_DIR, fileName, data);			
			} catch (Exception e) {}			
		}
	}
	
	//try to get the first file's name
	private String loadRecord()
	{
		List<String> list = null;
		if (context != null)
			try {
				list = Util.listFiles(context, SENDER_DIR);
			} catch (Exception e) {
				return null;
			}

		if (list == null || list.size() == 0) {
			return null;
		}

		return list.get(0);
	}
	
	private String convertListToJson(List<String> list) {
		String result = "[";
		for (String d : list) {
			result += d + ",";
		}
		result = result.substring(0, result.length()-1);
		result += "]";
		return result;
	}
	
	private boolean sendList(final List<String> list) {//TODO:流量统计:统计每次上传成功的字节数
		String jsonStr = convertListToJson(list);

		boolean succed = Communication.executeSyncPostTask(apiName, jsonStr);
		return succed;
	}
	
	@Override
	public void run() {
		while(TrackConfig.getInstance().getLoggingFlag()) {//config flag
				//First step : send memory data if there is any.
				ArrayList<String> list = null;
				synchronized (queue) {
					int size = 0;
					size = queue.size();//TODO:is "size" neccessary?
					if (size > 0)
						list = queue.remove(0);
				}
				
				if (list != null) {//there's memory data, send
					boolean succed = sendList(list);
					
					if (!succed) {
						saveListToFile(list);
					}
				}
				else	//try load & send persistence file if there is any.
				{
					String recordPath = loadRecord();
					if (recordPath != null)
					{
						@SuppressWarnings("unchecked")
						ArrayList<String> singleRecordList = (ArrayList<String>)Util.loadSerializable(recordPath);
						if (singleRecordList != null && sendList(singleRecordList))
						{
							try {
								new File(recordPath).delete();
							} catch (Exception e) {}
						}
					}
				}
				
				//Check if we have more data to send.
				boolean hasMoreData = hasDataToSend();
				
				while (!isSendingReady() || !hasMoreData) {
					try {
						Log.d("BxSender", "wait");
						synchronized (sendMutex) {
							wait(300000);//time out 5 min
						}
						hasMoreData = hasDataToSend();
						Log.d("BxSender", "wake up");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
		}//while config flag
	}
}