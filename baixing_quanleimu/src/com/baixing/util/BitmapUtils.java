/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.baixing.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.view.WindowManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.baixing.data.GlobalDataManager;

/**
 * Class containing some static utility methods.
 */
public class BitmapUtils {
	
    private static boolean useSampleSize = false;
    
    public static void enableSampleSize(boolean b){
    	useSampleSize = b;
    }
    
    public static class _Rect{
    	public int width = 0;
    	public int height =0;
    	public _Rect(){
    		this.width = 0;
    		this.height = 0;
    	}
    };
    
    public static final int IO_BUFFER_SIZE = 8 * 1024;

    private BitmapUtils() {};

    /**
     * Workaround for bug pre-Froyo, see here for more info:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     */
    public static void disableConnectionReuseIfNecessary() {
        // HTTP connection reuse which was buggy pre-froyo
        if (hasHttpConnectionBug()) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    /**
     * Get the size in bytes of a bitmap.
     * @param bitmap
     * @return size in bytes
     */
    @SuppressLint("NewApi")
    public static int getBitmapSize(Bitmap bitmap) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
//            return bitmap.getByteCount();
//        }
        // Pre HC-MR1
        return bitmap.getRowBytes() * bitmap.getHeight();
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
    
    public static boolean isPathValidForDiskCache(File path){
    	
    	boolean bPathExists = path.exists() && path.canWrite();
        
        return bPathExists;
    }

    /**
     * Get the memory class of this device (approx. per-app memory limit)
     *
     * @param context
     * @return
     */
    public static int getMemoryClass(Context context) {
        return ((ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE)).getMemoryClass();
    }

    /**
     * Check if OS version has a http URLConnection bug. See here for more information:
     * http://android-developers.blogspot.com/2011/09/androids-http-clients.html
     *
     * @return
     */
    public static boolean hasHttpConnectionBug() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO;
    }

    /**
     * Check if OS version has built-in external cache dir method.
     *
     * @return
     */
    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

//    /**
//     * Check if ActionBar is available.
//     *
//     * @return
//     */
//    public static boolean hasActionBar() {
//        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
//    }
	
	public static Bitmap decodeSampledBitmapFromFile(String fileName) {
	
		if(null == fileName)	return null;
		if(!(new File(fileName)).exists()) return null;
		
	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    if(useSampleSize){
			_Rect rc = new _Rect();
			rc.width = 200;
			rc.height = 200;
			WindowManager wm = 
					(WindowManager)GlobalDataManager.getInstance().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
			rc.width = wm.getDefaultDisplay().getWidth()/2;//shrink display to save memory
			rc.height = wm.getDefaultDisplay().getHeight()/2;//shrink display area to save memory
			
		    options.inJustDecodeBounds = true;
		    BitmapFactory.decodeFile(fileName,options);
	
		    // Calculate inSampleSize
		    options.inSampleSize = calculateInSampleSize(options, rc.width, rc.height);
	    }
	    else{
	    	options.inSampleSize = 1;
	    }
	    
	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    options.inPurgeable = true;
	    return BitmapFactory.decodeFile(fileName, options);
	}
	
	public static Bitmap decodeSampledBitmapFromFile(InputStream stream){
	
	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    if(useSampleSize){	    	
			_Rect rc = new _Rect();
			rc.width = 200;
			rc.height = 200;
			WindowManager wm = 
					(WindowManager)GlobalDataManager.getInstance().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
			rc.width = wm.getDefaultDisplay().getWidth()/2;//shrink display to save memory
			rc.height = wm.getDefaultDisplay().getHeight()/2;//shrink display area to save memory
			
		    options.inJustDecodeBounds = true;
		    BitmapFactory.decodeStream(stream);
	
		    // Calculate inSampleSize
		    options.inSampleSize = calculateInSampleSize(options, rc.width, rc.height);
	    }
	    else{
	    	options.inSampleSize = 1;
	    }
	    
	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    options.inPurgeable = true;
	    return BitmapFactory.decodeStream(stream);
	}
	
	public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	        inSampleSize = Math.round((float)height / (float)reqHeight);
	        int t = Math.round((float)width / (float)reqWidth);
	        if(t > inSampleSize) inSampleSize = t;
	        
	    }
	    
	    return inSampleSize;
	}
	
	static public Bitmap createThumbnail(String path, int thunbWidth, int thumbHeight)
	{
		Bitmap srcBmp = getBitmap(path);
		if (srcBmp == null)
		{
			return null;
		}
		
//		Float width  = Float.valueOf(srcBmp.getWidth());
//		Float height = Float.valueOf(srcBmp.getHeight());
//		Float ratio = thumbHeight/height;
		Bitmap thumbnail = Bitmap.createScaledBitmap(srcBmp, thunbWidth, thumbHeight, true);

		if (thumbnail != srcBmp)
		{
			srcBmp.recycle();
		}
		
		return thumbnail;
	}

	public static Bitmap getBitmap(String path)
	{
		Bitmap currentBmp = null;
		if (path != null) {
			try {
			    
			    BitmapFactory.Options bfo = new BitmapFactory.Options();
		        bfo.inJustDecodeBounds = true;
		        BitmapFactory.decodeFile(path, bfo);
		        
			    BitmapFactory.Options o =  new BitmapFactory.Options();
                o.inPurgeable = true;
                
                int maxDim = 600;
                
                o.inSampleSize = getClosestResampleSize(bfo.outWidth, bfo.outHeight, maxDim);
                
                
                currentBmp = BitmapFactory.decodeFile(path, o);
                
                return currentBmp;
				//photo = Util.newBitmap(tphoto, 480, 480);
				//tphoto.recycle();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}			
		
		return null;
	}
	
	public static String saveBitmapToSdCard(String path,String name,Bitmap bitmap) {
		String res = null;
		FileOutputStream fos = null; 
		if (Environment.getExternalStorageState() != null) {
			try {
				File p = new File("/sdcard/" + "deviceTool"); // ����Ŀ¼
				File s = new File("/sdcard/" + "deviceTool" + "/" + path); // ����Ŀ¼
				File f = new File("/sdcard/" + "deviceTool" + "/" + path + "/" + name + ".png"); // �����ļ�
				if (!p.exists()) {
					p.mkdir();
				}
				if (!s.exists()) {
					s.mkdir();
				}
				if (!f.exists()) {
					f.createNewFile();
				}
				fos = new FileOutputStream(f);
				
				bitmap.compress(CompressFormat.JPEG, 100, fos);
				fos.close();
				res = f.getAbsolutePath();
			} catch (FileNotFoundException e) {
				res = "û���ҵ��ļ�";
				e.printStackTrace();
			} catch (Exception e) {
				res = "SD��δ��װ";
				e.printStackTrace();
			}
		}else{
			res = "��SD��";
		}
		return res;
	}
	
	private static int getClosestResampleSize(int cx, int cy, int maxDim)
    {
        int max = Math.max(cx, cy);
        
        int resample = 1;
        for (resample = 1; resample < Integer.MAX_VALUE; resample++)
        {
            if (resample * maxDim > max)
            {
                resample--;
                break;
            }
        }
        
        if (resample > 0)
        {
            return resample;
        }
        return 1;
    }
	
	public static String getRealPathFromURI(Activity context, Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = context.managedQuery(contentUri, proj, null, null, null);

		if (cursor == null)
			return null;

		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

		cursor.moveToFirst();

		String ret = cursor.getString(column_index);
//		cursor.close();
		return ret;
	}

}
