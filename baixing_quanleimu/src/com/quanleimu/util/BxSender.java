package com.quanleimu.util;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

public class BxSender implements Runnable{
	private boolean isFileReady;
	private List<String> manifest = null;
	private List<BxTrackData> list = null;
	private Context context = null;
	private static String apiName = "trackdata";
	private int sendingTimes = 0;
	private static final String SERIALIZABLE_TRACK_MANIFEST = "bx_track_manifest.ser";//花名册存放的文件

	//constructor
	public BxSender(Context context) {
		this.context = context;
		isFileReady = false;
	}
	
	public void setFileFlag(boolean flag) {
		isFileReady = flag;
	}
	
	private boolean isFileReady() {
		return isFileReady;
	}
	
	private boolean isSendingReady() {
		return Communication.isNetworkActive();
	}
	
	private Object loadFromLocal(Context context, String file) {
		return Util.loadDataFromLocate(context, file);
	}
	
	private void clearFile(Context context, String file) {
		Util.clearData(context, file);
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
		String compressedJson = null;
		try {
//			System.out.println("compressedJson("+GzipUtil.compress(jsonStr).length()+"):"+ GzipUtil.compress(jsonStr));
			compressedJson = GzipUtil.compress(jsonStr);
		} catch (IOException e) {
			e.printStackTrace();
		}
		Communication.executeSyncPostTask(apiName, compressedJson, new Communication.CommandListener() {
			
			@Override
			public void onServerResponse(String serverMessage) {
				System.out.println("send ok!"+serverMessage);
			}
			
			@Override
			public void onException(Exception ex) {
				System.out.print("send wrong");
				sendingTimes++;
				if (sendingTimes<2)//再次发送
					sendList(list);
				else
					sendingTimes = 0;
			}
		});
	}
	
	@Override
	public void run() {
		while(true) {
			synchronized (this) {
				while (!isFileReady() || !isSendingReady()) {
					try {
						System.out.println("Wait..");
						wait();
						//notified
						System.out.println("wake up~");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				//file ready & sending ready
				System.out.println("ready to send~");
				manifest = BxTracker.getInstance().getManifest();
				while (manifest.size() > 0) {
					list = (ArrayList<BxTrackData>) (loadFromLocal(context,
							manifest.get(0)));
					if (list != null) {
						sendList(list);
						clearFile(context, manifest.get(0));
					}
					manifest.remove(0);
				}
				setFileFlag(false);
				manifest.clear();
				Util.saveDataToLocate(context, SERIALIZABLE_TRACK_MANIFEST,
						manifest);
			}
		}//while true
	}

}
