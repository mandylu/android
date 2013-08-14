//zengming@baixing.com
package com.baixing.broadcast.push;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.baixing.broadcast.CommonIntentAction;
import com.baixing.broadcast.NotificationIds;
import com.baixing.data.GlobalDataManager;
import com.baixing.util.Version;
import com.baixing.util.ViewUtil;

/**
 * Created with IntelliJ IDEA.
 * User: zengming
 * Date: 12-11-9
 * Time: PM2:10
 * To change this template use File | Settings | File Templates.
 */
public class PushUpdateHandler extends PushHandler {
	
	private static final String TAG = PushUpdateHandler.class.getSimpleName();

    PushUpdateHandler(Context context) {
        super(context);
    }

    @Override
    public boolean acceptMessage(String type) {
        return "bxupdate".equals(type);
    }

    @Override
    public void processMessage(String message) {
    	Log.d(TAG, message);
    	
        try
        {   // { type:"bxupdate", data:{serverVersion:"3.1", apkUrl:"xxx", versionInfo:"yyy"} }
            JSONObject json = new JSONObject(message);

            JSONObject data = json.getJSONObject("d");

//            String serverVersion = updateInfo.getString("serverVersion");
            String serverVersion = data.getString("serverVersion");
            String apkUrl = data.getString("apkUrl");
            
            Log.i(TAG, data + " : " + apkUrl);

            Pattern p = Pattern.compile("http(s)?://\\w+");
            Matcher matcher = p.matcher(apkUrl);
            if (matcher.find() == false) {
                return;
            }
            
            if (Version.compare(serverVersion, GlobalDataManager.getInstance().getVersion()) == 1) {
            	String title = "百姓网有新版本啦~";
    			if (data.has("title")) {
    				title = data.getString("title");
    			}
    			
    			String content = "赶紧去更新";
    			if (data.has("content")) {
    				content = data.getString("content");
    			}
    			
                Bundle extral = new Bundle();
                extral.putString("apkUrl", apkUrl);
                ViewUtil.putOrUpdateNotification(cxt, NotificationIds.NOTIFICATION_ID_UPGRADE, CommonIntentAction.ACTION_NOTIFICATION_UPGRADE, 
                		title, content, extral, false);
            }
        }
        catch(Exception ex)
        {
        	Log.e(TAG, ex.toString());
        }
    }
}
