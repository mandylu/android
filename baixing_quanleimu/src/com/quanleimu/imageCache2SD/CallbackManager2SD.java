/**
 *CallbackManager.java
 *2011-10-13 下午10:06:39
 *Touch Android
 *http://bbs.droidstouch.com
 */
package com.quanleimu.imageCache2SD;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.graphics.Bitmap;

public class CallbackManager2SD
{
private ConcurrentHashMap<String, List<ImageLoaderCallback2SD>> callbackMap;
	
	
	public CallbackManager2SD()
	{
		
		callbackMap = new ConcurrentHashMap<String, List<ImageLoaderCallback2SD>>();
	}
	
	
	
	public void put(String url,ImageLoaderCallback2SD callback)
	{
		
		if(!callbackMap.contains(url)){
			callbackMap.put(url, new ArrayList<ImageLoaderCallback2SD>());
		}
		callbackMap.get(url).add(callback);
		
	}
	
	public void callback(String url,Bitmap bitmap)
	{
		
		List<ImageLoaderCallback2SD> callbacks =callbackMap.get(url);
		
		if(null == callbacks)
			return;
		
		
		for (ImageLoaderCallback2SD callback : callbacks)
		{
			if(null != callback)
				callback.refresh(url, bitmap);
		}
		
		callbacks.clear();
		callbackMap.remove(url);
		
	}
}
