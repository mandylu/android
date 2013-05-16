package com.baixing.imageCache;

import java.io.File;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.view.WindowManager;

class Utils{
    
    public static class _Rect{
    	public int width = 0;
    	public int height =0;
    	public _Rect(){
    		this.width = 0;
    		this.height = 0;
    	}
    };   

    /**
     * Check how much usable space is available at a given path.
     *
     * @param path The path to check
     * @return The space available in bytes
     */
    @SuppressLint("NewApi")
    public static long getUsableSpace(File path) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
//            return path.getUsableSpace();
//        }
        
        try{
	        final StatFs stats = new StatFs(path.getPath());
	        return (long) stats.getBlockSize() * (long) stats.getAvailableBlocks();
        }catch(Exception e){
        	e.printStackTrace();
        }
        
        return -1;
    }
    
	
    private static boolean useSampleSize = false;
    
    public static boolean useSampleSize(){
    	return useSampleSize;
    }
    
    public static void enableSampleSize(boolean b){
    	useSampleSize = b;
    }	
	
	public static int calculateInSampleSize(Context ctx, BitmapFactory.Options options, int reqWidth, int reqHeight) {
		if(reqWidth <= 0 && reqHeight <= 0){
			if(ctx != null){
				WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
				DisplayMetrics metrics = new DisplayMetrics();
				wm.getDefaultDisplay().getMetrics(metrics);
				reqWidth = metrics.widthPixels;
				reqHeight = metrics.heightPixels;
			}
		}
		final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	        if (width > height) {
	            inSampleSize = Math.round((float)height / (float)reqHeight);
	        } else {
	            inSampleSize = Math.round((float)width / (float)reqWidth);
	        }
	    }
	    return inSampleSize;
	}
	

    /**
     * Check if external storage is built-in or removable.
     *
     * @return True if external storage is removable (like an SD card), false
     *         otherwise.
     */
    @SuppressLint("NewApi")
    public static boolean isExternalStorageRemovable() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
//            return Environment.isExternalStorageRemovable();
//        }
        return true;
    }
    
    /**
     * Get the external app cache directory.
     *
     * @param context The context to use
     * @return The external cache dir
     */
    @SuppressLint("NewApi")
    public static File getExternalCacheDir(Context context) {
//        if (hasExternalCacheDir()) {
//            return context.getExternalCacheDir();
//        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }
    
    
    public static boolean isPathValidForDiskCache(File path){
    	
    	boolean bPathExists = path.exists() && path.canWrite();
        
        return bPathExists;
    }

    public static String getMD5(String strMD5)
    {
        byte[] source = strMD5.getBytes();
        String s = null;
        char hexDigits[] =
        { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        try
        {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            md.update(source);
            byte tmp[] = md.digest();
            char str[] = new char[16 * 2];
            int k = 0;
            for (int i = 0; i < 16; i++)
            {
                byte byte0 = tmp[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            s = new String(str);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return s;
    }
}