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
			send(intent.getStringExtra("phoneNumber"), context);
		}
	}

	private void send(String phoneNumber, Context context) {
		SmsManager smsManager = SmsManager.getDefault();
		String smsText = "我是百姓网推广员，有事请打这个电话：" + Util.getDevicePhoneNumber();
		/*String SENT_SMS_ACTION = "SENT_SMS_ACTION";
		String DELIVERED_SMS_ACTION = "DELIVERED_SMS_ACTION";
		// 处理发送成功逻辑
		Intent sentIntent = new Intent(SENT_SMS_ACTION);
		PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
				sentIntent, 0);
		context.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context _context, Intent _intent) {
				switch (getResultCode()) {
				case Activity.RESULT_OK:
					Toast.makeText(_context, "短信发送成功", Toast.LENGTH_SHORT)
							.show();
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					break;
				}
				_context.unregisterReceiver(this);
			}
		}, new IntentFilter(SENT_SMS_ACTION));
		// 处理接收成功逻辑
		Intent deliverIntent = new Intent(DELIVERED_SMS_ACTION);
		PendingIntent deliverPI = PendingIntent.getBroadcast(context, 0,
				deliverIntent, 0);
		context.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context _context, Intent _intent) {
				Toast.makeText(_context, "收信人已经成功接收", Toast.LENGTH_SHORT)
						.show();
				_context.unregisterReceiver(this);
			}
		}, new IntentFilter(DELIVERED_SMS_ACTION));*/

		smsManager.sendTextMessage(phoneNumber, null, smsText, null,
				null);
	}
}
