package com.xiaomi.mipush;

import com.xiaomi.mipush.sdk.MiPushClient;

import android.content.Context;

public class MiPushService {

	private static final String MiPush_APP_ID = "1001139";
	private static final String MiPush_APP_TOKEN = "800100193139";

	public static void initialize(Context context, String alias, String topic,
			MiPushCallback callback) {
		callback.setCategory(null);
		MiPushClient.initialize(context, MiPush_APP_ID, MiPush_APP_TOKEN, callback);
	}

}