package com.quanleimu.imageCache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.util.LruCache;
import android.util.Log;

import com.quanleimu.activity.R;
import com.quanleimu.util.BitmapUtils;
import com.quanleimu.util.DiskLruCache;
import com.quanleimu.util.NetworkProtocols;
import com.quanleimu.util.Util;



public class ImageManager
{

	//private Map<String, SoftReference<Bitmap>> imgCache ;
	
	private List<Bitmap> trashList = new ArrayList<Bitmap>();
	private LruCache<String, Bitmap> imageLruCache;
	private DiskLruCache imageDiskLruCache = null;

	
	private Context context;	
	
	public static Bitmap userDefualtHead;
	
	public ImageManager(Context context)
	{
		this.context = context;
		//imgCache = new HashMap<String, SoftReference<Bitmap>>();
		userDefualtHead =drawabltToBitmap(context.getResources().getDrawable(R.drawable.moren));
		
	    // Get memory class of this device, exceeding this amount will throw an
	    // OutOfMemory exception.
	    final int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
	    
	    File fileCacheDir = DiskLruCache.getDiskCacheDir(context, "");
	    long capacity_20M = 20*1024*1024;
	    long capacity_halfFreeSpace = BitmapUtils.getUsableSpace(fileCacheDir) / 2;
	    if(capacity_halfFreeSpace < 0){
	    	Log.d("ImageManager", "FATAL error: disk cache dir is not valid!");
	    }
	    final long diskCacheSize =  capacity_20M < capacity_halfFreeSpace ? capacity_20M : capacity_halfFreeSpace;
	    
	    imageDiskLruCache = DiskLruCache.openCache(context, fileCacheDir, diskCacheSize);
	    if(null == imageDiskLruCache){
	    	Log.d("ImageManager", "FATAL error: disk cache is not correctly installed!");
	    }

	    // Use 1/8th of the available memory for this memory cache.
	    final int cacheSize = 1024 * 1024 * memClass / 8;    
	    
	    imageLruCache = new LruCache<String, Bitmap>(cacheSize){
	        @Override
	        protected int sizeOf(String key, Bitmap bitmap) {
	            // The cache size will be measured in bytes rather than number of items.
	            int bytes = bitmap.getHeight()*bitmap.getRowBytes();
	            return bytes;
	        }
	        
	        @Override
	        protected void entryRemoved(boolean evicted, String key, Bitmap oldValue, Bitmap newValue){
//	        	if(!evicted)
//	        		oldValue.recycle();
	        	
	        	super.entryRemoved(evicted, key, oldValue, newValue);
	        }
	    };    
	}
	
	public void enableSampleSize(boolean b){
		BitmapUtils.enableSampleSize(b);
	}
	
	public boolean contains(String url)
	{
		
		//return imgCache.containsKey(url);
		synchronized(this){
			return (null != imageLruCache.get(url));
		}
		
	}
	
	public Bitmap getFromMemoryCache(String url)
	{
		Bitmap bitmap = null;
		
		bitmap = this.getFromMapCache(url);
		
		if(null == bitmap)
		{
			
			bitmap =getFromFileCache(url);
		}
		
		return bitmap;		
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
	
	public Bitmap getFromFileCache(String url)
	{
		//Log.d("LruDiskCache", "get from filecache: "+url+";");
		if(null != imageDiskLruCache){
			return imageDiskLruCache.get(getMd5(url));
		}
		
		return null;
	}
	
	public Bitmap getFromMapCache(String url)
	{
		Bitmap bitmap = null;
		
//		SoftReference<Bitmap> ref = null;
//		
//		synchronized (this)
//		{
//			ref = imgCache.get(url);
//		}
//		if(null != ref)
//		{
//			bitmap = ref.get();
//			
//		}
		
		synchronized (this){
			bitmap = imageLruCache.get(url);			
		}
		return bitmap;
	}
	
	public void forceRecycle(String url, boolean rightNow){
//		Bitmap bitmap = imageLruCache.get(url);
//		if(bitmap!=null&& !bitmap.isRecycled()){
//			bitmap.recycle();
//			bitmap = null;
//		}else{
//		}
//		
		if(url == null || url.equals(""))return;
		Bitmap bitmap = null;
		synchronized(this){
			bitmap = imageLruCache.remove(url);//anyway ,remove it from cache//imageLruCache.get(url);
		}
		if(bitmap != null){
			Log.d("recycle", "hahaha remove unuesd bitmap~~~~~~~~~~~~~~~    " + url + ", recycle right now ? " + rightNow);
			if (rightNow)
			{
				bitmap.recycle();
			}
			else
			{
				synchronized (trashList) {
					trashList.add(bitmap);
				}
			}
						
//			bitmap.recycle();
			bitmap = null;
//			System.gc();			
		}
				
	}
	
	/**
	 * FIXME: hard code for android fragment issue. fixme if you have any good idea.
	 * When work with android support V4 fragment. system may force UI update even when fragment is destoried. So we must delay force recycle the bitmap.
	 */
	public void postRecycle()
	{
		Thread t = new Thread(
				new Runnable() {
					
					@Override
					public void run() {
						
						List<Bitmap> tmpList = null;
						synchronized (trashList) {
							tmpList = new ArrayList<Bitmap>();
							tmpList.addAll(trashList);
							trashList.clear();
						}
						
						try {
							if (tmpList.size() > 0) //Sleep only if we have something to recycle.
							{
								Thread.sleep(2000); 
							}
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						
						for (Bitmap bp : tmpList)
						{
							Log.d("recycle", "exe delay recycle bitmap " + bp);
							bp.recycle();
						}
						
						System.gc(); //Force gc after bitmap recycle.
					}
				});
		
		t.start();
	}
	
	public void forceRecycle(){//release all bitmap
		
//		for(bitmap r : imageLruCache.){
//            if(r != null){
//                Bitmap b = r.get();
//                
//                if(b != null && !b.isRecycled()){
//	                b.recycle();
//	                b = null;
//            		}
//                
//            }
//        }
//	  imgCache.clear();
		
		imageLruCache.evictAll();
	}
	
	public Bitmap safeGetFromFileCacheOrAssets(String url)
	{
		String fileName = getMd5(url);
		Bitmap bitmap = this.getFromFileCache(url);
		
		if(null == bitmap){
			try{
				FileInputStream is = context.getAssets().openFd(fileName).createInputStream();
				BitmapFactory.Options o =  new BitmapFactory.Options();
	            o.inPurgeable = true;
	            bitmap = BitmapFactory.decodeStream(is, null, o);
	            
	            if(null != imageDiskLruCache){
	            	imageDiskLruCache.put(fileName, bitmap);
	            }
	            
			}catch(FileNotFoundException ee){
				
			}catch(IOException eee){
				
			}
		}
		
		if(null != bitmap)
		{
			synchronized (this)
			{
				//imgCache.put(url, new SoftReference<Bitmap>(bitmap));
				
				imageLruCache.put(url, bitmap);
			}			
		}
		
		return bitmap;
	}
	
	
	public Bitmap safeGetFromDiskCache(String url)
	{
		Bitmap bitmap = this.getFromFileCache(url);

		if(null != bitmap)
		{
			synchronized (this)
			{
				imageLruCache.put(url, bitmap);
			}
		}
		return bitmap;
	}
	
	public Bitmap safeGetFromNetwork(String url) throws HttpException
	{
		Bitmap bitmap = downloadImg(url);
		
		if(null != bitmap)
		{
			synchronized (this)
			{
				imageLruCache.put(url, bitmap);
			}
		}
		return bitmap;
	}
	
	public Bitmap downloadImg(String urlStr) throws HttpException
	{
		//Log.d("LruDiskCache", "download image: "+urlStr+";");
		
		HttpClient httpClient = null;
        InputStream bis = null;
        Bitmap bitmapRet = null;
        
		try
		{
			httpClient = NetworkProtocols.getInstance().getHttpClient();
	        
	        HttpGet httpGet = new HttpGet(urlStr); 
	        HttpResponse response = httpClient.execute(httpGet);	
	        
	        String key = getMd5(urlStr);
	        if(null != imageDiskLruCache){
	           	imageDiskLruCache.put(key, response.getEntity().getContent());
	           	
	           	httpClient.getConnectionManager().shutdown();
	           	httpClient = null;
	           	
	           	bitmapRet = imageDiskLruCache.get(key);
	        }else{
	    		
	    		InputStream inputStream = response.getEntity().getContent();
	    		bitmapRet = BitmapUtils.decodeSampledBitmapFromFile(inputStream);	    		
	        }
            
            return bitmapRet;
		}
		catch (Exception e){			
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
	
//	public static List<String> listNames= new ArrayList<String>();;
//	public String  writeToFile(String fileName, InputStream is)
//	{
//		
//		BufferedInputStream bis = null;
//		
//		BufferedOutputStream bos = null;
//		
//		try
//		{
//			bis = new BufferedInputStream(is);
//			bos = new BufferedOutputStream(context.openFileOutput(fileName, Context.MODE_PRIVATE));
//			
//			byte[] buffer = new byte[1024];
//			int length;
//			while((length = bis.read(buffer)) != -1)
//			{
//				bos.write(buffer, 0, length);
//			}
////			listNames = MyApplication.list;
////			if(listNames == null || listNames.size() == 0)
////			{
////				listNames = new ArrayList<String>();
////				listNames.add(fileName);
////			}
////			else if(!listNames.contains(fileName))
////			{
////				listNames.add(fileName);
////			}
////			MyApplication.list = listNames;
//			
//			
//		} catch (Exception e)
//		{
//			
//		}
//		finally
//		{
//			
//			try
//			{
//				if(null != bis)
//				{
//					bis.close();
//				}
//				
//				if(null != bos)
//					{
//						bos.flush();
//						bos.close();
//					
//					}
//				
//			}
//			catch (IOException e)
//			{
//				e.printStackTrace();
//			}
//		}
//		
//		
//		return context.getFilesDir() + "/" + fileName;
//		
//	}
	
	
	
	
	
	
	private String getMd5(String src)
	{
		return Util.MD5(src);
	}
	
	
	
	
	private Bitmap drawabltToBitmap(Drawable drawable)
	{
		
		BitmapDrawable tempDrawable = (BitmapDrawable)drawable;
		return tempDrawable.getBitmap();
		
	}

	public String getFileInDiskCache(String url) {
		if(null != imageDiskLruCache){
			return imageDiskLruCache.getFilePath(this.getMd5(url));
		}else{
			return "";
		}
	}
}

