//xumengyi@baixing.com
package com.baixing.imageCache;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import com.baixing.data.GlobalDataManager;
public class SimpleImageLoader{
	public static void AdjustPriority(ArrayList<String> urls){
		GlobalDataManager.getImageLoader().AdjustPriority(urls);
	}
	
	public static void Cancel(List<String> urls){
		GlobalDataManager.getImageLoader().Cancel(urls);
	//	QuanleimuApplication.lazyImageLoader.forceRecycle();
	}
	
	public static void Cancel(String url, Object object){
		GlobalDataManager.getImageLoader().Cancel(url, object);
	}
	
	public static String getFileInDiskCache(String url){
		return GlobalDataManager.getImageLoader().getFileInDiskCache(url);
	}

	public static void showImg(final View view,final String url, final String preUrl, Context con, WeakReference<Bitmap> bmp)//final int defaultResImgId)
	{
		view.setTag(url);	
		WeakReference<Bitmap> bitmap = GlobalDataManager.getImageLoader().get(url, getCallback(url,preUrl, view, bmp));//defaultResImgId));		
		if(bitmap==null || bitmap.get() == null){
		}else{			
			(new AsyncTask<Bitmap, Boolean, Bitmap>(){
	
				@Override
				protected Bitmap doInBackground(Bitmap... bitmaps) { 
					return bitmaps[0];
				}
				
				@Override
				protected void onPostExecute(Bitmap bitmap_) {  
					synchronized(GlobalDataManager.getImageLoader()){
						if(((String)view.getTag()).equals(url)){
							if(!bitmap_.isRecycled()){
								if(!url.equals(preUrl)){
									WeakReference<Bitmap> bmp = GlobalDataManager.getImageLoader().getBitmapInMemory(preUrl);
									if(bmp != null && bmp.get() != null){
										Drawable curDrawable = 
												view instanceof ImageView ? ((ImageView)view).getDrawable() : view.getBackground();
										if(curDrawable != null && (curDrawable instanceof BitmapDrawable)){
											Bitmap curBmp = ((BitmapDrawable)curDrawable).getBitmap();
											if(curBmp != null && curBmp.hashCode() == bmp.hashCode()){
												int count = decreaseBitmapReferenceCount(bmp.hashCode(), view.hashCode());
												if(0 >= count){
													GlobalDataManager.getImageLoader().forceRecycle(preUrl);
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
	
	public static void showImg(final View view,final String url, String preUrl, Context con){
		showImg(view, url, preUrl, con, null);
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
	
	private static ImageLoaderCallback getCallback(final String url,final String preUrl, final View view, final WeakReference<Bitmap> defaultBmp)//final int defaultImgRes)
	{		
		return new ImageLoaderCallback()
		{ 
			private boolean inFailStatus = false;
			public void refresh(String url, Bitmap bitmap)
			{
				if(bitmap == null) return;
				synchronized(GlobalDataManager.getImageLoader()){
					if(url.equals(view.getTag().toString()))
					{
						if(!bitmap.isRecycled()){
							if(!url.equals(preUrl)){
								WeakReference<Bitmap> bmp = GlobalDataManager.getImageLoader().getBitmapInMemory(preUrl);
								if(bmp != null){
									Drawable curDrawable = 
											view instanceof ImageView ? ((ImageView)view).getDrawable() : view.getBackground();
									if(curDrawable != null && (curDrawable instanceof BitmapDrawable)){
										Bitmap curBmp = ((BitmapDrawable)curDrawable).getBitmap();
										if(curBmp != null && curBmp.hashCode() == bmp.hashCode()){
											int count = decreaseBitmapReferenceCount(bmp.hashCode(), view.hashCode());
											if(0 >= count){
												GlobalDataManager.getImageLoader().forceRecycle(preUrl);
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
