//xuweiyan@baixing.com
package com.baixing.tracking;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.baixing.android.api.ApiClient;
import com.baixing.android.api.ApiError;
import com.baixing.android.api.ApiListener;
import com.baixing.android.api.ApiParams;
import com.baixing.android.api.cmd.BaseCommand;
import com.baixing.data.GlobalDataManager;
import com.baixing.util.Communication;
import com.baixing.util.GzipUtil;
import com.baixing.util.Util;

//singleton
public class Sender implements Runnable{
	private Context context = null;
	private static String apiName = "trackdata";
	private List<String> queue = null;
	static final String SENDER_DIR = "sender_dir";
	static final String SENDER_FILE_SUFFIX = ".log";//记录文件
	private long dataSize;
	private Object sendMutex = new Object();

	//singleton
	private static Sender instance = null;
	public static Sender getInstance() {
		if (instance == null) {
			instance = new Sender();
		}
		return instance;
	}
	//constructor
	private Sender() {
		dataSize = 0;
		context = GlobalDataManager.getInstance().getApplicationContext();
		queue = new ArrayList<String>();
		startThread();
	}
	
	private void startThread() {
		new Thread(this).start();
	}
	
	public void notifySendMutex() {
		Log.d("sendlistfunction","notifySendMutex");
		// Notify send thread to send data.
		synchronized (sendMutex) {
			sendMutex.notifyAll();
		}
	}
	
	public void notifyNetworkReady()
	{
		notifySendMutex();
	}
	
	public void addToQueue(String dataString) {
		synchronized (queue) {
			queue.add(dataString);
 		}
		
		notifySendMutex();
	}
	
	public List<String> getQueue() {
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
	
	private boolean checkQueueFull() {
		boolean isQueueFull = false;
		synchronized (queue) {
			isQueueFull = queue.size()>3;
		}
		return isQueueFull;
	}
	
	//save queue into files.
	public void save() {
		Log.d("sendlistfunction","sender save");
		List<String> newQueue = new ArrayList<String>();
		//in locker,addall is lightweight operation, not write file operation
		synchronized (queue) {
			newQueue.addAll(queue);
			queue.clear();
		}
		
		if (newQueue.size() > 0) {
			JSONArray compositeArray = new JSONArray();
			for(String itemString : newQueue) {
				JSONArray itemArray = null;
				try {
					itemArray = new JSONArray(itemString);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (itemArray!=null && itemArray.length()>0) {
					for (int i=0;i<itemArray.length();i++) {
						try {
							compositeArray.put(itemArray.get(i));
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			}
			if (compositeArray.length()>0) {
				saveListToFile(compositeArray.toString());
			}
//			saveListToFile((new JSONArray(newQueue)).toString());
			notifySendMutex();
			newQueue.clear();
		}
	}
	
	private void saveListToFile(String data)//坑说明：必须注意如果queue中每个元素都存一个文件，按秒来区分文件名，很可能重名，文件覆盖导致数据丢失
	{
		if (context != null) {
			String fileName = System.currentTimeMillis() + SENDER_FILE_SUFFIX;
			try {
				Log.d("sendlist","saveListToFile->"+fileName);
				Util.saveDataToFile(context, SENDER_DIR, fileName, data.getBytes());
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
	
	public static boolean executeSyncPostTask(final String apiName, final String jsonStr) {
//		String url = Communication.getApiUrl(apiName, new ArrayList<String>());
//		url += "&json=";
//		url += jsonStr;
		
		ApiParams params = new ApiParams();
		params.addParam("json", jsonStr);
		try {
			Log.d("sender", "try sending");
			String result = ApiClient.getInstance().invokeApi(apiName, params);//Communication.getDataByGzipUrl(url, true);
			Log.d("response",result);
			JSONObject error = new JSONObject(result);
			int code = (Integer) error.getJSONObject("error").get("code");
			if (code == 0)
				return true;
			else
				return false;
		} catch (Exception e) {
			return false;
		}
	}
	
	private boolean sendList(final String jsonStr) {//流量统计:统计每次上传成功的字节数
		Log.d("sendlistfunction",jsonStr);
		boolean succed = Sender.executeSyncPostTask(apiName, jsonStr);
		if (succed)
			try {
				dataSize += GzipUtil.compress(jsonStr).getBytes().length;
				Log.d("datasize","datasize:"+(dataSize/1024/1024!=0?dataSize/1024.0/1024+"MB":(dataSize/1024!=0?dataSize/1024.0+"KB":dataSize+"B")));
			} catch (Exception e) {
				e.printStackTrace();
			}
		
		if (succed && new File(Environment.getExternalStorageDirectory()
				+ "/baixing_debug_log_crl.dat").exists()) {
			try {
				JSONArray array = new JSONArray(jsonStr);
				for (int i = 0; i < array.length(); i++) {
					JSONObject log = null;
					log = array.getJSONObject(i);
					if (log != null) {
						Util.saveDataToSdCard("baixing", "sender_sendlistlog", log.toString()
								+ "\n", true);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return succed;
	}
	
	@Override
	public void run() {
		Log.d("sender", "run");
		boolean hasMoreData;
		boolean isQueueFull;
		while(TrackConfig.getInstance().getLoggingFlag()) {//config flag
				//First step : send memory data if there is any.
				String list = null;
				synchronized (queue) {
					if (queue.size() > 0)
						list = queue.remove(0);
				}
				Log.d("sendlistfunction","big while");
				if (list != null) {//there's memory data, send
					Log.d("sender", "has memory data");
					boolean succed = sendList(list);
					Log.d("sender", "after sendList");
					if (!succed) {
						Log.d("sender", "saveListToFile");
						saveListToFile(list);
					}
				}
				else	//try load & send persistence file if there is any.
				{
					String recordPath = loadRecord();
					if (recordPath != null)
					{
						String singleRecordList = new String(Util.loadData(recordPath));
						Log.d("sendlist","file send->"+recordPath);
						if (singleRecordList != null && sendList(singleRecordList))
						{
							Util.clearFile(recordPath);
						}
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				Log.d("sender", "check if there's more data to send");
				//Check if we have more data to send.
				hasMoreData = hasDataToSend();
				
				isQueueFull = checkQueueFull();
				
				while ((!isSendingReady() && !isQueueFull) || !hasMoreData) {//断网或者无数据
					Log.d("sendlist","into small while");
					try {
						Log.d("sendlistfunction", "small while,wait");
						synchronized (sendMutex) {
							sendMutex.wait(300000);//time out 5 min
						}
						hasMoreData = hasDataToSend();
						isQueueFull = checkQueueFull();
						Log.d("sender", "hasMoredata:"+hasMoreData);
						Log.d("sendlistfunction", "wake up");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
		}//while config flag
	}
}