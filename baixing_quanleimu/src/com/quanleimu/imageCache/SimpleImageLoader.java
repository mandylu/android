/**
 *SimpleImageLoader.java
 *2011-10-15 下午04:50:52
 *Touch Android
 *http://bbs.droidstouch.com
 */
package com.quanleimu.imageCache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.quanleimu.activity.MyApplication;
import com.quanleimu.activity.R;
import com.quanleimu.util.Helper;


/**
 * 
 *
 */
public class SimpleImageLoader
{


	
	public static void showImg(ImageView view,String url,Context con)
	{
		view.setTag(url);	
		Bitmap bitmap = MyApplication.lazyImageLoader.get(url, getCallback(url,view));
		bitmap = Helper.toRoundCorner(bitmap, 10);
	
		if(bitmap==null){
		    
//		    BitmapFactory.Options o =  new BitmapFactory.Options();
//            o.inPurgeable = true;
//            
//			Bitmap tmb = BitmapFactory.decodeResource(con.getResources(), R.drawable.moren, o);
//			Bitmap mb= Helper.toRoundCorner(tmb, 20);
//			tmb.recycle();
//			view.setImageBitmap(mb);			
		}else{
		view.setImageBitmap(bitmap);
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
					}
					else
					{
//						view.setImageResource(R.drawable.moren);
					}
				
			}
		};
		
	}
	
	
	public static void dispalyForDlg(ImageView imageView, String url,ProgressBar pb,Button btnBig)
	{
		
		imageView.setTag(url);
		Bitmap bmp = MyApplication.lazyImageLoader.get(url,createCallback(url, imageView,pb,btnBig));
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
