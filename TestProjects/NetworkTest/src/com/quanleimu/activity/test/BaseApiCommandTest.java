package com.quanleimu.activity.test;

import java.util.HashMap;

import android.test.AndroidTestCase;

import com.baixing.entity.AdList;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.network.ICacheProxy;
import com.baixing.network.api.ApiConfiguration;
import com.baixing.network.api.ApiError;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.network.api.BaseApiCommand.Callback;

public class BaseApiCommandTest extends AndroidTestCase {
	HashMap<String, String> cache = new HashMap<String, String>();
	@Override
	public void setUp(){
		ApiConfiguration.config("www.baixing.com", new ICacheProxy(){

			@Override
			public void onSave(String url, String data) {
				// TODO Auto-generated method stub
				cache.put(url, data);
			}

			@Override
			public String onLoad(String url) {
				// TODO Auto-generated method stub
				return cache.get(url);
			}
			
		}, "api_mobile_android", "c6dd9d408c0bcbeda381d42955e08a3f");
		BaseApiCommand.init("fakeudid", "87104556", "3.1", "baixingtest", "shanghai", "networktest");
	}
	
	private boolean asyncRequestReturned = false; 
	
	private Thread startThreadWaitingForAsyncRequest(){
		Thread t = new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(!asyncRequestReturned){
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		});
		
		t.start();
		
		return t;
	}
	public void testAsync(){
		asyncRequestReturned = false;
		
		Thread t = startThreadWaitingForAsyncRequest();
		
		ApiParams params = new ApiParams();
		params.addParam("rows", 30);
		BaseApiCommand cmd = BaseApiCommand.createCommand("ad_list", true, params);
		cmd.execute(mContext, new Callback(){

			@Override
			public void onNetworkDone(String apiName, String responseData) {
				// TODO Auto-generated method stub
				String response = responseData;
				assertNotNull(response);
				assertTrue(JsonUtil.getGoodsListFromJson(response).getData().size() > 0);
				asyncRequestReturned = true;
			}

			@Override
			public void onNetworkFail(String apiName, ApiError error) {
				// TODO Auto-generated method stub
				asyncRequestReturned = true;
			}
			
		});

		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testGetSync() {
		ApiParams params = new ApiParams();
		params.addParam("rows", 30);
		BaseApiCommand cmd = BaseApiCommand.createCommand("ad_list", true, params);
		String response = cmd.executeSync(mContext);
		AdList ads = JsonUtil.getGoodsListFromJson(response);
		assertNotNull(ads);
		AndroidTestCase.assertTrue(ads.getData().size() > 0);		
	}
	
	public void testPostSync() {
		ApiParams params = new ApiParams();
		params.addParam("rows", 30);
		BaseApiCommand cmd = BaseApiCommand.createCommand("ad_list", false, params);
		String response = cmd.executeSync(mContext);
		AdList ads = JsonUtil.getGoodsListFromJson(response);
		assertNotNull(ads);
		AndroidTestCase.assertTrue(ads.getData().size() > 0);		
	}
	
	public void testException(){
		asyncRequestReturned = false;
		Thread t = this.startThreadWaitingForAsyncRequest();
		ApiParams params = new ApiParams();
		params.addParam("rows", 30);
		BaseApiCommand cmd = BaseApiCommand.createCommand("getUser", false, params);
		cmd.execute(mContext, new Callback(){

			@Override
			public void onNetworkDone(String apiName, String responseData) {
				// TODO Auto-generated method stub
				AndroidTestCase.assertTrue("on networkdone shoun't be called here", false);
				asyncRequestReturned = true;
			}

			@Override
			public void onNetworkFail(String apiName, ApiError error) {
				// TODO Auto-generated method stub
				assertTrue(error.getErrorCode().equals("503"));
				asyncRequestReturned = true;
			}
		});
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void testErrorHandler(){
		asyncRequestReturned = false;
		Thread t = this.startThreadWaitingForAsyncRequest();

		ApiParams params = new ApiParams();
		params.addParam("rows", 30);
		params.addParam("mobile", "110");
		BaseApiCommand cmd = BaseApiCommand.createCommand("getUser", false, params);
		cmd.execute(mContext, new Callback(){

			@Override
			public void onNetworkDone(String apiName, String responseData) {
				// TODO Auto-generated method stub
				AndroidTestCase.assertTrue("on networkdone shoun't be called here", false);
				asyncRequestReturned = true;
			}

			@Override
			public void onNetworkFail(String apiName, ApiError error) {
				// TODO Auto-generated method stub
				AndroidTestCase.assertEquals(error.getErrorCode(), "1");
				asyncRequestReturned = true;
			}
			
		});
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	public void testCancel(){
		ApiParams params = new ApiParams();
		params.addParam("rows", 30);
		params.addParam("mobile", "110");
		BaseApiCommand cmd = BaseApiCommand.createCommand("getUser", false, params);
		cmd.execute(mContext, new Callback(){

			@Override
			public void onNetworkDone(String apiName, String responseData) {
				// TODO Auto-generated method stub
				AndroidTestCase.assertTrue("on networkdone shoun't be called here", false);
			}

			@Override
			public void onNetworkFail(String apiName, ApiError error) {
				// TODO Auto-generated method stub
				AndroidTestCase.assertTrue("on networkdone shoun't be called here", false);
			}
			
		});
		cmd.cancel();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void testCache(){
		cache.clear();
		ApiParams params = new ApiParams();
		params.addParam("rows", 30);
		params.useCache = true;
		
		BaseApiCommand cmd = BaseApiCommand.createCommand("ad_list", true, params);
		String response = cmd.executeSync(mContext);
		
		assertTrue(cache.size() == 1);
		Object[] key = cache.keySet().toArray();
		assertTrue(response.equals(cache.get((String)key[0])));
		
		final String customValue = "it's a changed value";
		cache.put((String)key[0], customValue);

		cmd = BaseApiCommand.createCommand("ad_list", true, params);
		response = cmd.executeSync(mContext);
		assertEquals(response, customValue);
	}
}