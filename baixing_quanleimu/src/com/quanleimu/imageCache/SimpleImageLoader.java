/**
 *SimpleImageLoader.java
 *2011-10-15 下午04:50:52
 *Touch Android
 *http://bbs.droidstouch.com
 */
package com.quanleimu.imageCache;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Bitmap.Config;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.util.Helper;


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
	}

	
	public static void showImg(final ImageView view,String url,Context con)
	{
		view.setTag(url);	
		Bitmap bitmap = QuanleimuApplication.lazyImageLoader.get(url, getCallback(url,view));
	
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
					try {
						bitmaps[0] = Helper.toRoundCorner(bitmaps[0], 10);									
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return bitmaps[0];
				}
				
				@Override
				protected void onPostExecute(Bitmap bitmap_) {  
					view.setImageBitmap(bitmap_);
				}
			}).execute(bitmap);			
		}	
	}
	
	 
	private static ImageLoaderCallback getCallback(final String url,final ImageView view)
	{		
		return new ImageLoaderCallback()
		{ 
			
			public void refresh(String url, Bitmap bitmap)
			{
				
					if(url.equals(view.getTag().toString()))
					{
						bitmap = Helper.toRoundCorner(bitmap, 10);
						view.setImageBitmap(bitmap);
//						
//						Log.d("simple image loader: ", "url: "+url+"   => callback called with view: "+ view.toString());
						
//						view.post(new Runnable(){
//							@Override
//							public void run(){
//								view.setImageBitmap(bitmap_rd);
//								view.invalidate();
//							}
//						});
//						
//						bitmap = null;
					}
					else
					{
//						view.setImageResource(R.drawable.moren);
//						System.out.print("fjljsafljaljalfjl");
					}
				
			}
		};
		
	}
	
	
	public static void dispalyForDlg(ImageView imageView, String url,ProgressBar pb,Button btnBig)
	{
		
		imageView.setTag(url);
		Bitmap bmp = QuanleimuApplication.lazyImageLoader.get(url,createCallback(url, imageView,pb,btnBig));
		bmp = Helper.toRoundCorner(bmp, 10);
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
					bitmap = Helper.toRoundCorner(bitmap, 10);
					imageView.setImageBitmap(bitmap);
				}
				else
				{
//					imageView.setImageResource(R.drawable.moren);
				}
			}
		};
	}
	
	
	
	
}
