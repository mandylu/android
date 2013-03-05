package com.baixing.sharing;

import com.baixing.broadcast.CommonIntentAction;
import com.baixing.sharing.WeiboSSOSharingManager.WeiboAccessTokenWrapper;
import com.baixing.sharing.weibo.Oauth2AccessToken;
import com.baixing.sharing.weibo.Weibo;
import com.baixing.sharing.weibo.WeiboAuthListener;
import com.baixing.sharing.weibo.WeiboDialogError;
import com.baixing.sharing.weibo.WeiboException;
import com.baixing.sharing.weibo.WeiboParameters;
import com.baixing.util.ViewUtil;
//import com.weibo.sdk.android.sso.SsoHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.baixing.sharing.weibo.*;

public class WeiboManagerActivity extends Activity{
	private SsoHandler mSsoHandler = null;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		this.getWindow().setBackgroundDrawable(null);
		super.onCreate(savedInstanceState);
		
	}
		
	public void doAuthSSO(){
		CookieSyncManager.createInstance(this); 
	    CookieManager cookieManager = CookieManager.getInstance();
	    cookieManager.removeAllCookie();
		mSsoHandler = new SsoHandler(this, Weibo.getInstance(WeiboSSOSharingManager.kWBBaixingAppKey, "http://www.baixing.com"));
        mSsoHandler.authorize(new WeiboAuthListener(){

			@Override
			public void onCancel() {
				// TODO Auto-generated method stub
				finish();
			}

			@Override
			public void onComplete(Bundle values) {
				// TODO Auto-generated method stub
	            String token = values.getString("access_token");  
	            String expires_in = values.getString("expires_in");  
	            Oauth2AccessToken accessToken = new Oauth2AccessToken(token, expires_in);  
	            if (accessToken.isSessionValid()) {  
					WeiboAccessTokenWrapper wtw = new WeiboAccessTokenWrapper();
					wtw.setToken(token);
					wtw.setExpires_in(expires_in);
					WeiboSSOSharingManager.saveToken(WeiboManagerActivity.this, wtw);
					WeiboManagerActivity.this.sendBroadcast(new Intent(CommonIntentAction.ACTION_BROADCAST_WEIBO_AUTH_DONE));
	            }
	            finish();
			}

			@Override
			public void onError(WeiboDialogError arg0) {
				// TODO Auto-generated method stub
				ViewUtil.showToast(WeiboManagerActivity.this, arg0.getMessage(), true);
			}

			@Override
			public void onWeiboException(WeiboException arg0) {
				// TODO Auto-generated method stub
				ViewUtil.showToast(WeiboManagerActivity.this, arg0.getMessage(), true);
			}
        	
        });
	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(mSsoHandler == null){
			doAuthSSO();
		}
	}
	
	@Override
	public void onPause(){
		super.onPause();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data){
		super.onActivityResult(requestCode, resultCode, data);
		
		if(mSsoHandler != null){
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
		}		
	}
}