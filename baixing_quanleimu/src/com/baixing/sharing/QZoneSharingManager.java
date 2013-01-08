package com.baixing.sharing;

import java.text.SimpleDateFormat;
import java.util.Date;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.Ad;
import com.baixing.entity.Ad.EDATAKEYS;
import com.baixing.entity.ImageList;
import com.tencent.tauth.TencentOpenAPI;
import com.tencent.tauth.TencentOpenAPI2;
import com.tencent.tauth.TencentOpenHost;
import com.tencent.tauth.bean.OpenId;
import com.tencent.tauth.http.Callback;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class QZoneSharingManager implements Callback {
	private String mAccessToken;
	private String mOpenId;
	private AuthReceiver receiver;
	static final String mAppid = "100358719";
	private Activity mActivity;

	public QZoneSharingManager(Activity startingActivity){
		mActivity = startingActivity;
		registerIntentReceivers();
	}
	
	public void share2QZone(Ad ad){
		Bundle bundle = new Bundle();

		bundle.putString("title", ad.getValueByKey(EDATAKEYS.EDATAKEYS_TITLE));
		bundle.putString("url", ad.getValueByKey(EDATAKEYS.EDATAKEYS_LINK));
		
//		//用户评论内容，也叫发表分享时的分享理由。禁止使用系统生产的语句进行代替。最长40个中文字，超出部分会被截断。
//		bundle.putString("comment", ("QQ登录SDK：测试comment" + new Date()));
		
		bundle.putString("summary", ad.getValueByKey(EDATAKEYS.EDATAKEYS_DESCRIPTION));
		ImageList il = ad.getImageList();
		if(il != null){
			String resize180 = il.getResize180();
			resize180 = resize180.replaceAll(",", "|");
			bundle.putString("images", resize180);
		}
		
		//分享内容的类型。4表示网页；5表示视频（type=5时，必须传入playurl）。
		bundle.putString("type", "4");
		
		TencentOpenAPI2.sendStore(mActivity.getApplicationContext(), mAccessToken, mAppid, mOpenId, "_self", bundle, new Callback() {
			@Override
			public void onSuccess(final Object obj) {
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
			}
		}, null);
	}
	
	public void destroy(){
		if (receiver != null) {
			unregisterIntentReceivers();
		}
	}

	private void auth() {
		Intent intent = new Intent(mActivity, com.tencent.tauth.TAuthView.class);
		intent.putExtra(TencentOpenHost.CLIENT_ID, mAppid);
		intent.putExtra(
				TencentOpenHost.SCOPE,
				"get_user_info,get_user_profile,add_share,add_topic,list_album,upload_pic,add_album");
		intent.putExtra(TencentOpenHost.TARGET, "_blank");
		// intent.putExtra(TencentOpenHost.CALLBACK, this);
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

				// 用access token 来获取open id
				TencentOpenAPI.openid(access_token, new Callback() {

					public void onCancel(int flag) {

					}

					@Override
					public void onSuccess(final Object obj) {
						mActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								mOpenId = ((OpenId) obj).getOpenId();
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
}