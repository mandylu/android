package com.quanleimu.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.quanleimu.activity.QuanleimuApplication;

//singleton
public class BxSender implements Runnable{
//	private boolean isQueueReady;
	private List<BxTrackData> list = null;
	private Context context = null;
	private static String apiName = "trackdata";
	private int sendingTimes = 0;
	private List<ArrayList<BxTrackData>> queue = null;
	private static final String SERIALIZABLE_SENDER_DIR = "BxLogDir";
	private static final String SERIALIZABLE_SENDER_FILE_PREFIX = "bx_sender";//记录文件
	private static final String SERIALIZABLE_SENDER_FILE_SUFFIX = ".ser";//记录文件

	
	
	//singleton
	private static BxSender instance = null;
	public static BxSender getInstance() {
		if (instance == null) {// && BxMobileConfig.getInstance().getLoggingFlag() == true
			instance = new BxSender();
		}
		return instance;
	}
	private BxSender() {//构造器
		this.context = QuanleimuApplication.context;
		queue = new ArrayList<ArrayList<BxTrackData>>();
		startThread();
	}
	
	private void startThread() {
		new Thread(this).start();
	}
	
	public void addToQueue(ArrayList<BxTrackData> dataList) {
		queue.add((ArrayList<BxTrackData>) dataList);
	}
	
	public List<ArrayList<BxTrackData>> getQueue() {
		return queue;
	}

	private boolean getQueueFlag() {
		int size = 0;
		synchronized (this.queue) {
			size = queue.size();
		}
		return (size > 0);
	}
	
	private boolean isSendingReady() {
		return Communication.isNetworkActive();
	}
	
//	private Object loadFromLocal(String file) {
//		return Util.loadDataFromLocate(this.context, file);
//	}
	
	//save queue
	public void save() {
		String fileName = "";
		for (ArrayList<BxTrackData> data : queue) {
			fileName = SERIALIZABLE_SENDER_FILE_PREFIX + System.currentTimeMillis()/1000 + SERIALIZABLE_SENDER_FILE_SUFFIX;
			Util.saveSerializableToPath(context, SERIALIZABLE_SENDER_DIR, fileName, data);
		} 
	}
	
	//load queue
	public void load() {
		List<String> list = Util.listFiles(context, SERIALIZABLE_SENDER_DIR);
		if (list.size() > 0) {
			for(String file : list) {
				synchronized (queue) {
					queue.add((ArrayList<BxTrackData>)Util.loadSerializable(file));
				}
				Util.clearData(context, file);
				new File(file).delete();
			}
		}
	}
	
	private String convertListToJson(List<BxTrackData> list) {
		String result = "[";
		for (BxTrackData d : list) {
			result += d.toJsonObj().toString() + ",";
		}
		result = result.substring(0, result.length()-1);
		result += "]";
		return result;
	}
	
	private void sendList(final List<BxTrackData> list) {
		String jsonStr = convertListToJson(list);

		Communication.executeSyncPostTask(apiName, jsonStr, new Communication.CommandListener() {
			
			@Override
			public void onServerResponse(String serverMessage) {
				Log.d("BxSender", "onServerResponse:" + serverMessage);
			}
			
			@Override
			public void onException(Exception ex) {
				Log.d("BxSender", "onException");
				sendingTimes++;
				if (sendingTimes<2)//TODO:发送失败wait
					sendList(list);
				else
					sendingTimes = 0;
			}
		});
	}
	
	@Override
	public void run() {
		load();
		if (Util.listFiles(context, "BxLogDir").size() > 0) {
			for(String file : Util.getFileList()) {
				//add file to queue
				synchronized (this.queue) {
					queue.add((ArrayList<BxTrackData>)Util.loadDataFromLocate(this.context, file));
				}
//				Util.clearData(this.context, file);
			}
		}
		while(true) {
				while (!getQueueFlag() || !isSendingReady()) {
					try {
						Log.d("BxSender", "wait()");
						synchronized (this.queue) {
							wait(300000);//wait time out 5 min
						}
						Log.d("BxSender", "wake up~~");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				//file ready & sending ready
				Log.d("BxSender","ready to send~");
				//TODO:gzip之后
				int size = 0;
				synchronized (queue) {
					while ((size = queue.size()) > 0) {
						list = queue.get(0);
						if (list != null) {
							sendList(queue.get(0));
							queue.remove(0);
						}
					}
				}
		}//while true
	}

}
