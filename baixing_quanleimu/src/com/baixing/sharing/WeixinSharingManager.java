package com.baixing.sharing;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.graphics.Bitmap;

import com.baixing.entity.Ad;
import com.baixing.entity.Ad.EDATAKEYS;
import com.baixing.entity.ImageList;
import com.baixing.imageCache.ImageCacheManager;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.SendMessageToWX;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.tencent.mm.sdk.openapi.WXAppExtendObject;
import com.tencent.mm.sdk.openapi.WXMediaMessage;
import com.tencent.mm.sdk.openapi.WXTextObject;
import com.tencent.mm.sdk.openapi.WXWebpageObject;

class WeixinSharingManager extends BaseSharingManager{
	static private final String WX_APP_ID = "wx862b30c868401dbc";
//	static private final String WX_APP_ID = "wx47a12013685c6d3b";//debug
	private Activity mActivity;
	private IWXAPI mApi;
	public WeixinSharingManager(Activity activity){
		mActivity = activity;
		mApi = WXAPIFactory.createWXAPI(mActivity, WX_APP_ID, false);
		mApi.registerApp(WX_APP_ID);
	}

	@Override
	public void share(Ad ad) {
		// TODO Auto-generated method stub
//		String detailJson = convert2JSONString(ad);
		String title = "我在转让：" + ad.getValueByKey("title") + ",请各位朋友们帮忙转发下哦~";
		
		String imgUrl = super.getThumbnailUrl(ad);

		WXMediaMessage obj = new WXMediaMessage();
		
		String description = "";
		if(ad.getMetaData() != null){
			for(int i = 0; i < ad.getMetaData().size(); ++ i){
				String meta = ad.getMetaData().get(i);
				String [] ms = meta.split(" ");
				if(ms != null && ms.length == 2){
					description += "，" + ms[1];
				}
			}
		}
		if(description.charAt(0) == '，'){
			description = description.substring(1);
		}
		obj.description = description;
		obj.title = title;

//		if(mApi.getWXAppSupportAPI() >= 0x21020001){
			WXWebpageObject webObj = new WXWebpageObject();
			String link = ad.getValueByKey("link");
			link = link.replace(".baixing.com/", ".baixing.com/m/");
			webObj.webpageUrl = link;
			obj.mediaObject = webObj;
//		}else{
//			WXAppExtendObject appObj = new WXAppExtendObject();
////			appObj.fileData = detailJson.getBytes();
//			appObj.fileData = ad.getValueByKey(EDATAKEYS.EDATAKEYS_ID).getBytes();
//			obj.mediaObject = appObj;
//		}
		
		if(imgUrl != null){
			WeakReference<Bitmap> thumbnail = ImageCacheManager.getInstance().getFromCache(imgUrl);
			if(thumbnail != null && thumbnail.get() != null){
				obj.setThumbImage(thumbnail.get());
			}
		}
		sendWXRequest(obj);
	}
	
	private void sendWXRequest(WXMediaMessage msg){
		SendMessageToWX.Req req = new SendMessageToWX.Req();
		req.transaction = String.valueOf(System.currentTimeMillis());
		req.message = msg;
		if(mApi.getWXAppSupportAPI() >= 0x21020001){
			req.scene = SendMessageToWX.Req.WXSceneTimeline;
		}
		mApi.sendReq(req);
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
	}
	
	private String convert2JSONString(Ad detail){
		JSONObject obj = new JSONObject();
		Set<String> keys = detail.getKeys();
		Object[] keyAry = keys.toArray();
		JSONArray jsonAry = new JSONArray();
		JSONObject subObj = new JSONObject();
		try{
			for(int i = 0; i < keyAry.length; ++ i){				
				String key = (String)keyAry[i];
				String value = detail.getValueByKey((String)keyAry[i]); 
				if(value == null) value = "";
				subObj.put(key, value);
			}			
			
			if(detail.getImageList() != null){
				JSONObject jsonImgs = new JSONObject();
				if(detail.getImageList().getBig() != null && !detail.getImageList().getBig().equals("")){
					JSONArray imgAry = new JSONArray();
					String big = detail.getImageList().getBig();
					String[] bigs = big.split(",");
					for(int m = 0; m < bigs.length; ++ m){
						imgAry.put(bigs[m]);//.substring(1, bigs[m].length() - 1));
					}
//					jsonImgs.put("big", detail.getImageList().getBig());
					jsonImgs.put("big", imgAry);
				}
				if(detail.getImageList().getResize180() != null && !detail.getImageList().getResize180().equals("")){
//					jsonImgs.put("resize180", detail.getImageList().getResize180());
					JSONArray imgAry = new JSONArray();
					String resize = detail.getImageList().getResize180();
					String[] resizes = resize.split(",");
					for(int m = 0; m < resizes.length; ++ m){
						imgAry.put(resizes[m]);//.substring(1, resizes[m].length() - 1));
					}
					jsonImgs.put("resize180", imgAry);
					
				}
				subObj.put("images", jsonImgs);
			}
			
			if(detail.getMetaData() != null && detail.getMetaData().size() > 0){
				JSONArray jsonMetaAry = new JSONArray();
				for(int t = 0; t < detail.getMetaData().size(); ++ t){
					jsonMetaAry.put(detail.getMetaData().get(t));
				}
				subObj.put("metaData", jsonMetaAry);
			}
			jsonAry.put(subObj);
			obj.put("data", jsonAry);
			obj.put("count", 1);
		} catch(JSONException e){
			e.printStackTrace();
		}
		
//		GoodsList gl = JsonUtil.getGoodsListFromJson(obj.toString());
		return obj.toString();
	}
	
	@Override
	public void auth(){
		
	}
	
}