package com.quanleimu.broadcast.push;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.QuanleimuMainActivity;
import com.quanleimu.activity.R;
import com.quanleimu.broadcast.BXStartupIntentReceiver;
import com.quanleimu.broadcast.CommonIntentAction;
import com.quanleimu.broadcast.NotificationIds;
import com.quanleimu.util.Util;
import com.quanleimu.util.Version;
import com.quanleimu.util.ViewUtil;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: zengming
 * Date: 12-11-9
 * Time: PM2:10
 * To change this template use File | Settings | File Templates.
 */
public class PushUpdateHandler extends PushHandler {

    PushUpdateHandler(Context context) {
        super(context);
    }

    @Override
    public boolean acceptMessage(String type) {
        return "bxupdate".equals(type);
    }

    @Override
    public void processMessage(String message) {
        try
        {   // { type:"bxupdate", data:{serverVersion:"3.1", apkUrl:"xxx", versionInfo:"yyy"} }
            JSONObject json = new JSONObject(message);

            JSONObject data = json.getJSONObject("data");

//            String serverVersion = updateInfo.getString("serverVersion");
            String serverVersion = data.getString("serverVersion");
            String apkUrl = data.getString("apkUrl");
            String versionInfo = data.getString("versionInfo");

            Pattern p = Pattern.compile("http(s)?://\\w+");
            Matcher matcher = p.matcher(apkUrl);
            if (matcher.find() == false) {
                return;
            }

            if (Version.compare(serverVersion, QuanleimuApplication.version) == 1) {
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(cxt)
                                .setSmallIcon(R.drawable.app_icon)
                                .setContentTitle("百姓网客户端新版本啦~")
                                .setContentText("去看看");

                Intent resultIntent = new Intent(cxt, BXStartupIntentReceiver.class);
                resultIntent.setAction("bxupdate");
                resultIntent.putExtra("apkUrl", apkUrl);

                PendingIntent resultPendingIntent = PendingIntent.getBroadcast(cxt, 200, resultIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
                mBuilder.setContentIntent(resultPendingIntent);
                NotificationManager mNotificationManager =
                        (NotificationManager) cxt.getSystemService(Context.NOTIFICATION_SERVICE);
// mId allows you to update the notification later on.
                mNotificationManager.notify(0, mBuilder.getNotification());
            }
        }
        catch(Exception ex)
        {

        }
    }
}
