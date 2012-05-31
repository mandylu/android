/**
 *LazyImageLoader.java
 *2011-10-13 下午10:05:53
 *Touch Android
 *http://bbs.droidstouch.com
 */
package com.quanleimu.imageCache;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.quanleimu.activity.QuanleimuApplication;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;



public class LazyImageLoader
{

	private static final int MESSAGE_ID =1;
	public static final String EXTRA_IMG_URL="extra_img_url";
	public static final String EXTRA_IMG="extra_img";
	
	
	
	private ImageManager imgManger = new ImageManager(QuanleimuApplication.context);
	
	private Vector<String> urlDeque = new Vector<String>();
	
	private DownloadImageThread downloadImgThread = new DownloadImageThread();
	
	private CallbackManager callbackManager = new CallbackManager();

	public Bitmap get(String url,ImageLoaderCallback callback)
	{
		
		Bitmap bitmap = null;//ImageManager.userDefualtHead;
		
		if(imgManger.contains(url))
		{
			bitmap = imgManger.getFromCache(url);
//			
//			if(null == bitmap){
//				Log.d("simple image loader:", "image contained but is null");
//			}

			return bitmap;
		}
		
		else
		{
			callbackManager.put(url, callback);			
			startDownLoadTread(url);
	    }

		return bitmap;
	}
	
	public void AdjustPriority(ArrayList<String> urls){
		while(urls.size() > 0){
			String url = urls.remove(urls.size() - 1);
			
			if(urlDeque.remove(url)){
				urlDeque.add(0, url);
			}
		}
	}
	
	public void Cancel(List<String> urls){
		for(int i = 0; i < urls.size(); ++i){
			String url = urls.get(i);

			urlDeque.remove(url);
			callbackManager.remove(url);
			
		}	
	}
	
	public boolean checkWithImmediateIO(String url){
		
		Bitmap result = null;
		
		if(imgManger.contains(url)){
			result = imgManger.getFromCache(url); 			
		}		
		else{
			result = imgManger.safeGetFromFile(url);
	    }

		return (result != null);		
	}
	
	public Bitmap getWithImmediateIO(String url,ImageLoaderCallback callback){
		
		Bitmap result = null;
		
		if(imgManger.contains(url)){
			result = imgManger.getFromCache(url); 			
		}		
		else{
			result = imgManger.safeGetFromFile(url);
			
			if(result == null){
				callbackManager.put(url, callback);			
				startDownLoadTread(url);
			}
	    }

		return result;		
	}
	
	
	
	private void startDownLoadTread(String url)
	{
		putUrlToUrlQueue(url);
		
		State state = downloadImgThread.getState();
		
		if(state== State.NEW)
		{
			downloadImgThread.start();
		}
		else if(state == State.TERMINATED)
		{
			downloadImgThread = new DownloadImageThread();
			downloadImgThread.start();
		}
	}
	
	
	private void putUrlToUrlQueue(String url)
	{
		
		if(!urlDeque.contains(url))
		{
			urlDeque.add(url);
		}
	}
	
	
	
	Handler handler = new Handler()
	{
		
		public void handleMessage(android.os.Message msg) 
		{
			
			switch(msg.what)
			{
			
				case MESSAGE_ID :
				{
					
					Bundle bundle = msg.getData();
					
					String url =bundle.getString(EXTRA_IMG_URL);
					Bitmap bitmap = bundle.getParcelable(EXTRA_IMG);
			
					callbackManager.callback(url, bitmap);
					
					break;
				}
			}
			
		};
	};
	
	
	
	
	
	private  class DownloadImageThread extends Thread
	{
		
		
		private boolean isRun=true;
		
		public void shutDown()
		{
			isRun =false;
		}
		
		
		public void run()
		{
			try
			{
				while(isRun && urlDeque.size() > 0)
				{
					String url= urlDeque.remove(0);
					
					if(null == url){
						break;
					} 
					Bitmap bitmap=imgManger.safeGet(url);
					if(bitmap==null){
						
					}
					Message msg=handler.obtainMessage(MESSAGE_ID);
					Bundle bundle =msg.getData();
					bundle.putSerializable(EXTRA_IMG_URL, url);
					bundle.putParcelable(EXTRA_IMG, bitmap);
					handler.sendMessage(msg);
				}
			}
			catch (Exception e)
			{
			
			}
			finally
			{
				shutDown();
			}
			
		}
		
		
		
		
	}
	
	
}
