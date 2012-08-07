/**
 *ImageLoaderCallback.java
 *2011-10-13 下午10:04:31
 *Touch Android
 *http://bbs.droidstouch.com
 */
package com.quanleimu.imageCache;

import android.graphics.Bitmap;

public interface ImageLoaderCallback
{

	void refresh(String url,Bitmap bitmap);
	
	Object getObject();
	
}
