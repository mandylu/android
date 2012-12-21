//liuchong@baixing.com
package com.baixing.util;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

public class UploadImageCommand implements Runnable {

	private Context context;
	private String bmpPath;
	private ProgressListener listener;
	private boolean inProgress;


	public UploadImageCommand(Context context, String path) {
		super();
		this.bmpPath = path;
		this.context = context;
	}
	
	public boolean startUpload(ProgressListener listener)
	{
		synchronized(this)
		{
			if (inProgress)
			{
				return false;
			}
			
			inProgress = true;
			
			if (listener != null)
			{
				this.listener = listener;
			}
			else
			{
				this.listener = new ProgressListener () { //Avoid null check
					public void onStart(String imagePath) {
					}

					public void onCancel(String imagePath){}
					
					public void onFinish(Bitmap bmp, String imagePath) {
					}};
					
					
			}
		}
		
		
		Thread t = new Thread(this);
		t.start();
		
		return true;
	}
	
	public static interface ProgressListener {
		void onStart(String imagePath);
		void onCancel(String imagePath);
		void onFinish(Bitmap bmp, String imagePath);
	}

	
	@Override
	public void run() {

		try
		{
			listener.onStart(bmpPath);

			synchronized (this) {

				// ++ uploadCount;

				if (bmpPath == null || bmpPath.equals("")) {
					listener.onCancel(bmpPath);
					return;
				}

				Uri uri = Uri.parse(bmpPath);
				String path = getRealPathFromURI(uri); // from Gallery
				if (path == null) {
					path = uri.getPath(); // from File Manager
				}
				Bitmap currentBmp = null;
				if (path != null) {
					try {

						BitmapFactory.Options bfo = new BitmapFactory.Options();
						bfo.inJustDecodeBounds = true;
						BitmapFactory.decodeFile(path, bfo);

						BitmapFactory.Options o = new BitmapFactory.Options();
						o.inPurgeable = true;

						int maxDim = 600;

						o.inSampleSize = getClosestResampleSize(bfo.outWidth,
								bfo.outHeight, maxDim);

						currentBmp = BitmapFactory.decodeFile(path, o);
						// photo = Util.newBitmap(tphoto, 480, 480);
						// tphoto.recycle();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (currentBmp == null) {
//					--uploadCount;
					listener.onCancel(bmpPath);
					return;
				}

				String result = Communication.uploadPicture(currentBmp);
//				--uploadCount;
				listener.onFinish(currentBmp, result);
				
				currentBmp.recycle();
				currentBmp = null;
			}
		}
		finally {
			inProgress = false;
			listener = null;
		}
		
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

	
	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);//.managedQuery(contentUri, proj, null, null, null);

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
