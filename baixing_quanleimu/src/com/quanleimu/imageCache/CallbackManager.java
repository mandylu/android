/**
 *CallbackManager.java
 *2011-10-13 下午10:06:39
 *Touch Android
 *http://bbs.droidstouch.com
 */
package com.quanleimu.imageCache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;
import android.util.Log;

public class CallbackManager
{
private ConcurrentHashMap<String, List<ImageLoaderCallback>> callbackMap;
	
	
	public CallbackManager()
	{
		
		callbackMap = new ConcurrentHashMap<String, List<ImageLoaderCallback>>();
	}
	
	public void remove(String url){
		callbackMap.remove(url);
	}
	
	public void put(String url,ImageLoaderCallback callback)
	{
		
		if(null == callbackMap.get(url)){
			callbackMap.put(url, new ArrayList<ImageLoaderCallback>());
			//Log.d("simple image loader: ", "url: "+url+"   => callback array created: ");
		}
//		else
//		{
//			Log.d("simple image loader: ", "url: "+url+"   => callback array existing: ");
//		}
		callbackMap.get(url).add(callback);
		
//		Log.d("simple image loader: ", "url: "+url+"   => callback count is now: "+ callbackMap.get(url).size());
		
	}
	
	public void callback(String url,Bitmap bitmap)
	{
		
		List<ImageLoaderCallback> callbacks =callbackMap.get(url);
		
		if(null == callbacks)
			return;
		
		
		for (ImageLoaderCallback callback : callbacks)
		{
			if(null != callback){
				callback.refresh(url, bitmap);
//				Log.d("simple image loader: ", "url: "+url+"   => callback is valid and called once");
			}
//			else
//			{
//				Log.d("simple image loader: ", "url: "+url+"   => callback is null");
//			}
		}
		
		callbacks.clear();
		callbackMap.remove(url);
		
	}
}
