package com.quanleimu.broadcast;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.quanleimu.activity.R;
import com.quanleimu.database.ChatMessageDatabase;
import com.quanleimu.entity.ChatMessage;

/**
 * 
 * @author liuchong
 *
 */
public class ChatMessageManager 
{
	public static final int NOTIFICATION_ID_MESSAGE = 1001;
	private ChatMessageListener listener;
	private Context context;
	ChatMessageManager(Context context)
	{
		this.context = context;
	}
	
	public void handleChatMessage(String msgJson)
	{
		//TODO: check if the msg is chat message.
		try {
			JSONObject jsonObj = new JSONObject(msgJson);
			if (jsonObj == null || !jsonObj.has("type") || !"bxmessage".equals(jsonObj.get("type")))
			{
				return;
			}
			
			ChatMessage msg = ChatMessage.fromJson(jsonObj.getString("data"));
			
			final String titleText = jsonObj.getString("text");
			
			ChatMessageDatabase.prepareDB(context);
			ChatMessageDatabase.storeMessage(msg);
			
			if (listener != null && listener.getUserId().equals(msg.getTo()))
			{
				listener.onNewMessage(msg);
			}
			else
			{
				showNotification(msg, titleText);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void setMessageListener(ChatMessageListener msgListener)
	{
		this.listener = msgListener;
	}
	
	public void removeMessageListener(ChatMessageListener msgListener)
	{
		if (this.listener == msgListener)
		{
			this.listener = null;
		}
	}
	
	public static interface ChatMessageListener
	{
		String getSessionId();
		String getUserId();
		void onNewMessage(ChatMessage msg);
	}
	
	private void showNotification(ChatMessage message, String notifyTitle) {
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		
		String tickerText = message.getMessage();
		int icon = R.drawable.push_icon;

		String contentTitle = notifyTitle == null ? "私信提醒" : notifyTitle;
		String contentText = message.getMessage();

		Intent notificationIntent = new Intent(CommonIntentAction.ACTION_NOTIFICATION_MESSAGE);
		notificationIntent.putExtra("isTalking", true);
		notificationIntent.putExtra(CommonIntentAction.EXTRA_MSG_MESSAGE, message);
		notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification notification = new Notification(icon, tickerText,
				System.currentTimeMillis());
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);
		
		mNotificationManager.notify(NOTIFICATION_ID_MESSAGE, notification);
	}
}
