/**
 *ImageLoaderCallback.java
 *2011-10-13 下午10:04:31
 *Touch Android
 *http://bbs.droidstouch.com
 */
package com.quanleimu.imageCache2SD;

import android.graphics.Bitmap;

public interface ImageLoaderCallback2SD
{

	void refresh(String url,Bitmap bitmap);
	
}
