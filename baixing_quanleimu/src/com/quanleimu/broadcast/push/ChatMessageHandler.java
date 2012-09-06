package com.quanleimu.broadcast.push;

import org.json.JSONException;
import org.json.JSONObject;

import com.quanleimu.activity.QuanleimuMainActivity;
import com.quanleimu.broadcast.CommonIntentAction;
import com.quanleimu.broadcast.NotificationIds;
import com.quanleimu.database.ChatMessageDatabase;
import com.quanleimu.entity.ChatMessage;
import com.quanleimu.util.ViewUtil;
import com.quanleimu.view.TalkView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ChatMessageHandler extends PushHandler {

	ChatMessageHandler(Context context) {
		super(context);
	}

	@Override
	public boolean acceptMessage(String type) {
		return "bxmessage".equals(type) && isAuthenticated();
	}

	@Override
	public void processMessage(String message) {

		try {
			JSONObject jsonObj = new JSONObject(message);
			
			ChatMessage msg = ChatMessage.fromJson(jsonObj.getString("data"));
			
			//Store it
			ChatMessageDatabase.prepareDB(cxt);
			ChatMessageDatabase.storeMessage(msg);
			
			//Broadcast in application.
			Intent intent = new Intent(CommonIntentAction.ACTION_BROADCAST_NEW_MSG);
			intent.putExtra(CommonIntentAction.EXTRA_MSG_MESSAGE, msg);
			cxt.sendBroadcast(intent);
			
			//Update UI or schedule status bar notification.
			if (TalkView.CURRENT_RECEIVER_RRICKY != null && 
					msg.getFrom().equals(TalkView.CURRENT_RECEIVER_RRICKY))
			{
				//TODO: update badge count.
			}
			else if (!isUIActive(QuanleimuMainActivity.class.getName()))
			{
				final String titleText = jsonObj.has("text") ? jsonObj.getString("text") : "私信提醒";
				Bundle bundle = new Bundle();
				bundle.putBoolean("isTalking", true);
				bundle.putSerializable(CommonIntentAction.EXTRA_MSG_MESSAGE, msg);
				ViewUtil.putOrUpdateNotification(cxt, NotificationIds.NOTIFICATION_ID_CHAT_MESSAGE, CommonIntentAction.ACTION_NOTIFICATION_MESSAGE, titleText, msg.getMessage(), bundle, false);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	
	}

}
