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

import com.baixing.activity.BaseActivity;
import com.baixing.broadcast.CommonIntentAction;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.Ad;
import com.baixing.entity.Ad.EDATAKEYS;
import com.baixing.entity.ImageList;
import com.baixing.imageCache.ImageCacheManager;
import com.baixing.util.Util;
import com.tencent.open.HttpStatusException;
import com.tencent.open.NetworkUnavailableException;
import com.tencent.tauth.Constants;
import com.tencent.tauth.IRequestListener;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import com.tencent.tauth.bean.OpenId;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONException;
import org.json.JSONObject;

public class QZoneSharingManager  extends BaseSharingManager implements IUiListener{
	private final int MSG_AUTO_SUCCEED = 1;
	private final int MSG_AUTO_FAIL = 2;
	private final int MSG_UPLOADIMG_FINISH = 3;
	private String mAccessToken;
	private String mOpenId;
	private String mExpires_in;
//	private AuthReceiver receiver;
	public static final String mAppid = "100358719";
	private Activity mActivity;
	private Ad mAd;
	private String mImageUrl;
	private Tencent mTencent;
	static final public String STRING_ACCESS_TOKEN = "qzoneaccess";
	static final public String STRING_OPENID = "qzonopenid";
	static final public String STRING_EXPIRES_IN = "expires_in";

	public QZoneSharingManager(Activity startingActivity){
		mActivity = startingActivity;
		mAccessToken = (String)Util.loadDataFromLocate(mActivity, STRING_ACCESS_TOKEN, String.class);
		mOpenId = (String)Util.loadDataFromLocate(mActivity, STRING_OPENID, String.class);
		mExpires_in = (String)Util.loadDataFromLocate(mActivity, STRING_EXPIRES_IN, String.class);
		mTencent = Tencent.createInstance(mAppid, mActivity.getApplicationContext());
		if(mAccessToken != null && mOpenId != null && mExpires_in != null){
			mTencent.setAccessToken(mAccessToken, mExpires_in);
			mTencent.setOpenId(mOpenId);
		}
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
//						TencentOpenAPI.uploadPic(this.mAccessToken, mAppid, this.mOpenId, bundle, new Callback(){
//	
//							@Override
//							public void onCancel(int arg0) {
//								// TODO Auto-generated method stub
//								handler.sendEmptyMessage(MSG_UPLOADIMG_FINISH);
//							}
//	
//							@Override
//							public void onFail(int arg0, String arg1) {
//								// TODO Auto-generated method stub
//								handler.sendEmptyMessage(MSG_UPLOADIMG_FINISH);
//							}
//	
//							@Override
//							public void onSuccess(Object arg0) {
//								// TODO Auto-generated method stub
//								Message msg = Message.obtain();
//								msg.what = MSG_UPLOADIMG_FINISH;
//								msg.obj = arg0;
//								handler.sendMessage(msg);
//							}
//							
//						});
						return;
					}
				}
			}
		}
		handler.sendEmptyMessage(MSG_UPLOADIMG_FINISH);
	}
	
	private void share2QZone(final Ad ad){
		
		String imgUrl = super.getThumbnailUrl(mAd);
		String imgPath = (imgUrl == null || imgUrl.length() == 0) ? "" : ImageCacheManager.getInstance().getFileInDiskCache(imgUrl);

		Bundle bundle = new Bundle();
		bundle.putString(WeiboSharingFragment.EXTRA_WEIBO_CONTENT,
				"我用百姓网App 发布了\"" + mAd.getValueByKey("title") + "\"" + "麻烦朋友们帮忙转发一下～ ");
		bundle.putString(WeiboSharingFragment.EXTRA_PIC_URI,
				(imgPath == null || imgPath.length() == 0) ? "" : imgPath);
		bundle.putString(BaseSharingFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
		bundle.putString(BaseSharingFragment.EXTRA_EXPIRES_IN, mExpires_in);
		bundle.putString(QZoneSharingFragment.EXTRA_OPEN_ID, mOpenId);
		bundle.putString(QZoneSharingFragment.EXTRA_LINK, mAd.getValueByKey(EDATAKEYS.EDATAKEYS_LINK));
		bundle.putString(QZoneSharingFragment.EXTRA_TITLE, mAd.getValueByKey("title"));
		bundle.putString(QZoneSharingFragment.EXTRA_SUMMARY, mAd.getValueByKey(EDATAKEYS.EDATAKEYS_DESCRIPTION));
		((BaseActivity)mActivity).pushFragment(new QZoneSharingFragment(), bundle, false);
		


//		TencentOpenAPI2.sendStore(mActivity, mAccessToken, mAppid, mOpenId, "_self", bundle, new Callback() {
//			@Override
//			public void onSuccess(final Object obj) {
//				SharingCenter.trackShareResult("qzone", true, null);
//				Context ctx = GlobalDataManager.getInstance().getApplicationContext();
//				if(ctx != null){
//					Intent intent = new Intent(CommonIntentAction.ACTION_BROADCAST_SHARE_SUCCEED);
//					intent.putExtra(CommonIntentAction.EXTRA_MSG_SHARED_AD_ID, ad.getValueByKey(EDATAKEYS.EDATAKEYS_ID));
//					ctx.sendBroadcast(intent);
//				}
//				mActivity.runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						Toast toast = Toast.makeText(mActivity.getApplicationContext(), "成功分享到QQ空间！", Toast.LENGTH_SHORT);
//						toast.show();
//					}
//				});
//			}
//			   		
//			@Override
//			public void onFail(final int ret, final String msg) {
//				SharingCenter.trackShareResult("qzone", false, "code:" + ret + " msg:" + msg);
//				mActivity.runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						Toast toast = Toast.makeText(mActivity.getApplicationContext(), "分享失败，错误信息:" + ret + ", " + msg, Toast.LENGTH_SHORT);
//						toast.show();
//					}
//				});
//			}
//			   
//			@Override
//			public void onCancel(int flag) {
//				SharingCenter.trackShareResult("qzone", false, "cancel_flag:" + flag);
//			}
//		}, null);
//		Context ctx = GlobalDataManager.getInstance().getApplicationContext();
//		if(ctx != null){
//			Intent intent = new Intent(CommonIntentAction.ACTION_BROADCAST_SHARE_SUCCEED);
//			intent.putExtra(CommonIntentAction.EXTRA_MSG_SHARED_AD_ID, ad.getValueByKey(EDATAKEYS.EDATAKEYS_ID));
//			ctx.sendBroadcast(intent);
//		}

	}
	
	@Override
	public void auth() {
		mTencent.login(mActivity, "get_user_info,get_user_profile,add_share,add_topic,list_album,upload_pic,add_album", this);
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

	@Override
	public void share(Ad ad) {
		// TODO Auto-generated method stub
		mAd = ad;
		if(mAccessToken == null || mOpenId == null || mExpires_in == null){
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
//		if (receiver != null) {
//			unregisterIntentReceivers();
//		}		
	}

	@Override
	public void onCancel() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onComplete(JSONObject arg0) {
		// TODO Auto-generated method stub
		if(arg0 == null) return;
		try{
			mAccessToken = arg0.getString("access_token");
			mExpires_in = arg0.getString("expires_in");
			mOpenId = arg0.getString("openid");
			Util.saveDataToLocate(mActivity, STRING_ACCESS_TOKEN, mAccessToken);
			Util.saveDataToLocate(mActivity, STRING_OPENID, mOpenId);
			Util.saveDataToLocate(mActivity, STRING_EXPIRES_IN, mExpires_in);
			handler.sendEmptyMessage(MSG_AUTO_SUCCEED);
			mActivity.sendBroadcast(new Intent(CommonIntentAction.ACTION_BROADCAST_QZONE_AUTH_SUCCESS));
		}catch(JSONException e){
			
		}
	}

	@Override
	public void onError(UiError arg0) {
		// TODO Auto-generated method stub
		
	}
}