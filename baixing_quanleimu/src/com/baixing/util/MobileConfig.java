package com.baixing.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.baixing.android.api.ApiClient;
import com.baixing.android.api.ApiClient.Api;
import com.baixing.android.api.ApiParams;
import com.baixing.data.GlobalDataManager;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
import com.baixing.util.Communication.BXHttpException;
import com.baixing.util.PerformEvent.Event;

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
		Context context = GlobalDataManager.getInstance().getApplicationContext();
		Pair<Long, String> p = Util.loadJsonAndTimestampFromLocate(context, "mobile_config");
		String jsonString = p.second;
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

    public boolean isUseUmengUpdate() {
        try {
            return json.getBoolean("umengUpdateFlag");
        } catch (JSONException e) {
            return false;//默认记录
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
            return (Version.compare(serverVersion, GlobalDataManager.getInstance().getVersion()) == 1);
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

			Pair<Long, String> p = 
					Util.loadJsonAndTimestampFromLocate(GlobalDataManager.getInstance().getApplicationContext(), "mobile_config");
			if (System.currentTimeMillis() / 1000 - p.first <= 24 * 3600){
				PerformanceTracker.stamp(Event.E_LoadConfig_Less_24);//, PerformanceTracker.getFileName(), PerformanceTracker.getLineNumber(), System.currentTimeMillis());
				return;
			}
			
//			String url = Communication.getApiUrl(apiName, new ArrayList<String>());
			try {
				String content = ApiClient.getInstance().invokeApi(Api.createGet(apiName), new ApiParams());//
				if (content != null && content.length() > 0) {
					MobileConfig.this.json = new JSONObject(content);
					Util.saveJsonAndTimestampToLocate(GlobalDataManager.getInstance().getApplicationContext(), "mobile_config", content, System.currentTimeMillis()/1000);
					
//			        if (MobileConfig.this.hasNewVersion()) {			        	
//			            UpdateHelper.getInstance().checkNewVersion(QuanleimuApplication.getApplication().getApplicationContext());
//			        }
				}
			}
			catch (Throwable t)
			{
				Log.e("QLM", "get mobile config faild. caused by " + t.getMessage());
			}
			finally
			{
				BxMessageCenter.defaultMessageCenter().postNotification(IBxNotificationNames.NOTIFICATION_CONFIGURATION_UPDATE, null);
			}
			PerformanceTracker.stamp(Event.E_Leave_MobileConfigThread);//, PerformanceTracker.getFileName(), PerformanceTracker.getLineNumber(), System.currentTimeMillis());
		}
		
	}
}
