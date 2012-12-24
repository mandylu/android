package com.baixing.api.test;

import org.json.JSONObject;
import android.util.Log;

import com.baixing.database.ChatMessageDatabase;
import com.baixing.entity.ChatMessage;

import android.test.AndroidTestCase;
import com.baixing.android.api.ApiClient;
import com.baixing.android.api.ApiError;
import com.baixing.android.api.ApiListener;
import com.baixing.android.api.ApiParams;

public class ApiClientTest extends AndroidTestCase implements ApiListener {
	public void setUp()
	{
		try {
			super.setUp();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		ApiClient.getInstance().init(getContext(), "udid_for_test", "3.2", "unittest", "shanghai", null);
		
	}
	
	public void testadlist()
	{
		ApiParams ap = new ApiParams();
		ap.addParam("query", "cityEnglishName:shanghai categoryEnglishName:shouji");
		ApiClient.getInstance().remoteCall("ad_list", ap, this);
		
		synchronized(this){
			try{
				this.wait();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	
	public void onComplete(JSONObject json, String rawData){
		Log.d("onComplete", json.toString());
		Log.d("onComplete", "rawData == " + rawData);
		synchronized(this){
			this.notifyAll();
		}
	}
	public void onError(ApiError error){
		Log.d("onError", error.toString());
		synchronized(this){
			this.notifyAll();
		}
	}
	public void onException(Exception e){
		Log.d("onException", e.getMessage());
		e.printStackTrace();
		synchronized(this){
			this.notifyAll();
		}
	}
	
	
}
