package com.baixing.sharing;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.baixing.broadcast.CommonIntentAction;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.Ad;
import com.baixing.entity.Ad.EDATAKEYS;
import com.baixing.entity.ImageList;
import com.baixing.imageCache.ImageCacheManager;
import com.baixing.util.Util;
import com.tencent.tauth.TencentOpenAPI;
import com.tencent.tauth.TencentOpenAPI2;
import com.tencent.tauth.TencentOpenHost;
import com.tencent.tauth.bean.OpenId;
import com.tencent.tauth.http.Callback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class QZoneSharingManager  extends BaseSharingManager implements Callback{
	private final int MSG_AUTO_SUCCEED = 1;
	private final int MSG_AUTO_FAIL = 2;
	private final int MSG_UPLOADIMG_FINISH = 3;
	private String mAccessToken;
	private String mOpenId;
	private AuthReceiver receiver;
	static final String mAppid = "100358719";
	private Activity mActivity;
	private Ad mAd;
	private String mImageUrl;
	static final public String STRING_ACCESS_TOKEN = "qzoneaccess";
	static final public String STRING_OPENID = "qzonopenid";

	public QZoneSharingManager(Activity startingActivity){
		mActivity = startingActivity;
		registerIntentReceivers();
		mAccessToken = (String)Util.loadDataFromLocate(mActivity, STRING_ACCESS_TOKEN, String.class);
		mOpenId = (String)Util.loadDataFromLocate(mActivity, STRING_OPENID, String.class);
	}
	
	private void uploadImage(){
		if(mAd == null) return;
		ImageList il = mAd.getImageList();
		if(il != null){
			String big = il.getBig();
			if(big != null && big.length() > 0){
				String path = ImageCacheManager.getInstance().getFileInDiskCache(big.split(",")[0]);
				if(path != null && path.length() > 0){
					
					StringBuilder sb = new StringBuilder();
					FileInputStream fis = null;
					try{
						File file = new File(path);
//						fis = mActivity.openFileInput(path);
						fis =  new FileInputStream(file);
						
						byte[] fileContent = new byte[1024];
						int numRead = 0;
						while((numRead = fis.read(fileContent)) > 0){
							String fc = new String(fileContent);
							sb.append(fc);
						}					
					}catch(FileNotFoundException e){
						
					}catch(IOException e){
						
					}finally{
						if(fis != null){
							try{
								fis.close();
							}catch(IOException e){
								
							}
						}
					}
					if(sb != null && sb.length() > 0){
						Bundle bundle = new Bundle();
						bundle.putByteArray("picture", sb.toString().getBytes());
						TencentOpenAPI.uploadPic(this.mAccessToken, mAppid, this.mOpenId, bundle, new Callback(){
	
							@Override
							public void onCancel(int arg0) {
								// TODO Auto-generated method stub
								handler.sendEmptyMessage(MSG_UPLOADIMG_FINISH);
							}
	
							@Override
							public void onFail(int arg0, String arg1) {
								// TODO Auto-generated method stub
								handler.sendEmptyMessage(MSG_UPLOADIMG_FINISH);
							}
	
							@Override
							public void onSuccess(Object arg0) {
								// TODO Auto-generated method stub
								Message msg = Message.obtain();
								msg.what = MSG_UPLOADIMG_FINISH;
								msg.obj = arg0;
								handler.sendMessage(msg);
							}
							
						});
						return;
					}
				}
			}
		}
		handler.sendEmptyMessage(MSG_UPLOADIMG_FINISH);
	}
	
	private void share2QZone(final Ad ad){
		Bundle bundle = new Bundle();

		bundle.putString("title", ad.getValueByKey(EDATAKEYS.EDATAKEYS_TITLE));		
		bundle.putString("url", ad.getValueByKey(EDATAKEYS.EDATAKEYS_LINK));		
		bundle.putString("comment", 
				"我用百姓网App发布了：" + ad.getValueByKey(EDATAKEYS.EDATAKEYS_TITLE) + "，麻烦朋友们帮忙转发一下～");		
		bundle.putString("summary", ad.getValueByKey(EDATAKEYS.EDATAKEYS_DESCRIPTION));
		if(mImageUrl != null && mImageUrl.length() > 0){
			bundle.putString("images", mImageUrl);
		}				
		//分享内容的类型。4表示网页；5表示视频（type=5时，必须传入playurl）。
		bundle.putString("type", "4");
		
		TencentOpenAPI2.sendStore(mActivity, mAccessToken, mAppid, mOpenId, "_self", bundle, new Callback() {
			@Override
			public void onSuccess(final Object obj) {
				SharingCenter.trackShareResult("qzone", true, null);
				Context ctx = GlobalDataManager.getInstance().getApplicationContext();
				if(ctx != null){
					Intent intent = new Intent(CommonIntentAction.ACTION_BROADCAST_SHARE_SUCCEED);
					intent.putExtra(CommonIntentAction.EXTRA_MSG_SHARED_AD_ID, ad.getValueByKey(EDATAKEYS.EDATAKEYS_ID));
					ctx.sendBroadcast(intent);
				}
				mActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast toast = Toast.makeText(mActivity.getApplicationContext(), "成功分享到QQ空间！", Toast.LENGTH_SHORT);
						toast.show();
					}
				});
			}
			   		
			@Override
			public void onFail(final int ret, final String msg) {
				SharingCenter.trackShareResult("qzone", false, "code:" + ret + " msg:" + msg);
				mActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						Toast toast = Toast.makeText(mActivity.getApplicationContext(), "分享失败，错误信息:" + ret + ", " + msg, Toast.LENGTH_SHORT);
						toast.show();
					}
				});
			}
			   
			@Override
			public void onCancel(int flag) {
				SharingCenter.trackShareResult("qzone", false, "cancel_flag:" + flag);
			}
		}, null);
		Context ctx = GlobalDataManager.getInstance().getApplicationContext();
		if(ctx != null){
			Intent intent = new Intent(CommonIntentAction.ACTION_BROADCAST_SHARE_SUCCEED);
			intent.putExtra(CommonIntentAction.EXTRA_MSG_SHARED_AD_ID, ad.getValueByKey(EDATAKEYS.EDATAKEYS_ID));
			ctx.sendBroadcast(intent);
		}

	}
	
	@Override
	public void auth() {
		Intent intent = new Intent(mActivity, com.tencent.tauth.TAuthView.class);
		intent.putExtra(TencentOpenHost.CLIENT_ID, mAppid);
		intent.putExtra(
				TencentOpenHost.SCOPE,
				"get_user_info,get_user_profile,add_share,add_topic,list_album,upload_pic,add_album");
		intent.putExtra(TencentOpenHost.TARGET, "_self");
//		intent.putExtra(TencentOpenHost.CALLBACK, "auth://tauth.qq.com/");
		mActivity.startActivity(intent);
	}

	private void registerIntentReceivers() {
		receiver = new AuthReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(TencentOpenHost.AUTH_BROADCAST);
		GlobalDataManager.getInstance().getApplicationContext().registerReceiver(receiver, filter);
	}

	private void unregisterIntentReceivers() {
		GlobalDataManager.getInstance().getApplicationContext().unregisterReceiver(receiver);
	}
	
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == MSG_AUTO_SUCCEED){
				uploadImage();
			}else if(msg.what == MSG_UPLOADIMG_FINISH){
				share2QZone(mAd);
			}
		}
	};

	class AuthReceiver extends BroadcastReceiver {

		private static final String TAG = "AuthReceiver";

		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle exts = intent.getExtras();
			String raw = exts.getString("raw");
			String access_token = exts.getString(TencentOpenHost.ACCESS_TOKEN);
			String expires_in = exts.getString(TencentOpenHost.EXPIRES_IN);
			String error_ret = exts.getString(TencentOpenHost.ERROR_RET);
			String error_des = exts.getString(TencentOpenHost.ERROR_DES);

			Date date = new Date(Long.valueOf(expires_in));
			SimpleDateFormat sDateFormat = new SimpleDateFormat("MM-dd HH:mm");
			String dt = sDateFormat.format(date);

			Log.d(TAG, String.format("raw: %s, access_token:%s, expires_in:%s",
					raw, access_token, dt));
			if (access_token != null) {
				mAccessToken = access_token;
				Util.saveDataToLocate(mActivity, STRING_ACCESS_TOKEN, mAccessToken);

				// 用access token 来获取open id
				TencentOpenAPI.openid(access_token, new Callback() {

					public void onCancel(int flag) {
					}

					@Override
					public void onSuccess(final Object obj) {
						mActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mOpenId = ((OpenId)obj).getOpenId();
								Util.saveDataToLocate(mActivity, STRING_OPENID, mOpenId);
								if (handler != null) {
									handler.sendEmptyMessage(MSG_AUTO_SUCCEED);
								}
							}
						});
					}

					@Override
					public void onFail(int ret, final String msg) {
						mActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(mActivity.getApplicationContext(), msg, 0);
							}
						});
					}
				});
			}
			if (error_ret != null) {
			}
		}
	}

	@Override
	public void onCancel(int arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFail(int arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSuccess(Object arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void share(Ad ad) {
		// TODO Auto-generated method stub
		mAd = ad;
		if(mAccessToken == null || mOpenId == null){
			auth();
		}else if(ad.getImageList() != null && ad.getImageList().getBig() != null && ad.getImageList().getBig().length() > 0){
			uploadImage();
		}else{
			share2QZone(ad);
		}
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		if (receiver != null) {
			unregisterIntentReceivers();
		}		
	}
}