package com.quanleimu.util;

import java.util.ArrayList;

import com.quanleimu.activity.QuanleimuApplication;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class TrackConfig {
	
	
	private  String response;
	private boolean isLogging = true;//default config
	private boolean hasResponseFromApi = false;
	
	private static TrackConfig instance = null;
	public static TrackConfig getInstance() {
		if (instance==null) {
			instance = new TrackConfig();
		}
		return instance;
	}
	//constructor
	private TrackConfig() {
		
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
	
	class ConfigRunnable implements Runnable {

		@Override
		public void run() {
			String apiName = "mobile_config";
			String url = Communication.getApiUrl(apiName, new ArrayList<String>());
			try {
				response = Communication.getDataByUrl(url, true);
			} catch (Exception e) {

			} finally {
				if (response != null && response.equals("\"false\"")) {
					isLogging = false;
				}else if (response != null && response.equals("\"true\"")){
					isLogging = true;
				}
			}
		}
	}
}
