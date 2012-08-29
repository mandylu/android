package com.quanleimu.util;

import com.quanleimu.activity.R;
import com.quanleimu.broadcast.CommonIntentAction;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.widget.Toast;

/**
 * 
 * @author liuchong
 *
 */
public class ViewUtil {
	
	public static void postShortToastMessage(final View parent, final String message, long delay)
	{
		if (parent == null || message == null)
		{
			return;
		}
		
		final long delayTime = delay > 0 ? delay : 0;
		
		parent.postDelayed(new Runnable() {

			public void run() {
				Toast.makeText(parent.getContext(), message, Toast.LENGTH_SHORT).show();
			}
			
		}, delayTime);
	}
	
	public static void postShortToastMessage(final View parent,final int resId, long delay)
	{
		if (parent == null)
		{
			return;
		}
		
		final long delayTime = delay > 0 ? delay : 0;
		
		parent.postDelayed(new Runnable() {

			public void run() {
				Toast.makeText(parent.getContext(), resId, Toast.LENGTH_SHORT).show();
			}
			
		}, delayTime);
	}
	
	public static void putOrUpdateNotification(final Context context, final int notificationId, String title, String msg, final Bundle extras, final boolean isPersistant)
	{
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		WakeLock wk = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "NOTIFY_WAKEUP");
		wk.acquire(1000);
		
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		String tickerText = msg;//message.getMessage();
		int icon = R.drawable.push_icon;

		String contentTitle = title == null ? "" : title;
		String contentText = msg;

		Intent notificationIntent = new Intent(CommonIntentAction.ACTION_NOTIFICATION_MESSAGE);
		if (extras != null)
		{
			notificationIntent.putExtras(extras);
		}
		PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification notification = new Notification(icon, tickerText,
				System.currentTimeMillis());
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		if (isPersistant)
		{
			notification.flags |= Notification.FLAG_NO_CLEAR;
		}
		else
		{
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
		}
		
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
		
		mNotificationManager.notify(notificationId, notification);
	}
}
