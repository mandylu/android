package com.baixing.sharing;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.baixing.activity.BaseActivity;
import com.baixing.broadcast.CommonIntentAction;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.Ad;
import com.baixing.entity.Ad.EDATAKEYS;
import com.baixing.tracking.LogData;
import com.baixing.tracking.TrackConfig;
import com.baixing.tracking.Tracker;
import com.baixing.util.Util;

public class SharingCenter{
	static private BaseSharingManager sm;
	public static String shareFrom = null;
	public static String adId = null;
	public static String categoryName = null;
	
	public static boolean isWeixinInstalled(Context ctx){
		return WeixinSharingManager.isWXInstalled(ctx);
	}

	public static void share2Weibo(BaseActivity activity, Ad ad){
		release();
		trackShareStart("weibo");
//		sm = new WeiboSharingManager(activity);
		sm = new WeiboSSOSharingManager(activity);
		registerReceiver(ad);
		sm.share(ad);
	}
	
	public static void share2Weixin(BaseActivity activity, Ad ad){
		release();
		trackShareStart("weixin");
		sm = new WeixinSharingManager(activity);
		registerReceiver(ad);
		sm.share(ad);
	}
	
	public static void share2QZone(BaseActivity activity, Ad ad){
		release();
		trackShareStart("qzone");
		sm = new QZoneSharingManager(activity);
		registerReceiver(ad);
		sm.share(ad);
	}
	
	public static void release(){
		if(sm != null){
			sm.release();
			sm = null;
		}
	}
	
	static private void registerReceiver(final Ad ad){
		GlobalDataManager.getInstance().getApplicationContext().registerReceiver(new BroadcastReceiver(){

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				Bundle bundle = intent.getExtras();
				String id = null;
				if(bundle != null){
					id = bundle.getString(CommonIntentAction.EXTRA_MSG_SHARED_AD_ID);
				}
				if(id == null || id.length() == 0){
					id = ad.getValueByKey(EDATAKEYS.EDATAKEYS_ID);
				}
				if(id != null && id.length() > 0){
					String savedIds = (String)Util.loadDataFromLocate(GlobalDataManager.getInstance().getApplicationContext(), CommonIntentAction.EXTRA_MSG_SHARED_AD_ID, String.class);
					if(savedIds != null && savedIds.length() > 0){
						id += "," + savedIds;
					}
					Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), CommonIntentAction.EXTRA_MSG_SHARED_AD_ID, id);
				}
				GlobalDataManager.getInstance().getApplicationContext().unregisterReceiver(this);
			}
			
		}, new IntentFilter(CommonIntentAction.ACTION_BROADCAST_SHARE_SUCCEED));
	}

	public static void trackShareResult(String channel, boolean success, String failReason) {
		LogData e = Tracker.getInstance().event(TrackConfig.TrackMobile.BxEvent.SHARE)
				.append(TrackConfig.TrackMobile.Key.SHARE_FROM, SharingCenter.shareFrom)
				.append(TrackConfig.TrackMobile.Key.SHARE_CHANNEL, channel)
				.append(TrackConfig.TrackMobile.Key.ADID, SharingCenter.adId)
				.append(TrackConfig.TrackMobile.Key.SECONDCATENAME, SharingCenter.categoryName)
				.append(TrackConfig.TrackMobile.Key.RESULT, success ? TrackConfig.TrackMobile.Value.YES : TrackConfig.TrackMobile.Value.NO);
		if(!success) e.append(TrackConfig.TrackMobile.Key.FAIL_REASON, failReason);
		e.end();
	}

	public static void trackShareStart(String channel) {
		Tracker.getInstance().event(TrackConfig.TrackMobile.BxEvent.SHARE_START)
				.append(TrackConfig.TrackMobile.Key.SHARE_FROM, SharingCenter.shareFrom)
				.append(TrackConfig.TrackMobile.Key.SHARE_CHANNEL, channel)
				.append(TrackConfig.TrackMobile.Key.ADID, SharingCenter.adId)
				.append(TrackConfig.TrackMobile.Key.SECONDCATENAME, SharingCenter.categoryName)
				.end();
	}
}