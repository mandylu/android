package com.xiaomi.mipush;

import java.util.List;

import android.util.Log;

import com.baixing.broadcast.push.PushDispatcher;
import com.xiaomi.mipush.sdk.MiPushClient.MiPushClientCallback;

public class MiPushCallback extends MiPushClientCallback {
	
	private static final String TAG = MiPushCallback.class.getSimpleName();
	
	private PushDispatcher pushDispatcher;
	
	public MiPushCallback(PushDispatcher dispatcher) {
		this.pushDispatcher = dispatcher;
	}
	
	public String getCategory() {
		return super.getCategory();
	}

	public void setCategory(String category) {
		super.setCategory(category);
	}

	@Override
	public void onCommandResult(String command, long resultCode, String reason,
			List<String> params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInitializeResult(long resultCode, String reason, String regID) {
		Log.i(TAG, "regID = " + regID);
		// registeDevice
	}

	@Override
	public void onReceiveMessage(String content, String topic, String alias) {
		Log.i(TAG, "json = " + content);
		pushDispatcher.dispatch(content);
	}

	@Override
	public void onSubscribeResult(long resultCode, String reason, String topic) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUnsubscribeResult(long resultCode, String reason, String topic) {
		// TODO Auto-generated method stub

	}

}
