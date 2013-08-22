//liuchong@baixing.com
package com.baixing.broadcast.push;

import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.baixing.broadcast.CommonIntentAction;
import com.baixing.broadcast.NotificationIds;
import com.baixing.util.Util;
import com.baixing.util.ViewUtil;

/**
 * 
 * @author liuchong
 *
 */
public class PushDispatcher {
	
	private static final String TAG = PushDispatcher.class.getSimpleName();
	
	private Context context;
	private PushHandler[] handlers;// = new ArrayList<PushHandler>();
	public PushDispatcher(Context context)
	{
		this.context = context;
		handlers = new PushHandler[] {
				new BXInfoHandler(context),
                new PushUpdateHandler(context),
				new UrlHandler(context)
		};
	}
	
	public void dispatch(String msgJson)
	{
        Log.d("push", msgJson);
        Boolean showDebugPush = (Boolean) Util.loadDataFromLocate(context, "showDebugPush", Boolean.class);
        if (showDebugPush != null && showDebugPush) {
            ViewUtil.putOrUpdateNotification(context,
                    NotificationIds.NOTIFICATION_ID_DEBUG,
                    CommonIntentAction.ACTION_NOTIFICATION_DEBUG,
                    "收到 push", msgJson, new Bundle(), false);
        }
		try
		{
			JSONObject jsonObj = new JSONObject(msgJson);
			if (jsonObj == null || !jsonObj.has("a"))
			{
				return;
			}
			
			final String type = jsonObj.getString("a");
			for (PushHandler h : handlers)
			{
				if (h.acceptMessage(type))
				{
					Log.d(TAG, "type = " + type);
					try {
						h.processMessage(msgJson);
					}
					catch(Throwable t)
					{
						Log.e(TAG, t.toString());
					}
				}
			}
		}
		catch(Throwable t)
		{
			
		}
	}
	
}
