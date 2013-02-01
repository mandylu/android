//liuchong@baixing.com
package com.baixing.broadcast.push;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;
import com.baixing.broadcast.CommonIntentAction;
import com.baixing.broadcast.NotificationIds;
import com.baixing.util.Communication;
import com.baixing.util.Util;
import com.baixing.util.ViewUtil;
import com.quanleimu.activity.R;
import org.json.JSONObject;

import android.content.Context;

/**
 * 
 * @author liuchong
 *
 */
public class PushDispatcher {
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
        Boolean showDebugPush = (Boolean) Util.loadDataFromLocate(context, "showDebugPush", Boolean.class);
        if (showDebugPush) {
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
					try {
						h.processMessage(msgJson);
					}
					catch(Throwable t)
					{
						//Ignor, handler do not affect each other.
					}
				}
			}
		}
		catch(Throwable t)
		{
			
		}
	}
	
}
