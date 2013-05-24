//xumengyi@baixing.com
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
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.baixing.data.GlobalDataManager;
import com.baixing.entity.BXThumbnail;

/**
 * Class containing some static utility methods.
 */
public class BitmapUtils {
	public static final String TAG = "BitmapUtils";
	
	public static final int DEFAULT_THUMBNAIL_WIDTH = 200;
	public static final int DEFAULT_THUMBNAIL_HEIGHT = 200; 

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

	private static Bitmap getBitmap(String path)
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
	
	public static int getClosestResampleSize(int cx, int cy, int maxDim)
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
        
        if (resample > 1)
        {
        	//For fucking LenovoA60 device: only 1, 2, 4, 8, 16, 32 ... works on this device.
			if (resample < 4) {
				resample = 2;
			} else if (resample < 8) {
				resample = 4;
			} else {
				resample = 8;
			}          

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
	
	private static File getOutputMediaFile() {
		String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/bx/";
		File dirF = new File(dir);
		dirF.mkdirs();
		
//		return new File(Environment.getExternalStorageDirectory(), "bx_" + System.currentTimeMillis() + ".jpg");
		return new File(dirF, "bx_" + System.currentTimeMillis() + ".jpg");
	}
	
	public static final BXThumbnail copyAndCreateThrmbnail(String sourceFile, Context context) {
		String savedPath = getOutputMediaFile().getAbsolutePath();
		if (savedPath == null) {
			return null;
		}
		
		Bitmap source = null;
		try {
			FileOutputStream fos = new FileOutputStream(savedPath);
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPurgeable = true;
			options.inJustDecodeBounds = true;
			
			BitmapFactory.decodeFile(sourceFile, options);
			
			options.inJustDecodeBounds = false;
			options.inSampleSize = getClosestResampleSize(options.outWidth, options.outHeight, 600);
			
			source = BitmapFactory.decodeFile(sourceFile, options);
			
			source.compress(CompressFormat.JPEG, 100, fos);
			
			fos.close();
		}
		catch (Throwable t) {
			Log.e(TAG, "copy image failed." + t.getMessage());
		}
		finally {
			if (source != null) {
//				try {
//					ExifInterface original = new ExifInterface(sourceFile);
//					ExifInterface target = new ExifInterface(savedPath);
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				//TODO: copy EXIF.
				
			}
		}
		
		if (source == null) {
			return null;
		}

		try {
			Bitmap bp = Bitmap.createScaledBitmap(source, DEFAULT_THUMBNAIL_WIDTH, DEFAULT_THUMBNAIL_HEIGHT, true);
			
			source.recycle();
			return BXThumbnail.createThumbnail(savedPath, bp);
		} catch (Throwable t) {
			return BXThumbnail.createThumbnail(savedPath, source);
		}
	}
	
	/**
	 * Save the data to persist data and then return the thumbnail.
	 * 
	 * @param data
	 * @param rotation value range [0, 360]. Target image data should  rotate this degree when saved to file.
	 * @param context 
	 * @param isMirror if true, an mirror transform should be added when saving the target data.
	 * @return return the saved file path and thumbnail which can be shown to user.
	 */
	public static final BXThumbnail saveAndCreateThumbnail(byte[] data, int rotation, Context context, boolean isMirror) {
		String savedPath = getOutputMediaFile().getAbsolutePath();
		if (savedPath == null) {
			return null;
		}
		
		Bitmap source = null;
		try {
            FileOutputStream fos = new FileOutputStream(savedPath);
            
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPurgeable = true;
            options.inJustDecodeBounds = true;
            
            BitmapFactory.decodeByteArray(data, 0, data.length, options);
            
            options.inJustDecodeBounds = false;
            options.inSampleSize = getClosestResampleSize(options.outWidth, options.outHeight, 600);
            
            source = BitmapFactory.decodeByteArray(data, 0, data.length, options);//Bitmap.createScaledBitmap(bitmap, 200, 200, false);
            
            source.compress(CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        } catch (Throwable t) {
        	Log.d(TAG, "other exception when processing image." + t.getMessage());
        }
		
		if (source == null) {
			return null;
		}
        
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPurgeable = true;
        
		try {
			Matrix m = new Matrix();
			if (isMirror) {
				m.setValues(new float[] {-1, 0, 0, 
										 0, 1, 0, 
										 0, 0, 1});
			}
			m.postRotate(rotation);
			float scaleW = (float)DEFAULT_THUMBNAIL_WIDTH / (float) source.getWidth();
			float scaleH = (float) DEFAULT_THUMBNAIL_HEIGHT / (float) source.getHeight();
			m.postScale(scaleW, scaleH);

			Bitmap out = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), m, false);
			return BXThumbnail.createThumbnail(savedPath, out);
		} catch (Throwable e) {
			Log.d(TAG, "save and create image failed " + e.getMessage());
		}
		finally {
			 ExifInterface exif = null;
				try {
					exif = new ExifInterface(savedPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (exif != null) {
					exif.setAttribute(ExifInterface.TAG_ORIENTATION, getExifOrientation(rotation));
					
					try {
						exif.saveAttributes();
					} catch (IOException e) {
						//Igonr exception
					}
				}
		}
		
		return null;
	}
	
	private static String getExifOrientation(int degree) {
		switch (degree) {
		case 0:
			return "" + ExifInterface.ORIENTATION_NORMAL;
		case 90:
			return "" + ExifInterface.ORIENTATION_ROTATE_90;
		case 180:
			return "" + ExifInterface.ORIENTATION_ROTATE_180;
		case 270:
			return "" + ExifInterface.ORIENTATION_ROTATE_270;
			default:
				return "" + ExifInterface.ORIENTATION_UNDEFINED;
		}
	}

}
