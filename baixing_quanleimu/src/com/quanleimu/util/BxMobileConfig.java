package com.quanleimu.util;

import java.util.ArrayList;

import com.quanleimu.activity.QuanleimuApplication;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BxMobileConfig {
	
	private static BxMobileConfig instance;
	private  String response;
	
	private final static int CONFIG_ON = 2;
	private Handler handler;
	
	//constructor
	private BxMobileConfig() {
		//handler
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == CONFIG_ON) {
					Log.d("BXM", "Come with Track & Sender");
					//生成BxTracker和BxSender
					BxSender sender = new BxSender(QuanleimuApplication.context);
					Thread senderThread = new Thread(sender);
					senderThread.start();
					BxTracker tracker = BxTracker.getInstance();
					tracker.initialize(QuanleimuApplication.context, sender);
				}
				super.handleMessage(msg);
			}
			
		};
		
		//thread
		new Thread(new configRunnable()).start();
	}
	
	public static BxMobileConfig getInstance() {
		if (instance==null) {
			instance = new BxMobileConfig();
		}
		return instance;
	}
	
	private void sendMessage(int what) {
		Message message = null;
		if (handler != null) {
			message = handler.obtainMessage();
			message.what = what;
			handler.sendMessage(message);
		}
	}
	
	class configRunnable implements Runnable {

		@Override
		public void run() {
			String apiName = "mobile_config";
			String url = Communication.getApiUrl(apiName, new ArrayList<String>());
			try {
				response = Communication.getDataByUrl(url, true);
				System.out.println(response);
				if (response != null && response.equals("\"true\"")) {
//					System.out.println("response:true");
					BxMobileConfig.getInstance().sendMessage(CONFIG_ON);
				}else{
//					System.out.println("response:none");
				}
			} catch (Exception e) {
//				System.out.println("response:exception");
			}
		}
		
	}
}
