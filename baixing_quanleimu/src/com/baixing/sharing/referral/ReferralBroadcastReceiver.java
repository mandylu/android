package com.baixing.sharing.referral;

import com.baixing.util.Util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;
import android.widget.Toast;

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
