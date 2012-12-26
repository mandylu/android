//xumengyi@baixing.com
package com.baixing.imageCache;

import java.lang.Thread.State;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
public class ImageLoaderManager{

	private static final int MESSAGE_ID =1;
	public static final int MESSAGE_FAIL = 2;
	public static final String EXTRA_IMG_URL="extra_img_url";
	public static final String EXTRA_IMG="extra_img";
	private Vector<String> urlDequeDiskIO = new Vector<String>();
	private DiskIOImageThread diskIOImgThread = new DiskIOImageThread();	
	private Vector<String> urlDequeDownload = new Vector<String>();	
	private DownloadImageThread[] downloadImgThread = 
			new DownloadImageThread[]{new DownloadImageThread(), new DownloadImageThread(), new DownloadImageThread()}; 	
	private CallbackManager callbackManager = new CallbackManager();
	
	static ImageLoaderManager instance;
	static public ImageLoaderManager getInstance(){
		if(instance == null){
			instance = new ImageLoaderManager();
		}
		return instance;
	}
	
	public WeakReference<Bitmap> get(String url,ImageLoaderCallback callback, final int defaultImgRes){
		WeakReference<Bitmap> bitmap = null;//ImageManager.userDefualtHead;
		
		//1. try to get from memory cache
		if(ImageCacheManager.getInstance().contains(url)){
			bitmap = ImageCacheManager.getInstance().getFromMemoryCache(url);
		}
		
		if(bitmap!=null){//if found in memory cache, just return that to the caller
			return bitmap;
		}else{//else, try try to load from disk cache
			callbackManager.put(url, callback);			
			startFetchingTread(url);
	    }		
		return bitmap;
	}
	
	public WeakReference<Bitmap> get(String url,ImageLoaderCallback callback){
		WeakReference<Bitmap> bitmap = null;//ImageManager.userDefualtHead;
		
		//1. try to get from memory cache
		if(ImageCacheManager.getInstance().contains(url)){
			bitmap = ImageCacheManager.getInstance().getFromMemoryCache(url);
		}
		if(bitmap != null && bitmap.get() != null){//if found in memory cache, just return that to the caller
			return bitmap;
		}else{//else, try try to load from disk cache
			callbackManager.put(url, callback);			
			startFetchingTread(url);
	    }		
		return bitmap;
	}
	
	public void AdjustPriority(ArrayList<String> urls){
		while(urls.size() > 0){
			String url = urls.remove(urls.size() - 1);
			if(urlDequeDiskIO.remove(url)){
				urlDequeDiskIO.add(0, url);
			}
			if(urlDequeDownload.remove(url)){
				urlDequeDownload.add(0, url);
			}			
		}
	}
	
	public void Cancel(List<String> urls){
		for(int i = 0; i < urls.size(); ++i){
			String url = urls.get(i);		
			urlDequeDiskIO.remove(url);
			urlDequeDownload.remove(url);
			callbackManager.remove(url);
		}	
	}
	
	public void Cancel(String url, Object object) {
		if(callbackManager.remove(url, object)){
			urlDequeDiskIO.remove(url);
			urlDequeDownload.remove(url);
		}
	}
	
	public WeakReference<Bitmap> getWithImmediateIO(String url){		
		WeakReference<Bitmap> result = null;		
		if(ImageCacheManager.getInstance().contains(url)){
			result = ImageCacheManager.getInstance().getFromMemoryCache(url); 	
			if(result != null && result.get() != null){
				return result;
			}
		}		
		result = ImageCacheManager.getInstance().safeGetFromFileCacheOrAssets(url);
		return result;		
	}

	protected void putToDownloadDeque(String url) {
		if(!urlDequeDownload.contains(url)){
			urlDequeDownload.add(url);
		}
	}
	
	private void startFetchingTread(String url){
		//put url to load-deque for disk-cache, and start loading-from-disk-cache if necessary
		putUrlToUrlQueue(url);		
		State state = diskIOImgThread.getState();		
		if(state== State.NEW){
			diskIOImgThread.start();
		}
		else if(state == State.TERMINATED){
			diskIOImgThread = new DiskIOImageThread();
			diskIOImgThread.start();
		}
	}
	
	synchronized private void startDownloadingTread(){
		for(int i = 0; i < downloadImgThread.length; ++ i){
			State state = downloadImgThread[i].getState();			
			if(state== State.NEW){
				downloadImgThread[i].start();
			}
			else if(state == State.TERMINATED){
				downloadImgThread[i] = new DownloadImageThread();
				downloadImgThread[i].start();
			}
		}
	}	
	
	private void putUrlToUrlQueue(String url){		
		if(!urlDequeDiskIO.contains(url) && !urlDequeDownload.contains(url)){
			urlDequeDiskIO.add(url);
		}
	}
	
	Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			
			switch(msg.what){	
				case MESSAGE_ID :{					
					Bundle bundle = msg.getData();					
					String url =bundle.getString(EXTRA_IMG_URL);
					Bitmap bitmap = bundle.getParcelable(EXTRA_IMG);			
					callbackManager.callback(url, bitmap);					
					break;
				}
				case MESSAGE_FAIL:{
					Bundle bundle = msg.getData();
					String url =bundle.getString(EXTRA_IMG_URL);					
					callbackManager.fail(url);					
					break;
				}				
			}			
		};
	};	
	
	private void notifyFail(String url){
		callbackManager.fail(url);
	}
	
	private  class DiskIOImageThread extends Thread{		
		private boolean isRun=true;
		private String mCurrentUrl = null;
		
		public void shutDown(){
			isRun =false;
		}
		
		public void run(){
			try{
				while(isRun && urlDequeDiskIO.size() > 0)
				{					
					mCurrentUrl = urlDequeDiskIO.remove(0);
					if(null == mCurrentUrl){
						continue;
					} 
					
					WeakReference<Bitmap> bitmap = ImageCacheManager.getInstance().safeGetFromDiskCache(mCurrentUrl);
					if(bitmap==null || bitmap.get() == null){//if not in disk cache, put the url to download-deque for further downloading
						putToDownloadDeque(mCurrentUrl);
						startDownloadingTread();
					}else{
						Message msg=handler.obtainMessage(MESSAGE_ID);
						Bundle bundle =msg.getData();
						bundle.putSerializable(EXTRA_IMG_URL, mCurrentUrl);
						bundle.putParcelable(EXTRA_IMG, bitmap.get());
						handler.sendMessage(msg);
					}
				}
			}
			catch (Exception e){
				e.printStackTrace();
			}
			finally{
				shutDown();
			}		
		}
	}
	
	private  class DownloadImageThread extends Thread{
		private boolean isRun=true;
		public void shutDown(){
			isRun =false;
		}
		
		public void run(){
			try{
				while(isRun){
					String url = null;
					synchronized(urlDequeDownload){
						if(urlDequeDownload.size() <= 0){
							break;
						}
						url= urlDequeDownload.remove(0);
					}
					
					if(null == url || !url.trim().startsWith("http")){
						continue;
					} 
					
					WeakReference<Bitmap> bitmap = ImageCacheManager.getInstance().safeGetFromNetwork(url);
					
					if(null != bitmap && bitmap.get() != null){
						Message msg=handler.obtainMessage(MESSAGE_ID);
						Bundle bundle =msg.getData();
						bundle.putSerializable(EXTRA_IMG_URL, url);
						bundle.putParcelable(EXTRA_IMG, bitmap.get());
						handler.sendMessage(msg);
					}else{						
						notifyFail(url);
					}
				}
			}
			catch (Exception e){
				e.printStackTrace();
			}
			finally{
				shutDown();
			}			
		}			
	}

	private WeakReference<Bitmap> getBitmapInMemory(String url){
		if(url == null || url.equals("")) return null;
		return ImageCacheManager.getInstance().getFromMemoryCache(url);
	}
	
	public void showImg(final View view,final String url, final String preUrl, Context con, WeakReference<Bitmap> bmp){
		view.setTag(url);
		WeakReference<Bitmap> bitmap = get(url, getCallback(url,preUrl, view, bmp));//defaultResImgId));		
		if(bitmap != null && bitmap.get() != null){			
			(new AsyncTask<Bitmap, Boolean, Bitmap>(){
	
				@Override
				protected Bitmap doInBackground(Bitmap... bitmaps) { 
					return bitmaps[0];
				}
				
				@Override
				protected void onPostExecute(Bitmap bitmap_) {  
					synchronized(this){
						if(((String)view.getTag()).equals(url)){
							if(!bitmap_.isRecycled()){
								if(!url.equals(preUrl)){
									WeakReference<Bitmap> bmp = getBitmapInMemory(preUrl);
									if(bmp != null && bmp.get() != null){
										Drawable curDrawable = 
												view instanceof ImageView ? ((ImageView)view).getDrawable() : view.getBackground();
										if(curDrawable != null && (curDrawable instanceof BitmapDrawable)){
											Bitmap curBmp = ((BitmapDrawable)curDrawable).getBitmap();
											if(curBmp != null && curBmp.hashCode() == bmp.hashCode()){
												int count = decreaseBitmapReferenceCount(bmp.hashCode(), view.hashCode());
												if(0 >= count){
													ImageCacheManager.getInstance().forceRecycle(preUrl, true);
												}
											}
										}
									}
								}							
								if(view instanceof ImageView){
									((ImageView)view).setImageBitmap(bitmap_);
								}else{
									view.setBackgroundDrawable(new BitmapDrawable(bitmap_));
								}
								increaseBitmapReferenceCount(bitmap_.hashCode(), view.hashCode());

							}
						}
					}
				}
			}).execute(bitmap.get());			
		}	
	}
	
	public void showImg(final View view,final String url, String preUrl, Context con){
		showImg(view, url, preUrl, con, null);
	}
	
	HashMap<Integer, ArrayList<Integer>> bmpReferenceMap = new HashMap<Integer, ArrayList<Integer>>();
	
	private int decreaseBitmapReferenceCount(int bmpHashCode, int viewHashCode){
		if(bmpReferenceMap.containsKey(bmpHashCode)){
			ArrayList<Integer> value = bmpReferenceMap.get(bmpHashCode);
			if(value != null){
				for(int i = 0; i < value.size(); ++ i){
					if(value.get(i) == viewHashCode){
						value.remove(i);
						break;
					}
				}
				return value.size();
			}
		}
		return -1;
	}
	
	private int increaseBitmapReferenceCount(int bmpHashCode, int viewHashCode){
		if(bmpReferenceMap.containsKey(bmpHashCode)){
			ArrayList<Integer> value = bmpReferenceMap.get(bmpHashCode);
			if(value != null){
				value.add(viewHashCode);
			}
			return value == null ? -1 : value.size();
		}else{
			ArrayList<Integer> value = new ArrayList<Integer>();
			value.add(viewHashCode);
			bmpReferenceMap.put(bmpHashCode, value);
			return 1;
		}
	}
	
	private ImageLoaderCallback getCallback(final String url,final String preUrl, final View view, final WeakReference<Bitmap> defaultBmp){		
		return new ImageLoaderCallback(){ 
			private boolean inFailStatus = false;
			public void refresh(String url, Bitmap bitmap)
			{
				if(bitmap == null) return;
				synchronized(this){
					if(url.equals(view.getTag().toString()))
					{
						if(!bitmap.isRecycled()){
							if(!url.equals(preUrl)){
								WeakReference<Bitmap> bmp = getBitmapInMemory(preUrl);
								if(bmp != null){
									Drawable curDrawable = 
											view instanceof ImageView ? ((ImageView)view).getDrawable() : view.getBackground();
									if(curDrawable != null && (curDrawable instanceof BitmapDrawable)){
										Bitmap curBmp = ((BitmapDrawable)curDrawable).getBitmap();
										if(curBmp != null && curBmp.hashCode() == bmp.hashCode()){
											int count = decreaseBitmapReferenceCount(bmp.hashCode(), view.hashCode());
											if(0 >= count){
												ImageCacheManager.getInstance().forceRecycle(preUrl, true);
											}												
										}
									}
								}
							}
							if(view instanceof ImageView){
								((ImageView)view).setImageBitmap(bitmap);
							}else{
								view.setBackgroundDrawable(new BitmapDrawable(bitmap));
							}
							increaseBitmapReferenceCount(bitmap.hashCode(), view.hashCode());
						}
						inFailStatus = false;
					}
				}
			}
			
			@Override
			public Object getObject(){
				return view;
			}

			@Override
			public void fail(String url) {
				if(url.equals(view.getTag().toString()) && defaultBmp != null && !inFailStatus)
				{
					//method #fail(String url) maybe not called on main thread(UI thread), we should make sure the UI update on main thread
					view.postDelayed(new Runnable() {
						public void run() {
							if(view instanceof ImageView){
								((ImageView)view).setImageBitmap(defaultBmp.get());//(defaultImgRes);
							}else{
								BitmapDrawable bd = new BitmapDrawable(defaultBmp.get());
								view.setBackgroundDrawable(bd);//setBackgroundResource(defaultImgRes);
							}
						}
					}, 100);
					inFailStatus = true;
				}
			}
		};		
	}	
}
