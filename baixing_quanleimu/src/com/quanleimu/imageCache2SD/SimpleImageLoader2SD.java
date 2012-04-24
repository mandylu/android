/**
 *SimpleImageLoader.java
 *2011-10-15 下午04:50:52
 *Touch Android
 *http://bbs.droidstouch.com
 */
package com.quanleimu.imageCache2SD;

import com.quanleimu.activity.MyApplication;
import com.quanleimu.activity.R;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;


/**
 * 
 *
 */
public class SimpleImageLoader2SD
{


	
	public static void showImg(ImageView view,String url)
	{
		
		view.setTag(url);	
		Bitmap bitmap = MyApplication.lazyImageLoader2SD.get(url, getCallback(url,view));
		if(bitmap==null){
			view.setImageResource(R.drawable.moren);
//			view.setVisibility(View.GONE);
			
		}else{
		view.setImageBitmap(bitmap);
		}
	
	}
	
	
	private static ImageLoaderCallback2SD getCallback(final String url,final ImageView view)
	{
		
		
		return new ImageLoaderCallback2SD()
		{ 
			
			public void refresh(String url, Bitmap bitmap)
			{
				
					if(url.equals(view.getTag().toString()))
					{
						view.setImageBitmap(bitmap);
					}
					else
					{
						view.setImageResource(R.drawable.moren);
					}
				
			}
		};
		
	}
	
	
	public static void dispalyForDlg(ImageView imageView, String url,ProgressBar pb,Button btnBig)
	{
		
		imageView.setTag(url);
		Bitmap bmp = MyApplication.lazyImageLoader2SD.get(url,createCallback(url, imageView,pb,btnBig));
		imageView.setImageBitmap(bmp);
	}
	
	
	private static ImageLoaderCallback2SD createCallback(final String url,final ImageView imageView,final ProgressBar pb,final Button btnBig)
	{
		
		return new ImageLoaderCallback2SD()
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
					imageView.setImageResource(R.drawable.moren);
				}
			}
		};
	}
	
	
	
	
}
