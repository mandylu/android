/**
 *LazyImageLoader.java
 *2011-10-13 下午10:05:53
 *Touch Android
 *http://bbs.droidstouch.com
 */
package com.quanleimu.imageCache2SD;

import java.lang.Thread.State;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.quanleimu.activity.MyApplication;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class LazyImageLoader2SD
{

	private static final int MESSAGE_ID =1;
	public static final String EXTRA_IMG_URL="extra_img_url";
	public static final String EXTRA_IMG="extra_img";
	
	
	
	private ImageManager2SD imgManger = new ImageManager2SD(MyApplication.context);
	
	private BlockingQueue<String> urlQueue = new ArrayBlockingQueue<String>(50);
	
	private DownloadImageThread downloadImgThread = new DownloadImageThread();
	
	private CallbackManager2SD callbackManager = new CallbackManager2SD();
	
	
	
	public Bitmap get(String url,ImageLoaderCallback2SD callback)
	{
		
Bitmap bitmap = ImageManager2SD.userDefualtHead;
		if(imgManger.contains(url))
		{
			bitmap = imgManger.getFromCache(url);

			return bitmap;
		}
		
		else
		{
			callbackManager.put(url, callback);			
			startDownLoadTread(url);
	    }

		return bitmap;
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
		
		if(!urlQueue.contains(url))
		{
			try
			{
				urlQueue.put(url);
				
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			
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
				while(isRun)
				{
					String url= urlQueue.poll();
					
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
