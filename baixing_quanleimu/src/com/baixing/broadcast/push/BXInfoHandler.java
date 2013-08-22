//liuchong@baixing.com
package com.baixing.broadcast.push;

import org.json.JSONObject;

import com.baixing.broadcast.BXNotificationService;
import com.baixing.broadcast.CommonIntentAction;
import com.baixing.broadcast.NotificationIds;
import com.baixing.data.GlobalDataManager;
import com.baixing.util.Util;
import com.baixing.util.ViewUtil;

import android.os.Bundle;
import android.util.Log;

import android.content.Context;

public class BXInfoHandler extends PushHandler {
	
	private static final String TAG = BXInfoHandler.class.getSimpleName();

	BXInfoHandler(Context context) {
		super(context);
	}

	@Override
	public boolean acceptMessage(String type) {
		return PageJumper.isValidPage(type);
	}

	@Override
	public void processMessage(String message) {
		try
		{
			JSONObject json = new JSONObject(message);

			JSONObject data = json.getJSONObject("d");
			
			String title = json.getString("t");
			
			String content = data.getString("content");
			
			Log.d(TAG, title + " : " + content);
			
//			String pushCode = "0";
//			if(data.has("pushCode")){
//				pushCode = data.getString("pushCode");
//			}
//			if(!Util.isPushAlreadyThere(cxt, pushCode)){
			Bundle bundle =  new Bundle();
			bundle.putString("data", json.getString("d"));
			bundle.putString("page", json.getString("a"));
				ViewUtil.putOrUpdateNotification(cxt, NotificationIds.NOTIFICATION_ID_BXINFO, 
						CommonIntentAction.ACTION_NOTIFICATION_BXINFO, title, content, bundle, false);
//				Util.saveDataToLocate(cxt, "pushCode", pushCode);
//				Util.saveDataToFile(cxt, null, "pushCode", pushCode.getBytes());
//			}
		}
		catch(Throwable t)
		{
			
		}
	}

}
