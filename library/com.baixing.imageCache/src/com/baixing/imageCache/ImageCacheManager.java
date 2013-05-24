//xumengyi@baixing.com
package com.baixing.imageCache;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpException;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.util.Pair;
import android.view.WindowManager;

import com.baixing.network.api.FileDownloadCommand;
import com.baixing.imageCache.Utils._Rect;
public class ImageCacheManager{
	private List<WeakReference<Bitmap>> trashList = new ArrayList<WeakReference<Bitmap>>();
	private LruCache<String, Pair<Integer, WeakReference<Bitmap>>> imageLruCache;
	private DiskLruCache imageDiskLruCache = null;	
	private Context context;	
	
	public ImageCacheManager(Context ctx){
		this.context = ctx;
	    // Get memory class of this device, exceeding this amount will throw an
	    // OutOfMemory exception.
	    final int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
	    
	    File fileCacheDir = DiskLruCache.getDiskCacheDir(context, "");
	    long capacity_20M = 20*1024*1024;
	    long capacity_halfFreeSpace = Utils.getUsableSpace(fileCacheDir) / 2;
	    final long diskCacheSize =  capacity_20M < capacity_halfFreeSpace ? capacity_20M : capacity_halfFreeSpace;
	    
	    imageDiskLruCache = DiskLruCache.openCache(context, fileCacheDir, diskCacheSize);

	    // Use 1/8th of the available memory for this memory cache.
	    final int cacheSize = 1024 * 1024 * memClass / 8;    
	    
	    imageLruCache = new LruCache<String, Pair<Integer, WeakReference<Bitmap>>>(cacheSize){
	        @Override
	        protected int sizeOf(String key, Pair<Integer, WeakReference<Bitmap>> value) {
	            // The cache size will be measured in bytes rather than number of items.
	        	if(value == null) return 0;
	        	return value.first;
	        }
	        
	        @Override
	        protected void entryRemoved(boolean evicted, String key, Pair<Integer, WeakReference<Bitmap>> oldValue, Pair<Integer, WeakReference<Bitmap>> newValue){
	        	super.entryRemoved(evicted, key, oldValue, newValue);
	        }
	    };    
	}
	
	public void enableSampleSize(boolean b){
		Utils.enableSampleSize(b);
	}
	
	public boolean contains(String url){
		synchronized(this){
			return (null != imageLruCache.get(url));
		}
	}
	
//	private static void configOption(BitmapFactory.Options option, int maxWidth, int maxHeight){
//		int sampleSize = Utils.calculateInSampleSize(ImageCacheManager.getInstance().context, option, maxWidth, maxHeight);
//		option.inJustDecodeBounds = false;
//		option.inPurgeable = true;
//		option.inInputShareable = true;
//		option.inSampleSize = sampleSize;
//		option.inPreferredConfig = Bitmap.Config.ARGB_8888;
//	}
	
	public Bitmap loadBitmapFromResource(int resId){
		WeakReference<Bitmap> cached = getFromCache(String.valueOf(resId));
		if(cached != null && cached.get() != null) return cached.get();
		BitmapFactory.Options option = new BitmapFactory.Options();
		option.inPurgeable = true;
		option.inInputShareable = true;
		option.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Bitmap ret = BitmapFactory.decodeResource(context.getResources(), resId, option);
		if(ret != null){
			saveBitmapToCache(String.valueOf(resId), new WeakReference<Bitmap>(ret));
		}
		return ret;
	}
	
	public WeakReference<Bitmap> getFromCache(String url){
		WeakReference<Bitmap> bitmap = null;
		bitmap = this.getFromMapCache(url);		
		if(null == bitmap || null == bitmap.get()){			
			bitmap = getFromFileCache(url);
			if(bitmap != null && bitmap.get() != null){
				this.saveBitmapToCache(url, bitmap);
			}
		}		
		return bitmap;		
	}

	private WeakReference<Bitmap> getFromFileCache(String url){
		if(null != imageDiskLruCache){
			return new WeakReference<Bitmap>(imageDiskLruCache.get(Utils.getMD5(url)));
		}
		
		return null;
	}
	
	private WeakReference<Bitmap> getFromMapCache(String url){
		WeakReference<Bitmap> bitmap = null;
		synchronized (this){
			Pair<Integer, WeakReference<Bitmap>> p = imageLruCache.get(url);
			if (p != null)
			{
				bitmap = p.second;			
			}
		}
		return bitmap;
	}
	
	public void forceRecycle(String url, boolean rightNow){
		if(url == null || url.equals(""))return;
		WeakReference<Bitmap> bitmap = null;
		synchronized(this){
			Pair<Integer, WeakReference<Bitmap>> p = imageLruCache.remove(url);//anyway ,remove it from cache//imageLruCache.get(url);
			if (p != null)
			{
				bitmap = p.second;
			}
		}
		if(bitmap != null){
			Log.d("recycle", "hahaha remove unuesd bitmap~~~~~~~~~~~~~~~    " + url + ", recycle right now ? " + rightNow);
			if (rightNow)
			{
				if(bitmap.get() != null){
					bitmap.get().recycle();
				}
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
	public void postRecycle(){
		Thread t = new Thread(
				new Runnable() {
					
					@Override
					public void run() {
						
						List<WeakReference<Bitmap> > tmpList = null;
						synchronized (trashList) {
							tmpList = new ArrayList<WeakReference<Bitmap> >();
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
						
						
						for (WeakReference<Bitmap> bp : tmpList)
						{
							Log.d("recycle", "exe delay recycle bitmap " + bp);
							if(bp.get() != null){
								bp.get().recycle();
							}
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
		synchronized (this) {
			imageLruCache.evictAll();
		}
	}
	
	public void saveBitmapToCache(String url, WeakReference<Bitmap> bitmap){
		try{
			if(null != bitmap && bitmap.get() != null){
				synchronized (this){
					int bytes = bitmap.get().getHeight() * bitmap.get().getRowBytes();
					imageLruCache.put(url, new Pair<Integer, WeakReference<Bitmap>>(bytes, bitmap));
				}
			}
		}
		catch(Throwable t){
			//Ignor runtime exception to make sure everything works.
		}
	}
	
	public WeakReference<Bitmap> safeGetFromNetwork(String url) throws HttpException{
		WeakReference<Bitmap> bitmap = new WeakReference<Bitmap>(downloadImg(url));
		saveBitmapToCache(url, bitmap);		
		return bitmap;
	}
	
	public void putImageToDisk(String url, Bitmap bmp){
		String key = Utils.getMD5(url);
		imageDiskLruCache.put(key, bmp);
	}
	
	private static Bitmap decodeSampledBitmapFromFile(Context ctx, String file){
		
	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    if(Utils.useSampleSize()){	    	
			_Rect rc = new _Rect();
			rc.width = 200;
			rc.height = 200;
			WindowManager wm = 
					(WindowManager)ctx.getSystemService(Context.WINDOW_SERVICE);
			rc.width = wm.getDefaultDisplay().getWidth()/2;//shrink display to save memory
			rc.height = wm.getDefaultDisplay().getHeight()/2;//shrink display area to save memory
			
		    options.inJustDecodeBounds = true;
//		    BitmapFactory.decodeStream(stream, null, options);//.decodeStream(stream);
		    BitmapFactory.decodeFile(file, options);
		    
		    // Calculate inSampleSize
		    options.inSampleSize = Utils.calculateInSampleSize(ctx, options, rc.width, rc.height);
	    }
	    else{
	    	options.inSampleSize = 1;
	    }
	    
	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    options.inPurgeable = true;
//	    return BitmapFactory.decodeStream(stream, null, options);
	    return BitmapFactory.decodeFile(file, options);
	}
	
	
	public Bitmap downloadImg(String urlStr) throws HttpException{
//		HttpClient httpClient = null;
//        InputStream bis = null;
        Bitmap bitmapRet = null;
        
		try
		{
//			httpClient = NetworkProtocols.getInstance().getHttpClient();
//	        
//	        HttpGet httpGet = new HttpGet(urlStr); 
//	        HttpResponse response = httpClient.execute(httpGet);	
	        
	        String key = Utils.getMD5(urlStr);
	        final String targetFile = imageDiskLruCache.createFilePath(key);
	        FileDownloadCommand cmd = new  FileDownloadCommand(urlStr);
	        boolean succed = cmd.doDownload(context, new File(targetFile));
	        if (succed) {
	        	enableSampleSize(true);
	        	bitmapRet = decodeSampledBitmapFromFile(context, targetFile);
	        	enableSampleSize(false);
	        }
	        
	        
	        if(null != imageDiskLruCache && bitmapRet != null){
	        	imageDiskLruCache.put(key, bitmapRet, targetFile);
//	           	httpClient.getConnectionManager().shutdown();
//	           	httpClient = null;
//	           	enableSampleSize(true);
//	           	bitmapRet = imageDiskLruCache.get(key);
	        }
//	        else{
//	    		
//	    		InputStream inputStream = response.getEntity().getContent();
//	    		enableSampleSize(true);
//	    		bitmapRet = decodeSampledBitmapFromFile(inputStream);
//	    		enableSampleSize(false);
//	        }
            
            return bitmapRet;
		}
		catch (Exception e){			
		}
		finally
		{			
//			try
//			{
//				if(null != httpClient){
//					httpClient.getConnectionManager().shutdown();
//				}
//				
//				if(null != bis)
//				{
//					bis.close();
//				}				    				
//			}
//			catch (IOException e)
//			{
//				e.printStackTrace();
//			}catch(Exception e){
//				
//			}
		} 
		
		return null;
	}

	public void clearall(){
		String[] str = context.fileList();
		for (int i = 0; i < str.length; i++) {
			context.deleteFile(str[i]);
		}
	}

	public String getFileInDiskCache(String url) {
		if(null != imageDiskLruCache){
			return imageDiskLruCache.getFilePath(Utils.getMD5(url));
		}else{
			return "";
		}
	}
}