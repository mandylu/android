package com.quanleimu.imageCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.WindowManager;

import com.quanleimu.activity.QuanleimuApplication;
import com.quanleimu.activity.R;
import com.quanleimu.util.NetworkProtocols;
import com.quanleimu.util.Util;

public class ImageManager
{

Map<String, SoftReference<Bitmap>> imgCache ;
	
	private Context context;
	
	private boolean useSampleSize = false;
	
	public static Bitmap userDefualtHead;
	
	public ImageManager(Context context)
	{
		this.context = context;
		imgCache = new HashMap<String, SoftReference<Bitmap>>();
		userDefualtHead =drawabltToBitmap(context.getResources().getDrawable(R.drawable.moren));
		
	}
	
	public void enableSampleSize(boolean b){
		this.useSampleSize = b;
	}
	
	public boolean contains(String url)
	{
		
		return imgCache.containsKey(url);
		
	}
	
	public Bitmap getFromCache(String url)
	{
		Bitmap bitmap = null;
		
		bitmap = this.getFromMapCache(url);
		
		if(null == bitmap)
		{
			
			bitmap =getFromFile(url);
		}
		
		return bitmap;
		
		
	}
	
	
	private static void screenDimension(_Rect rc){
		WindowManager wm = 
				(WindowManager)QuanleimuApplication.getApplication().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		rc.width = wm.getDefaultDisplay().getWidth()/2;//shrink display to save memory
		rc.height = wm.getDefaultDisplay().getHeight()/2;//shrink display area to save memory
				
	}
	
	public int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
		if(!	useSampleSize) return 1;
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	        inSampleSize = Math.round((float)height / (float)reqHeight);
	        int t = Math.round((float)width / (float)reqWidth);
	        if(t > inSampleSize) inSampleSize = t;
	        
	    }
	    
	    System.out.println("[decodeSampledBitmapFromFile] SampleSize = " + inSampleSize
	    		+ " reqWidth/width =" + reqWidth + "/" + width 
	    		+ " reqHeight/height = " + reqHeight + "/" + height);
	    return inSampleSize;
	}
	
	
/*
	public static Bitmap decodeSampledBitmapFromStream(InputStream is,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
		Bitmap result = null;
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    
	    try{
	    		BitmapFactory.decodeStream(is, null, options);

	    		// Calculate inSampleSize
	    		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

	    		// Decode bitmap with inSampleSize set
	    		//options.inSampleSize = 1;
		    options.inJustDecodeBounds = false;
		    options.inPurgeable = true;
		    result = BitmapFactory.decodeStream(is, null,options);
	    }catch(Exception e){
	    		e.printStackTrace();
	    }
	    
	    return result;
	}
*/	

	public Bitmap decodeSampledBitmapFromFile(String fileName,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(fileName,options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
	    
	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    options.inPurgeable = true;
	    return BitmapFactory.decodeFile(fileName, options);
	}

	
	public Bitmap decodeBitmapFromFile(String fileName){
		int reqWidth = 200;
		int reqHeight = 200;
		_Rect rc = new _Rect();
		rc.width = reqWidth;
		rc.height = reqHeight;
		screenDimension(rc);
		return decodeSampledBitmapFromFile(fileName, rc.width, rc.height);
	}
/*	
	public static Bitmap decodeBitmapFromStream(InputStream is){
		int reqWidth = 200;
		int reqHeight = 200;
		_Rect rc = new _Rect();
		rc.width = reqWidth;
		rc.height = reqHeight;
		screenDimension(rc);
		return ImageManager.decodeSampledBitmapFromStream(is, rc.width, rc.height);
	}
*/	
	
	public Bitmap getFromFile(String url)
	{
		Bitmap result = null;
		String fileName = context.getFilesDir() + "/" + this.getMd5(url);
		
		result = decodeBitmapFromFile(fileName);
		if(result != null){
			System.out.println(fileName + "===================== fetched bitmap from file cache =================== " + url);
		}else{
			System.out.println(fileName + "===================== Failed to fetch bitmap from file cache =================== " + url);
		}
		return result;
		/*
		FileInputStream is=null;
		
		try
		{
			is=context.openFileInput(fileName);
			
			//BitmapFactory.Options o =  new BitmapFactory.Options();
            //o.inPurgeable = true;
            //result = BitmapFactory.decodeStream(is, null, o);
			
			result = ImageManager.decodeBitmapFromStream(is);
			if(result != null){
				System.out.println("===================== fetched bitmap from file cache =================== " + url);
			}else{
				System.out.println("===================== Failed to fetch bitmap from file cache =================== " + url);
			}
			return result;
		} 
		catch (FileNotFoundException e)
		{
			try{
				is = context.getAssets().openFd(fileName).createInputStream();
				//BitmapFactory.Options o =  new BitmapFactory.Options();
	            //o.inPurgeable = true;
	            //return BitmapFactory.decodeStream(is, null, o);
				result = ImageManager.decodeBitmapFromStream(is);
				if(result != null){
					System.out.println("===================== fetched bitmap from file cache (assets) =================== " + url);
				}
				return result;
				
			}catch(FileNotFoundException ee){
				//ee.printStackTrace();
				return result;
			}catch(IOException eee){
				//eee.printStackTrace();
				return result;
			}
		}
		finally
		{
			if(null != is)
			{
				try{is.close();}catch(Exception ex){
					//ex.printStackTrace();
				};
			}
		}
		*/
		
	}
	
	public Bitmap getFromMapCache(String url)
	{
		Bitmap bitmap = null;
		
		SoftReference<Bitmap> ref = null;
		
		synchronized (this)
		{
			ref = imgCache.get(url);
		}
		if(null != ref)
		{
			bitmap = ref.get();
			System.out.println("===================== fetched bitmap from mem cache =================== " + url);
		}
		return bitmap;
	}
	public void forceRecycle(String url){
		SoftReference<Bitmap> r = imgCache.get(url);
		if(r!=null){
			Bitmap b = r.get();
			if(b != null && !b.isRecycled()){
				System.out.println("=============recycle bitmap======= " + b.toString() + " by " + url);
				b.recycle();
				b = null;
    			}
		}else{
			System.out.println("============= bitmap missed in cache ======= by " + url);
		}
		
		imgCache.remove(url);//anyway ,remove it from cache
		
	}
	public void forceRecycle(){//release all bitmap
		//imgCache.keySet()
		for(SoftReference<Bitmap> r:imgCache.values()){
            if(r != null){
                Bitmap b = r.get();
                
                if(b != null && !b.isRecycled()){
                	    System.out.println("=============recycle bitmap======= " + b.toString());
	                b.recycle();
	                b = null;
            		}
                
            }
        }
	  imgCache.clear();
	}
	
	public Bitmap safeGetFromFile(String url)
	{
		Bitmap bitmap = this.getFromFile(url);
		if(null != bitmap)
		{
			synchronized (this)
			{
				System.out.println("=============cache bitmap======= " + bitmap.toString() + " by " + url);
				imgCache.put(url, new SoftReference<Bitmap>(bitmap));
			}			
		}
		
		return bitmap;
	}
	
	
	public Bitmap safeGet(String url) throws HttpException
	{
		Bitmap bitmap = this.getFromFile(url);
		if(null == bitmap){
			bitmap = downloadImg(url);
		}
		if(null != bitmap)
		{
			synchronized (this)
			{
				System.out.println("=============cache bitmap======= " + bitmap.toString() + " by " + url);
				imgCache.put(url, new SoftReference<Bitmap>(bitmap));
			}
			
		}
		return bitmap;
		
	}
	
	public Bitmap downloadImg(String urlStr) throws HttpException
	{
		HttpClient httpClient = null;
        InputStream bis = null;
        Bitmap bitmapRet = null;
        
		try
		{
//			URL url = new URL(urlStr);
//			HttpURLConnection connection =(HttpURLConnection) url.openConnection();
			
			httpClient = NetworkProtocols.getInstance().getHttpClient();
	        
	        HttpPost httpPost = new HttpPost(urlStr); 
	        HttpResponse response = httpClient.execute(httpPost);			
            //BitmapFactory.Options o =  new BitmapFactory.Options();
            //o.inPurgeable = true;
            //bitmapRet = BitmapFactory.decodeFile(writeToFile(getMd5(urlStr),  response.getEntity().getContent()));       
            String newFile = writeToFile(getMd5(urlStr),  response.getEntity().getContent());
            System.out.println(newFile + "===================== downloaded bitmap =================== " + urlStr);
            bitmapRet = decodeBitmapFromFile(newFile);
            
            return bitmapRet;
		}catch (Exception e){
			
		}
		finally
		{			
			try
			{
				if(null != httpClient){
					httpClient.getConnectionManager().shutdown();
				}
				
				if(null != bis)
				{
					bis.close();
				}				    				
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}catch(Exception e){
				
			}
		} 
		
		return null;
	}
	
	
	
	public void clearall(){
		String[] str = context.fileList();
		for (int i = 0; i < str.length; i++) {
			context.deleteFile(str[i]);
		}
	}
	
	public static List<String> listNames= new ArrayList<String>();;
	public String  writeToFile(String fileName, InputStream is)
	{
		
		BufferedInputStream bis = null;
		
		BufferedOutputStream bos = null;
		
		try
		{
			bis = new BufferedInputStream(is);
			bos = new BufferedOutputStream(context.openFileOutput(fileName, Context.MODE_PRIVATE));
			
			byte[] buffer = new byte[1024];
			int length;
			while((length = bis.read(buffer)) != -1)
			{
				bos.write(buffer, 0, length);
			}
//			listNames = MyApplication.list;
//			if(listNames == null || listNames.size() == 0)
//			{
//				listNames = new ArrayList<String>();
//				listNames.add(fileName);
//			}
//			else if(!listNames.contains(fileName))
//			{
//				listNames.add(fileName);
//			}
//			MyApplication.list = listNames;
			
			
		} catch (Exception e)
		{
			
		}
		finally
		{
			
			try
			{
				if(null != bis)
				{
					bis.close();
				}
				
				if(null != bos)
					{
						bos.flush();
						bos.close();
					
					}
				
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		
		
		return context.getFilesDir() + "/" + fileName;
		
	}
	
	
	
	
	
	
	private String getMd5(String src)
	{
		return Util.MD5(src);
	}
	
	
	
	
	private Bitmap drawabltToBitmap(Drawable drawable)
	{
		
		BitmapDrawable tempDrawable = (BitmapDrawable)drawable;
		return tempDrawable.getBitmap();
		
	}
}


class _Rect{
	public int width = 0;
	public int height =0;
	public _Rect(){
		this.width = 0;
		this.height = 0;
	}
}

