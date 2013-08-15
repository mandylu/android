package com.xiaomi.mipush;

import java.util.List;

import android.content.Context;
import android.util.Log;

import com.baixing.broadcast.push.PushDispatcher;
import com.baixing.network.api.ApiError;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.network.api.BaseApiCommand.Callback;
import com.xiaomi.mipush.sdk.ErrorCode;
import com.xiaomi.mipush.sdk.MiPushClient.MiPushClientCallback;

public class MiPushCallback extends MiPushClientCallback {

	private static final String TAG = MiPushCallback.class.getSimpleName();

	private PushDispatcher pushDispatcher;
	private Context context;

	public MiPushCallback(PushDispatcher dispatcher) {
		this.pushDispatcher = dispatcher;
	}

	public String getCategory() {
		return super.getCategory();
	}

	public void setCategory(String category) {
		super.setCategory(category);
	}

	public void setContext(Context context) {
		this.context = context;
	}

	@Override
	public void onCommandResult(String command, long resultCode, String reason,
			List<String> params) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onInitializeResult(long resultCode, String reason, String regID) {
		if (resultCode == ErrorCode.SUCCESS) {
			Log.i(TAG, "regID = " + regID);
			register(regID);
		} else {
			Log.e(TAG, reason);
		}
	}

	@Override
	public void onReceiveMessage(String content, String topic, String alias) {
		Log.i(TAG, "json = " + content);
		pushDispatcher.dispatch(content);
	}

	@Override
	public void onSubscribeResult(long resultCode, String reason, String topic) {
		if (resultCode == ErrorCode.SUCCESS) {
			Log.i(TAG, "subscribed topic: " + topic);
		} else {
			Log.e(TAG, reason);
		}
	}

	@Override
	public void onUnsubscribeResult(long resultCode, String reason, String topic) {
		if (resultCode == ErrorCode.SUCCESS) {
			Log.i(TAG, "unsubscribed topic: " + topic);
		} else {
			Log.e(TAG, reason);
		}
	}

	public void register(final String regID) {
		Log.d(TAG, "registerDevice: " + regID);

		ApiParams params = new ApiParams();
		params.addParam("deviceToken", regID);
		Log.i(TAG, "params: " + params.toString());

		BaseApiCommand.createCommand("tokenupdate", true, params).execute(
				context, new Callback() {

					@Override
					public void onNetworkFail(String apiName, ApiError error) {
						Log.e(TAG, error.toString());
					}

					@Override
					public void onNetworkDone(String apiName,
							String responseData) {
						Log.d(TAG, "updatetoken succed " + responseData);
					}
				});
	}

}
