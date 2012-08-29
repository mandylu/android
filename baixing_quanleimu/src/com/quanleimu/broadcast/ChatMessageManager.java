package com.quanleimu.broadcast;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;

import com.quanleimu.database.ChatMessageDatabase;
import com.quanleimu.entity.ChatMessage;
import com.quanleimu.util.ViewUtil;

/**
 * 
 * @author liuchong
 *
 */
public class ChatMessageManager 
{
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
			
			
			ChatMessageDatabase.prepareDB(context);
			ChatMessageDatabase.storeMessage(msg);
			
			if (listener != null && listener.getUserId().equals(msg.getTo()))
			{
				listener.onNewMessage(msg);
			}
			else
			{
				final String titleText = jsonObj.has("text") ? jsonObj.getString("text") : "私信提醒";
				Bundle bundle = new Bundle();
				bundle.putBoolean("isTalking", true);
				bundle.putSerializable(CommonIntentAction.EXTRA_MSG_MESSAGE, msg);
				ViewUtil.putOrUpdateNotification(context, NotificationIds.NOTIFICATION_ID_CHAT_MESSAGE, titleText, msg.getMessage(), bundle, false);
//				showNotification(msg, titleText);
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
}
