//liuchong@baixing.com
package com.baixing.broadcast.push;

import org.json.JSONException;
import org.json.JSONObject;

import com.baixing.activity.QuanleimuMainActivity;
import com.baixing.broadcast.CommonIntentAction;
import com.baixing.broadcast.NotificationIds;
import com.baixing.database.ChatMessageDatabase;
import com.baixing.entity.ChatMessage;
import com.baixing.util.ViewUtil;
import com.baixing.view.fragment.TalkFragment;

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
			if (TalkFragment.CURRENT_RECEIVER_TRICKY != null && 
					msg.getFrom().equals(TalkFragment.CURRENT_RECEIVER_TRICKY))
			{
				//TODO: update badge count.
			}
			else/* if (!isUIActive(QuanleimuMainActivity.class.getName()))*/
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
