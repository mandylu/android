//xumengyi@baixing.com
package com.baixing.imageCache;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import android.graphics.Bitmap;

class CallbackManager{
	private ConcurrentHashMap<String, List<ImageLoaderCallback>> callbackMap;
	public CallbackManager(){		
		callbackMap = new ConcurrentHashMap<String, List<ImageLoaderCallback>>();
	}
	
	public void remove(String url){
		callbackMap.remove(url);
	}
	
	//return: true if passed url is no more valid and thus should be removed from request queues, false otherwise
	public boolean remove(String url, Object object){
		List<ImageLoaderCallback> callbackList = callbackMap.get(url);
		
		if(null != callbackList){
			for(ImageLoaderCallback callback : callbackList){
				if(null != callback && callback.getObject() == object){
					callbackList.remove(callback);
					break;
				}
			}
			
			if(0 == callbackList.size()){
				callbackMap.remove(callbackList);
				return true;
			}
			return false;
		}
		return true;
	}
	
	public void put(String url,ImageLoaderCallback callback){		
		if(null == callbackMap.get(url)){
			callbackMap.put(url, new ArrayList<ImageLoaderCallback>());
		}
		callbackMap.get(url).add(callback);
	}
	
	public void callback(String url,Bitmap bitmap){		
		List<ImageLoaderCallback> callbacks =callbackMap.get(url);		
		if(null == callbacks)
			return;
		for (ImageLoaderCallback callback : callbacks){
			if(null != callback){
				callback.refresh(url, bitmap);
			}
		}	
		callbacks.clear();
		callbackMap.remove(url);		
	}
	
	public void fail(String url){		
		List<ImageLoaderCallback> callbacks =callbackMap.get(url);		
		if(null == callbacks)
			return;
		for (ImageLoaderCallback callback : callbacks){
			if(null != callback){
				callback.fail(url);
			}
		}
	}
}
