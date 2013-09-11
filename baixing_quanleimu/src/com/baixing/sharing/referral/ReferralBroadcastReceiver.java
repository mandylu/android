package com.baixing.sharing.referral;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

import com.baixing.anonymous.AnonymousExecuter;
import com.baixing.anonymous.BaseAnonymousLogic;
import com.baixing.util.Util;
import com.baixing.util.post.PostCommonValues;

public class ReferralBroadcastReceiver extends BroadcastReceiver {

	public static final String ACTION_SEND_MSG = "com.baixing.sharing.referral.action.SEND_MSG";
	public static final String ACTION_SENT_POST = "com.baixing.sharing.referral.action.SENT_POST";

	@Override
	public void onReceive(Context context, Intent intent) {
		String phoneNumber = intent.getStringExtra("phoneNumber");
		if (ACTION_SEND_MSG.equals(intent.getAction())) {
			sendMsgFromLocal(phoneNumber, context);
		} else if (ACTION_SENT_POST.equals(intent.getAction())) {
			new ReferralUtil().updateReferral("post");
		}
	}

	private void sendMsgFromLocal(String phoneNumber, Context context) {
		SmsManager smsManager = SmsManager.getDefault();
		String smsText = "我是百姓网推广员，有事请打这个电话：" + Util.getDevicePhoneNumber();
		smsManager.sendTextMessage(phoneNumber, null, smsText, null, null);
	}
}
