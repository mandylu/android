/**
 *ImageLoaderCallback.java
 *2011-10-13 下午10:04:31
 *Touch Android
 *http://bbs.droidstouch.com
 */
package com.baixing.imageCache;

import android.graphics.Bitmap;

public interface ImageLoaderCallback
{

	void refresh(String url,Bitmap bitmap);
	
	void fail(String url);
	
	Object getObject();
	
}
