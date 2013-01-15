package com.baixing.sharing;

import android.app.Activity;

import com.baixing.entity.Ad;

public class SharingCenter{
	static private BaseSharingManager sm;
	
	public static void share2Weibo(Activity activity, Ad ad){
		release();
		sm = new WeiboSharingManager(activity);
		sm.share(ad);
	}
	
	public static void share2Weixin(Activity activity, Ad ad){
		release();
		sm = new WeixinSharingManager(activity);
		sm.share(ad);
	}
	
	public static void share2QZone(Activity activity, Ad ad){
		release();
		sm = new QZoneSharingManager(activity);
		sm.share(ad);
	}
	
	public static void release(){
		if(sm != null){
			sm.release();
			sm = null;
		}
	}
}