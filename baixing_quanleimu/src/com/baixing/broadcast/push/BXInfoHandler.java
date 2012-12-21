//liuchong@baixing.com
package com.baixing.broadcast.push;

import org.json.JSONObject;

import com.baixing.broadcast.BXNotificationService;
import com.baixing.broadcast.CommonIntentAction;
import com.baixing.broadcast.NotificationIds;
import com.baixing.util.Util;
import com.baixing.util.ViewUtil;
import com.quanleimu.activity.QuanleimuApplication;
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
				ViewUtil.putOrUpdateNotification(cxt, NotificationIds.NOTIFICATION_ID_BXINFO, 
						CommonIntentAction.ACTION_NOTIFICATION_BXINFO, title, content, null, false);
//				Util.saveDataToLocate(cxt, "pushCode", pushCode);
				Util.saveDataToFile(cxt, null, "pushCode", pushCode.getBytes());
			}
		}
		catch(Throwable t)
		{
			
		}
	}

}
