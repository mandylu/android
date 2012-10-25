package com.quanleimu.broadcast.push;

import org.json.JSONObject;

import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.broadcast.BXNotificationService;
import com.quanleimu.broadcast.CommonIntentAction;
import com.quanleimu.broadcast.NotificationIds;
import com.quanleimu.util.Util;
import com.quanleimu.util.ViewUtil;
import android.util.Log;

import android.content.Context;

public class BXInfoHandler extends PushHandler {

	BXInfoHandler(Context context) {
		super(context);
	}

	@Override
	public boolean acceptMessage(String type) {
		return "bxinfo".equals(type);
	}

	@Override
	public void processMessage(String message) {
		try
		{
			JSONObject json = new JSONObject(message);

			JSONObject data = json.getJSONObject("data");
			
			String title = "百姓网有新版本啦";
			if (data.has("title")) {
				title = data.getString("title");
			}
			
			String content = "赶紧去更新";
			if (data.has("content")) {
				content = data.getString("content");
			}
			
			String pushCode = "0";
			if(data.has("pushCode")){
				pushCode = data.getString("pushCode");
			}
			if(!Util.isPushAlreadyThere(cxt, pushCode)){
				QuanleimuApplication.version = Util.getVersion(cxt);
				QuanleimuApplication.udid = Util.getDeviceUdid(cxt);
				ViewUtil.putOrUpdateNotification(cxt, NotificationIds.NOTIFICATION_ID_BXINFO, 
						CommonIntentAction.ACTION_NOTIFICATION_BXINFO, title, content, null, false);
				Util.saveDataToLocate(cxt, "pushCode", pushCode);
			}
		}
		catch(Throwable t)
		{
			
		}
	}

}
