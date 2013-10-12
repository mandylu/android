package com.baixing.sharing.referral;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.baixing.data.GlobalDataManager;
import com.baixing.entity.BXLocation;
import com.baixing.entity.UserBean;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.util.Util;

public class ReferralNetwork extends Observable {

	private static final String TAG = ReferralNetwork.class.getSimpleName();

	public static final String PROMOTE_URL = "http://pages.baixing.com/mobile/dituishenqi/";
	
	private static ReferralNetwork instance = null;

	public static ReferralNetwork getInstance() {
		if (instance != null) {
			return instance;
		}
		instance = new ReferralNetwork();
		return instance;
	}
	
	public boolean savePromoHaibao(String businessMobile, String businessAddr, String imageUrls,
			String qrCodeID, BXLocation detailLocation) {
		Context context = GlobalDataManager.getInstance().getApplicationContext();
		UserBean promoter = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
		String promoterMobile = promoter.getPhone();
		String promoterUdid = Util.getDeviceUdid(context);
		String promoterUserId = promoter.getId();
		String gpsAddress = getDetailLocation(detailLocation);
		
		ApiParams params = new ApiParams();
		params.addParam("promoterMobile", promoterMobile);
		params.addParam("promoterUdid", promoterUdid);
		params.addParam("promoterUserId", promoterUserId);
		params.addParam("images", imageUrls);
		params.addParam("storeMobile", businessMobile);
		params.addParam("storeAddr", businessAddr);
		params.addParam("gpsAddr", gpsAddress);
		params.addParam("qrcodeId", qrCodeID);
		String jsonResponse = BaseApiCommand.createCommand("save_promo_haibao", true, params).executeSync(context);
		String posterId = getPosterId(jsonResponse);
		if (posterId != null) {
			HashMap<String, String> attrs = new HashMap<String, String>();
			attrs.put("haibaoId", posterId);
			if (ReferralNetwork.getInstance().savePromoLog(promoterMobile, ReferralUtil.TASK_HAIBAO, businessMobile, promoterUdid, promoterUserId, null, null, attrs)) {
				Toast.makeText(context, "海报推广成功！", Toast.LENGTH_SHORT).show();
				return true;
			} else {
				Toast.makeText(context, "推广记录保存失败", Toast.LENGTH_SHORT).show();
				return false;
			}
		}
		
		Toast.makeText(context, "发送失败，请检查网络后重试", Toast.LENGTH_SHORT).show();
		return false;
	}
	
	private String getPosterId(String jsonResult) {
		try {
			JSONObject obj = new JSONObject(jsonResult);
			if (obj != null) {
				JSONObject error = obj.getJSONObject("error");
				if (error != null) {
					String code = error.getString("code");
					if (code != null && code.equals("0")) {
						return obj.getString("id"); 
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
		return null;
	}
	
	private String getDetailLocation(BXLocation location) {
		if (location == null) {
			return "";
		}
		String latlon = null;
		try {
			latlon = "(" + location.fLat + "," + location.fLon + "); ";
		} catch (Exception e) {
			latlon = "";
		}
		String address = (location.detailAddress == null || location.detailAddress
				.equals("")) ? ((location.subCityName == null || location.subCityName
				.equals("")) ? "" : location.subCityName)
				: location.detailAddress;
		return (TextUtils.isEmpty(latlon) ? "" : latlon) + (TextUtils.isEmpty(address) ? "" : address);
	}
	
	public boolean savePromoLog(String promoterMobile, int taskType, String userMobile, String promoterUdid, String promoterUserId, String userUdid, String userUserId, Map<String, String> attrs) {
		ApiParams logParams = new ApiParams();
		logParams.addParam("parentMobile", promoterMobile);
		logParams.addParam("taskType", ReferralUtil.TASK_HAIBAO);
		logParams.addParam("childMobile", userMobile);
		
		if (null != promoterUdid) 	logParams.addParam("parentUdid", 	promoterUdid);
		if (null != promoterUserId) logParams.addParam("parentUserId",promoterUserId);
		if (null != userUdid) 		logParams.addParam("childUdid", 		userUdid);
		if (null != userUserId) 	logParams.addParam("childUserId", 	userUserId);
		if (null != attrs && attrs.size() != 0) {
			JSONObject json=new JSONObject();
			Iterator<java.util.Map.Entry<String, String>> iterator = attrs.entrySet().iterator();
			while (iterator.hasNext()) {
				java.util.Map.Entry<String, String> entry = iterator.next();			
				try {
					json.put(entry.getKey(), entry.getValue());
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			logParams.addParam("attr", json.toString());
			Log.d("savePoster",logParams.toString());
		}
		
		String logResponse = BaseApiCommand.createCommand("save_promo_log", true, logParams).executeSync(GlobalDataManager.getInstance().getApplicationContext());
		
		try {
			JSONObject obj = new JSONObject(logResponse);
			if (obj != null) {
				JSONObject error = obj.getJSONObject("error");
				Log.d("savePoster", error.toString());
				if (error != null) {
					String code = error.getString("code");
					if (code != null && code.equals("0")) {
						return true;
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}
}
