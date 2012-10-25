package com.quanleimu.util;

import java.util.ArrayList;

import com.quanleimu.activity.QuanleimuApplication;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BxMobileConfig {
	
	
	private  String response;
	private boolean isLogging = true;//默认开启mobile track为true
	private final static int CONFIG_ON = 2;
	private final static int CONFIG_OFF = 4;
	private Handler handler;
	private boolean hasResponseFromApi = false;
	
	private static BxMobileConfig instance = null;
	public static BxMobileConfig getInstance() {
		if (instance==null) {
			instance = new BxMobileConfig();
		}
		return instance;
	}
	//构造器
	private BxMobileConfig() {
		//handler
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == CONFIG_ON) {
					Log.d("BXMobileConfig", "Turn ON Tracker & Sender");
					isLogging = true;
				} else if (msg.what == CONFIG_OFF) {
					Log.d("BXMobileConfig", "Turn OFF Tracker & Sender");
					isLogging = false;
				}
				super.handleMessage(msg);
			}
			
		};
	}

	public boolean getLoggingFlag() {
		return isLogging;
	}
	
	public void getConfig() {
		if (hasResponseFromApi == false) {
			hasResponseFromApi = true;
			new Thread(new ConfigRunnable()).start();
		}
	}
	
	private void sendMessage(int what) {
		Message message = null;
		if (handler != null) {
			message = handler.obtainMessage();
			message.what = what;
			handler.sendMessage(message);
		}
	}
	
	class ConfigRunnable implements Runnable {

		@Override
		public void run() {
			String apiName = "mobile_config";
			String url = Communication.getApiUrl(apiName, new ArrayList<String>());
			try {
				response = Communication.getDataByUrl(url, true);
			} catch (Exception e) {
				Log.d("BxMobileConfig", "response:exception");
			} finally {
				if (response != null && response.equals("\"false\"")) {
					Log.d("BxMobileConfig", "response:");
					BxMobileConfig.getInstance().sendMessage(CONFIG_OFF);
				}else if (response != null && response.equals("\"true\"")){
					Log.d("BxMobileConfig", "mobile_config:none");
					BxMobileConfig.getInstance().sendMessage(CONFIG_ON);
				}
			}
		}
	}
}
