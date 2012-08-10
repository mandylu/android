/**
 *SimpleImageLoader.java
 *2011-10-15 下午04:50:52
 *Touch Android
 *http://bbs.droidstouch.com
 */
package com.quanleimu.imageCache;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.quanleimu.activity.QuanleimuApplication;


/**
 * 
 *
 */
public class SimpleImageLoader
{
	public static void AdjustPriority(ArrayList<String> urls){
		QuanleimuApplication.lazyImageLoader.AdjustPriority(urls);
	}
	
	public static void Cancel(List<String> urls){
		QuanleimuApplication.lazyImageLoader.Cancel(urls);
	//	QuanleimuApplication.lazyImageLoader.forceRecycle();
	}
	
	public static void Cancel(String url, Object object){
		QuanleimuApplication.lazyImageLoader.Cancel(url, object);
	}
	
	public static String getFileInDiskCache(String url){
		return QuanleimuApplication.lazyImageLoader.getFileInDiskCache(url);
	}

	public static void showImg(final ImageView view,final String url,Context con, final int defaultResImgId)
	{
		view.setTag(url);	
		Bitmap bitmap = QuanleimuApplication.lazyImageLoader.get(url, getCallback(url,view,defaultResImgId));
	
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
					if(((String)view.getTag()).equals(url)){
						view.setImageBitmap(bitmap_);
					}
				}
			}).execute(bitmap);			
		}	
	}
	
	public static void showImg(final ImageView view,final String url,Context con)
	{
		showImg(view, url, con, -1);
	}
	
	private static ImageLoaderCallback getCallback(final String url,final ImageView view, final int defaultImgRes)
	{		
		return new ImageLoaderCallback()
		{ 
			private boolean inFailStatus = false;
			public void refresh(String url, Bitmap bitmap)
			{
					if(url.equals(view.getTag().toString()))
					{
						view.setImageBitmap(bitmap);
						inFailStatus = false;
					}
					else
					{
//						view.setImageResource(R.drawable.moren);
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
							view.setImageResource(defaultImgRes);
						}
					}, 100);
					inFailStatus = true;
				}
			}
		};
		
	}
	
	
	public static void dispalyForDlg(ImageView imageView, String url,ProgressBar pb,Button btnBig)
	{
		
		imageView.setTag(url);
		Bitmap bmp = QuanleimuApplication.lazyImageLoader.get(url,createCallback(url, imageView,pb,btnBig));
		imageView.setImageBitmap(bmp);
	}
	
	
	private static ImageLoaderCallback createCallback(final String url,final ImageView imageView,final ProgressBar pb,final Button btnBig)
	{
		
		return new ImageLoaderCallback()
		{
			
			public void refresh(String url, Bitmap bitmap)
			{
				pb.setVisibility(View.GONE);
				imageView.setVisibility(View.VISIBLE);
				btnBig.setVisibility(View.VISIBLE);
				if (url.equals(imageView.getTag())) 
				{
					imageView.setImageBitmap(bitmap);
				}
				else
				{
//					imageView.setImageResource(R.drawable.moren);
				}
			}
			
			
			@Override
			public Object getObject(){
				return btnBig;
			}


			@Override
			public void fail(String url) {
				// TODO Auto-generated method stub
				
			}
		};
	}
	
}
