//xumengyi@baixing.com
package com.baixing.imageCache;

import android.graphics.Bitmap;

interface ImageLoaderCallback{
	void refresh(String url,Bitmap bitmap);	
	void fail(String url);	
	Object getObject();	
}
