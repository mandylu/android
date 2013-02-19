//xumengyi@baixing.com
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
import android.content.Intent;

public class UrlHandler extends PushHandler {

	UrlHandler(Context context) {
		super(context);
	}

	@Override
	public boolean acceptMessage(String type) {
		return type.equals("jumpurl");
	}

	@Override
	public void processMessage(String message) {
		try{
			JSONObject json = new JSONObject(message);

			JSONObject data = json.getJSONObject("d");
			
			String title = json.getString("t");
			
			String content = data.getString("content");
			Bundle bundle =  new Bundle();
			bundle.putString("data", json.getString("d"));
			ViewUtil.putOrUpdateNotification(cxt, NotificationIds.NOTIFICATION_ID_JUMPURL, 
					Intent.ACTION_VIEW, title, content, bundle, false);
		}
		catch(Throwable t){
			
		}
	}

}
