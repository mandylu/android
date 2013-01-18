//package com.baixing.sharing;
//
//import java.io.IOException;
//import java.io.Serializable;
//import java.net.MalformedURLException;
//import java.net.URL;
//import java.net.URLDecoder;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.os.Bundle;
//import android.util.Log;
//
//import com.baixing.entity.Ad;
//import com.baixing.entity.ImageList;
//import com.baixing.imageCache.ImageCacheManager;
//import com.baixing.util.Util;
//
//import com.weibo.net.AccessToken;
//import com.weibo.net.DialogError;
//import com.weibo.net.Oauth2AccessTokenHeader;
//import com.weibo.net.Utility;
//import com.weibo.net.Weibo;
//import com.weibo.net.WeiboDialogListener;
//import com.weibo.net.WeiboException;
//import com.weibo.net.WeiboParameters;
//
//public class WeiboSharingManager implements BaseSharingManager{
//	static class WeiboAccessTokenWrapper  implements Serializable{
//		/**
//		 * 
//		 */
//		private static final long serialVersionUID = 973987291134876738L;
//		private String token;
//		private String expires_in;
////		public WeiboAccessTokenWrapper(String token, String expires){
////			this.token = token;
////			this.expires_in = expires;
////		}
//		public String getToken() {
//			return token;
//		}
//		public void setToken(String token) {
//			this.token = token;
//		}
//		public String getExpires_in() {
//			return expires_in;
//		}
//		public void setExpires_in(String expires_in) {
//			this.expires_in = expires_in;
//		}
//		
//	}
//	private Ad mAd;
//	private Activity mActivity;
//	private static final String kWBBaixingAppKey = "3747392969";
//	private static final String kWBBaixingAppSecret = "ff394d0df1cfc41c7d89ce934b5aa8fc";
//	private static final String STRING_WEIBO_ACCESS_TOKEN = "weiboaccesstoken";
//	private Weibo mWeibo;
//	private WeiboAccessTokenWrapper mToken;
//	
//	private WeiboAccessTokenWrapper loadToken(){
//		return (WeiboAccessTokenWrapper)Util.loadDataFromLocate(mActivity, STRING_WEIBO_ACCESS_TOKEN, WeiboAccessTokenWrapper.class);
//	}
//	
//	private void saveToken(WeiboAccessTokenWrapper token){
//		Util.saveDataToLocate(mActivity, STRING_WEIBO_ACCESS_TOKEN, token);
//	}
//	
//	public WeiboSharingManager(Activity activity){
//		mActivity = activity;
//		mToken = loadToken();
//	}
//	
//	@Override
//	public void share(Ad ad) {
//		mAd = ad;
//		if(mToken != null && mToken.getExpires_in() != null && mToken.getExpires_in().length() > 0
//				&& mToken.getToken() != null && mToken.getToken().length() > 0){
//			doShare2Weibo(new AccessToken(mToken.getToken(), mToken.getExpires_in()));
//			return;
//		}
//		Weibo weibo = Weibo.getInstance();
//		weibo.setupConsumerConfig(kWBBaixingAppKey, kWBBaixingAppSecret);
//		weibo.setRedirectUrl("http://www.baixing.com");
//
//		WeiboParameters parameters=new WeiboParameters();        
//		parameters.add("forcelogin", "true");        
//		Utility.setAuthorization(new Oauth2AccessTokenHeader());          
//		weibo.dialog(mActivity, parameters, new WeiboDialogListener(){
//			@Override
//			public void onCancel() {
//				// TODO Auto-generated method stub
//			}
//
//			@Override
//			public void onComplete(Bundle arg0) {
//				// TODO Auto-generated method stub
//				String token = arg0.getString("access_token");
//				String expires_in = arg0.getString("expires_in");
//				AccessToken accessToken = new AccessToken(token, kWBBaixingAppSecret);
//				accessToken.setExpiresIn(expires_in);			
//				Weibo.getInstance().setAccessToken(accessToken);
//				WeiboAccessTokenWrapper wtw = new WeiboAccessTokenWrapper();
//				wtw.setToken(token);
//				wtw.setExpires_in(expires_in);
//				saveToken(wtw);
//
//				doShare2Weibo(accessToken);
//			}
//
//			@Override
//			public void onError(DialogError arg0) {
//				// TODO Auto-generated method stub
//				
//			}
//
//			@Override
//			public void onWeiboException(WeiboException arg0) {
//				// TODO Auto-generated method stub
//				
//			}
//        	  
//          });	 
//	}
//
//	@Override
//	public void release() {
//		// TODO Auto-generated method stub
//		
//	}
//	
//	private void doShare2Weibo(AccessToken accessToken){
//		ImageList il = mAd.getImageList();
//		String big = null;
//		if(il != null){
//			big = il.getBig();
//			if(big != null){
//				big = big.split(",")[0];
//			}
//		}
//	    
////        Intent i = new Intent(mActivity, WeiboSharingActivity.class);
////        i.putExtra(WeiboSharingActivity.EXTRA_WEIBO_CONTENT, 
////        		"我在#百姓网#发布" + mAd.getValueByKey("title") + ",求扩散！" + mAd.getValueByKey("link"));
////        i.putExtra(WeiboSharingActivity.EXTRA_PIC_URI, 
////        		(big == null || big.length() == 0) ? "" : ImageCacheManager.getInstance().getFileInDiskCache(big));
////        i.putExtra(WeiboSharingActivity.EXTRA_ACCESS_TOKEN, accessToken.getToken());
////        i.putExtra(WeiboSharingActivity.EXTRA_EXPIRES_IN, String.valueOf(accessToken.getExpiresIn()));
////        mActivity.startActivity(i);
//        
//		try{ 
//			Weibo.getInstance().share2weibo(mActivity,
//					accessToken.getToken(),
//					accessToken.getSecret(), 
//					"我在#百姓网#发布" + mAd.getValueByKey("title") + ",求扩散！" + mAd.getValueByKey("link"),
//					(big == null || big.length() == 0) ? "" : ImageCacheManager.getInstance().getFileInDiskCache(big));
//		}
//		catch(WeiboException e){
//			e.printStackTrace();
//		}        
//        
//        
//        
//        
//        
//        
//        
//        
//        
//	}
//}