/**
 *SimpleImageLoader.java
 *2011-10-15 下午04:50:52
 *Touch Android
 *http://bbs.droidstouch.com
 */
package com.quanleimu.imageCache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.quanleimu.activity.QuanleimuApplication;
import android.util.Log;


/**
 * 
 *
 */
public class SimpleImageLoader
{
	public static void AdjustPriority(ArrayList<String> urls){
		QuanleimuApplication.getImageLoader().AdjustPriority(urls);
	}
	
	public static void Cancel(List<String> urls){
		QuanleimuApplication.getImageLoader().Cancel(urls);
	//	QuanleimuApplication.lazyImageLoader.forceRecycle();
	}
	
	public static void Cancel(String url, Object object){
		QuanleimuApplication.getImageLoader().Cancel(url, object);
	}
	
	public static String getFileInDiskCache(String url){
		return QuanleimuApplication.getImageLoader().getFileInDiskCache(url);
	}

	public static void showImg(final View view,final String url, final String preUrl, Context con, final int defaultResImgId)
	{
		view.setTag(url);	
		Bitmap bitmap = QuanleimuApplication.getImageLoader().get(url, getCallback(url,preUrl, view,defaultResImgId));
	
//		Log.d("simple image loader: ", "url: "+url+"   => view: "+ view.toString() + "with tag " + view.getTag());
		
		if(bitmap==null){
		    
//		    BitmapFactory.Options o =  new BitmapFactory.Options();
//            o.inPurgeable = true;
//            
//			Bitmap tmb = BitmapFactory.decodeResource(con.getResources(), R.drawable.moren, o);
//			Bitmap mb= Helper.toRoundCorner(tmb, 20);
//			tmb.recycle();
//			view.setImageBitmap(mb);			
		}else{			
			(new AsyncTask<Bitmap, Boolean, Bitmap>(){
	
				@Override
				protected Bitmap doInBackground(Bitmap... bitmaps) { 
					return bitmaps[0];
				}
				
				@Override
				protected void onPostExecute(Bitmap bitmap_) {  
					synchronized(QuanleimuApplication.getImageLoader()){
						if(((String)view.getTag()).equals(url)){
//							Log.d("load image: ", "hahaha ln79  load url is: " + url + " and view:    " + view.hashCode() + "   "+ System.currentTimeMillis());
							if(!bitmap_.isRecycled()){
								if(!url.equals(preUrl)){
									Bitmap bmp = QuanleimuApplication.getImageLoader().getBitmapInMemory(preUrl);
									if(bmp != null){
										Drawable curDrawable = 
												view instanceof ImageView ? ((ImageView)view).getDrawable() : view.getBackground();
										if(curDrawable != null && (curDrawable instanceof BitmapDrawable)){
											Bitmap curBmp = ((BitmapDrawable)curDrawable).getBitmap();
											if(curBmp != null && curBmp.hashCode() == bmp.hashCode()){
//												Log.d("remove", "hahaha, before recycle, line: 77    " + System.currentTimeMillis());
												int count = decreaseBitmapReferenceCount(bmp.hashCode(), view.hashCode());
												if(0 >= count){
													QuanleimuApplication.getImageLoader().forceRecycle(preUrl);
												}else{
//													Log.d("not 0", "hahaha can't recycle ooooooooooooooooooo, ln 91");
												}
//												QuanleimuApplication.lazyImageLoader.forceRecycle(preUrl);
//												Log.d("remove", "hahaha, recycle, line: 77    " + System.currentTimeMillis());												
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

							}else{
								Log.d("load image, but recycled", "hahaha, already recycled~~~~~~~~~~ln 80");
							}
						}
					}
				}
			}).execute(bitmap);			
		}	
	}
	
	public static void showImg(final View view,final String url, String preUrl, Context con)
	{
		showImg(view, url, preUrl, con, -1);
	}
	
	static HashMap<Integer, ArrayList<Integer>> bmpReferenceMap = new HashMap<Integer, ArrayList<Integer>>();
	
	private static int decreaseBitmapReferenceCount(int bmpHashCode, int viewHashCode){
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
	
	private static int increaseBitmapReferenceCount(int bmpHashCode, int viewHashCode){
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
	
	private static ImageLoaderCallback getCallback(final String url,final String preUrl, final View view, final int defaultImgRes)
	{		
		return new ImageLoaderCallback()
		{ 
			private boolean inFailStatus = false;
			public void refresh(String url, Bitmap bitmap)
			{
				synchronized(QuanleimuApplication.getImageLoader()){
					if(url.equals(view.getTag().toString()))
					{
//						Log.d("load image: ", "hahaha ln107  load url is: " + url + "  and view:  " + view.hashCode() + "   "+ System.currentTimeMillis());
						if(!bitmap.isRecycled()){
							if(!url.equals(preUrl)){
								Bitmap bmp = QuanleimuApplication.getImageLoader().getBitmapInMemory(preUrl);
								if(bmp != null){
									Drawable curDrawable = 
											view instanceof ImageView ? ((ImageView)view).getDrawable() : view.getBackground();
									if(curDrawable != null && (curDrawable instanceof BitmapDrawable)){
										Bitmap curBmp = ((BitmapDrawable)curDrawable).getBitmap();
										if(curBmp != null && curBmp.hashCode() == bmp.hashCode()){
//											Log.d("remove", "hahaha, before recycle, line: 129    " + System.currentTimeMillis());
											int count = decreaseBitmapReferenceCount(bmp.hashCode(), view.hashCode());
											if(0 >= count){
												QuanleimuApplication.getImageLoader().forceRecycle(preUrl);
											}else{
//												Log.d("not 0", "hahaha can't recycle ooooooooooooooooooo, ln 175");
											}
//											Log.d("remove", "hahaha, recycle, line: 131   " + System.currentTimeMillis());												
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

							
						}else{
//							Log.d("load image, but recycled", "hahaha, already recycled~~~~~~~~~~ln 111");
						}
						inFailStatus = false;
						
						
					}
					else
					{
//						view.setImageResource(R.drawable.moren);
					}
				}
			}
			
			@Override
			public Object getObject(){
				return view;
			}

			@Override
			public void fail(String url) {
				if(url.equals(view.getTag().toString()) && defaultImgRes != -1 && !inFailStatus)
				{
					//method #fail(String url) maybe not called on main thread(UI thread), we should make sure the UI update on main thread
					view.postDelayed(new Runnable() {
						public void run() {
							if(view instanceof ImageView){
								((ImageView)view).setImageResource(defaultImgRes);
							}else{
								view.setBackgroundResource(defaultImgRes);
							}
						}
					}, 100);
					inFailStatus = true;
				}
			}
		};
		
	}	
	
}
