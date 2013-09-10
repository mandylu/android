package com.baixing.sharing.referral;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;

import com.baixing.util.Util;

public class ReferralBroadcastReceiver extends BroadcastReceiver {

	public static final String ACTION_SEND_MSG = "com.baixing.sharing.referral.action.SEND_MSG";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (ACTION_SEND_MSG.equals(intent.getAction())) {
			sendMsgFromLocal(intent.getStringExtra("phoneNumber"), context);
		}
	}

	private void sendMsgFromLocal(String phoneNumber, Context context) {
		SmsManager smsManager = SmsManager.getDefault();
		String smsText = "我是百姓网推广员，有事请打这个电话：" + Util.getDevicePhoneNumber();
		smsManager.sendTextMessage(phoneNumber, null, smsText, null, null);
	}
}
