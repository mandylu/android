package com.baixing.sharing.referral;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.baixing.activity.PersonalActivity;
import com.baixing.broadcast.NotificationIds;
import com.quanleimu.activity.R;

public class ReferralNotification {
	
	public static void showNotification(Context context, String message) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.push_icon, message, System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.defaults = Notification.DEFAULT_ALL;
		
		Intent notificationIntent = new Intent(context, PersonalActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.setLatestEventInfo(context, message, message, contentIntent);
		
		notificationManager.notify(NotificationIds.NOTIFICATION_ID_BXINFO, notification);
	}
	
	public static void cancelNotification(Context context) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(android.content.Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(NotificationIds.NOTIFICATION_ID_BXINFO);
	}

}
