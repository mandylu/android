package com.quanleimu.broadcast.push;

import com.tencent.mm.sdk.platformtools.Log;

import android.content.Context;

public class BXInfoHandler extends PushHandler {

	BXInfoHandler(Context context) {
		super(context);
	}

	@Override
	public boolean acceptMessage(String type) {
		return "bxinfo".equals(type);
	}

	@Override
	public void processMessage(String message) {
		// TODO Auto-generated method stub
		Log.d("BXInfoHandler", "processing message : " + message);
	}

}
