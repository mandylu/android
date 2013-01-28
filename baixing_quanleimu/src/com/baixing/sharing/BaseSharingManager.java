package com.baixing.sharing;

import com.baixing.entity.Ad;
import com.baixing.entity.ImageList;
import com.baixing.imageCache.ImageCacheManager;

public abstract class BaseSharingManager{
	public abstract void share(Ad ad);
	public abstract void auth();
	public abstract void release();
	protected static String getThumbnailUrl(Ad goodDetail){
		ImageList il = goodDetail.getImageList();
		String imgUrl = "";
		String imgPath = "";
		if(il != null){
			imgUrl = il.getResize180();
			if(imgUrl != null && imgUrl.length() > 0){
				imgPath = ImageCacheManager.getInstance().getFileInDiskCache(imgUrl.split(",")[0]);
			}
			
			if(imgPath == null || imgPath.length() == 0){
				imgUrl = il.getSquare();
				if(imgUrl != null){
					imgUrl = imgUrl.split(",")[0];
				}
			}
		}
		return imgUrl;
	}
}
