package com.quanleimu.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Pair;

import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.util.Communication.BXHttpException;

public class MobileConfig {
	private JSONObject json = null;
	
	private static MobileConfig instance = null;
	public static MobileConfig getInstance() {
		if (instance==null) {
			instance = new MobileConfig();
		}
		return instance;
	}
	
	private MobileConfig() {
		Context context = QuanleimuApplication.getApplication().getApplicationContext();
		String jsonString = (String) Util.loadDataFromLocate(context, "mobile_config");
		if (jsonString == null || jsonString.length() == 0) {
			try {
				InputStream input = context.getAssets().open("mobile_config.txt");
				byte b[] = new byte[input.available()];
				input.read(b);
				jsonString = new String(b);
			} catch (IOException e) {
				jsonString = null;
			}
		}
		if (jsonString != null && jsonString.length() > 0) {
			try {
				json = new JSONObject(jsonString);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
		
	public boolean isEnableTracker() {
		try {
			return json.getBoolean("trackFlag");
		} catch (JSONException e) {
			return true;//默认记录
		}
	}
	
	public long getCityTimestamp() {
		try {
			return json.getLong("cityTimestamp");
		} catch (JSONException e) {
			return 0l;
		}
	}
	
	public long getCategoryTimestamp() {
		try {
			return json.getLong("categoryTimestamp");
		} catch (JSONException e) {
			return 0l;
		}
	}

    public boolean hasNewVersion() {
        try {
            String serverVersion = json.getString("serverVersion");
            return (Version.compare(serverVersion, QuanleimuApplication.version) == 1);
        } catch (JSONException e) {
            return false;
        }
    }
	
	public void syncMobileConfig() {
		new Thread(new UpdateMobileConfigThread()).start();				
	}
	
	class UpdateMobileConfigThread implements Runnable {

		@Override
		public void run() {
			String apiName = "mobile_config";
			Pair<Long, Object> p = 
					Util.loadDataAndTimestampFromLocate(QuanleimuApplication.getApplication().getApplicationContext(), "mobile_config");
			if(p != null){
				if(System.currentTimeMillis() / 1000 - p.first <= 24 * 3600){
					return;
				}
			}
			String url = Communication.getApiUrl(apiName, new ArrayList<String>());
			try {
				String content = Communication.getDataByUrl(url, true);
				if (content != null && content.length() > 0) {
					MobileConfig.this.json = new JSONObject(content);
					Util.saveDataToLocate(QuanleimuApplication.getApplication().getApplicationContext(), "mobile_config", content);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (BXHttpException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}			
		}
		
	}
}
