//xumengyi@baixing.com
package com.baixing.util.post;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Pair;

import com.baixing.anonymous.AccountService;
import com.baixing.anonymous.AnonymousExecuter;
import com.baixing.anonymous.BaseAnonymousLogic;
import com.baixing.anonymous.AnonymousNetworkListener;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.BXLocation;
import com.baixing.entity.PostGoodsBean;
import com.baixing.entity.UserBean;
import com.baixing.entity.UserProfile;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
import com.baixing.network.api.ApiError;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.network.api.BaseApiCommand.Callback;
import com.baixing.util.ErrorHandler;
import com.baixing.util.Util;
import com.baixing.widget.VerifyFailDialog;
import com.quanleimu.activity.R;
import com.tencent.mm.algorithm.Base64;

public class PostNetworkService implements Callback, AnonymousNetworkListener{
	private Handler handler;
	private String city;
	private String category;
	private String mobile;
	private PostParams postParams;
	

	class PostParams{
		public String apiName;
		public ApiParams params;
	}
	
	public static class PostResultData extends Object{
		public String id;
		public int error;
		public String message;
		public boolean isRegisteredUser;
	}
	
	private static PostNetworkService instance;
	public static PostNetworkService getInstance(){
		if(instance == null){
			instance = new PostNetworkService();
		}
		return instance;
	}
	
	public void setHandler(Handler handler){
		this.handler = handler;
	}
	
	private PostNetworkService(){		
	}
//	public PostNetworkService(Handler handler){
//		this.handler = handler;
//	}
 
	public void retreiveMetaAsync(String cityEnglishName, String categoryEnglishName){
		String apiName = "category_meta_post";
		city = cityEnglishName;
		category = categoryEnglishName;
		ApiParams param = new ApiParams();
		param.addParam("categoryEnglishName", categoryEnglishName);
		param.addParam("cityEnglishName", (cityEnglishName == null ? GlobalDataManager.getInstance().getCityEnglishName() : cityEnglishName));
//		ApiClient.getInstance().remoteCall(Api.createPost(apiName), param, this);
		BaseApiCommand.createCommand(apiName, false, param).execute(GlobalDataManager.getInstance().getApplicationContext(), this);
	}
	
	public void doRegisterAndVerify(String mobile){		
		UserBean curUser = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
		if(curUser != null && curUser.getPhone() != null){
			String curUserStatus = AnonymousExecuter.retreiveAccountStatusSync(curUser.getPhone());
			if(curUserStatus != null && curUserStatus.equals(BaseAnonymousLogic.Status_Registered_Verified)){
				this.doPost();
			}else{
				AccountService.getInstance().initStatus(curUser.getPhone());
				AccountService.getInstance().setActionListener(this);
				AccountService.getInstance().start(BaseAnonymousLogic.Status_Registered_UnVerified);
//				TODO start verify
			}
		}else{
			String checkMobile = mobile;
			String deviceNumber = Util.getDevicePhoneNumber();
			if(deviceNumber != null && deviceNumber.length() == 11){
				checkMobile = deviceNumber;
			}
			String status = AnonymousExecuter.retreiveAccountStatusSync(checkMobile);
			if(status != null){
				if(status.equals(BaseAnonymousLogic.Status_UnRegistered)){
					sendMessage(PostCommonValues.MSG_POST_NEED_REGISTER, checkMobile);
				}else if(status.equals(BaseAnonymousLogic.Status_Registered_Verified)
						|| status.equals(BaseAnonymousLogic.Status_Registered_UnVerified)){
					sendMessage(PostCommonValues.MSG_POST_NEED_LOGIN, checkMobile);
				}else{
					sendMessage(PostCommonValues.MSG_ACCOUNT_CHECK_FAIL, status);
				}
			}else{
				sendMessage(PostCommonValues.MSG_ACCOUNT_CHECK_FAIL, status);	
			}
		}
	}	
	
	private void doPost(){
		if(postParams == null) return;
		UserBean user = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
//		if(user == null || user.getPhone() == null || !user.getPhone().equals(mobile)){
//			doLoginAfterPostSucceedSync();
//		}
		user = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
		if(user != null){
			postParams.params.removeParam("mobile");
			postParams.params.removeParam("userToken");
			postParams.params.appendAuthInfo(user.getPhone(), user.getPassword());
		}
		BaseApiCommand.createCommand(postParams.apiName, false, postParams.params).execute(GlobalDataManager.getInstance().getApplicationContext(), this);
	}
	
	public void savePostData(HashMap<String, String> params,
			HashMap<String, String> mustParams,
			LinkedHashMap<String, PostGoodsBean> beans,
			List<String> bmpUrls,
			BXLocation address,
			boolean editMode){
		if(address != null){
			params.put(PostCommonValues.STRING_AREA, getDistrictByLocation(address, beans));
		}

		String apiName = editMode ? "ad_update" : "ad_add";
		UserBean user = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
		boolean registered = (user != null && user.getPhone() != null && !user.getPhone().equals(""));
		ApiParams apiParam = new ApiParams();
		if(mustParams != null){
			apiParam.addAll(mustParams);
		}

		if(registered){
			apiParam.appendAuthInfo(user.getPhone(), user.getPassword());//.appendUserInfo(user);
		}
		if(address != null){
			apiParam.addParam("lat", String.valueOf(address.fGeoCodedLat));
			apiParam.addParam("lng", String.valueOf(address.fGeoCodedLon));
		}

		Set<String> keys = params.keySet();
		if(keys != null){
			Iterator<String> ite = keys.iterator();
			while(ite.hasNext()){
				String key = ite.next();
				String value = params.get(key);
				if (value != null && value.length() > 0 && beans.get(key) != null) {
//					try{
//						apiParam.addParam(URLEncoder.encode(beans.get(key).getName(), "UTF-8"),
//								URLEncoder.encode(value, "UTF-8").replaceAll("%7E", "~"));//ugly, replace, what's that? 
//						if(beans.get(key).getName().equals(PostCommonValues.STRING_DESCRIPTION)){//generate title from description
//							apiParam.addParam("title", 
//									URLEncoder.encode(value.substring(0, Math.min(25, value.length())), "UTF-8").replaceAll("%7E", "~"));

						apiParam.addParam(beans.get(key).getName(), value); 
						if(beans.get(key).getName().equals(PostCommonValues.STRING_DESCRIPTION)){//generate title from description
							apiParam.addParam("title", value.substring(0, Math.min(25, value.length())));
						
						}
//					}catch(UnsupportedEncodingException e){
//						e.printStackTrace();
//					}
				}
			}
		}
		
		String images = "";
		if(bmpUrls != null){
			for (int i = 0; i < bmpUrls.size(); i++) {				
				if(bmpUrls.get(i) != null && bmpUrls.get(i).contains("http:")){
					images += "," + bmpUrls.get(i);
				}
			}
			if(images != null && images.length() > 0 && images.charAt(0) == ','){
				images = images.substring(1);
			}
			if(images != null && images.length() > 0){
				apiParam.addParam("images", images);
			}
		}
		
		postParams = new PostParams();
		postParams.apiName = apiName;
		postParams.params = new ApiParams();
		postParams.params.addAll(apiParam.getParams());		
	}
	
	private void sendMessage(int what, Object obj){
		if(handler != null){
			Message msg = Message.obtain();
			msg.what = what;
			msg.obj = obj;
			handler.sendMessage(msg);
		}
	}
	
	private String getDistrictByLocation(BXLocation location, LinkedHashMap<String, PostGoodsBean> postList){
		if(location == null || location.subCityName == null) return null;
		if(postList != null && postList.size() > 0){
			Object[] postListKeySetArray = postList.keySet().toArray();
			for(int i = 0; i < postList.size(); ++ i){
				PostGoodsBean bean = postList.get(postListKeySetArray[i]);
				if(bean.getName().equals(PostCommonValues.STRING_AREA)){
					if(bean.getLabels() != null){
						for(int t = 0; t < bean.getLabels().size(); ++ t){
							if(location.subCityName.contains(bean.getLabels().get(t))){
								return bean.getValues().get(t);
							}
						}
					}						
				}
			}
		}
		return null;
	}
	
	private void handlePostMsgBack(String rawData){
		if (rawData != null) {
			PostResultData data = this.parseResult(rawData);
			if(data != null && data.error == 0){									
				sendMessage(PostCommonValues.MSG_POST_SUCCEED, data);
			}else if (data != null) {
				sendMessage(PostCommonValues.MSG_POST_FAIL, data);
			} else {
				sendMessage(PostCommonValues.MSG_POST_FAIL, "发布失败");
			}
		}
	}
	
	private PostResultData parseResult(String response) {
		if (response != null) {
			try{
				JSONObject jsonObject = new JSONObject(response);
				JSONObject errorJson = jsonObject.getJSONObject("error");
				int code = errorJson.getInt("code");
				String message = errorJson.getString("message");
				PostResultData data = new PostResultData();
				data.error = code;
				data.message = message;
				data.id = jsonObject.getString("id");
				data.isRegisteredUser = jsonObject.getBoolean("contactIsRegisteredUser");
				
				return data;
			}catch(JSONException e){
				//
			}
		}
		
		return null;
	}
	
	private void handleGetMetaMsgBack(String rawData){
		if(rawData != null){
			LinkedHashMap<String, PostGoodsBean> pl = JsonUtil.getPostGoodsBean(rawData);
			if(pl == null || pl.size() == 0){
				sendMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE, null);
				return;
			}
			Util.saveJsonAndTimestampToLocate(GlobalDataManager.getInstance().getApplicationContext(), 
					category + city, rawData, System.currentTimeMillis()/1000);
			sendMessage(PostCommonValues.MSG_GET_META_SUCCEED, pl);
		} else {
			sendMessage(PostCommonValues.MSG_GET_META_FAIL, null);
		}
	}

	@Override
	public void onNetworkDone(String apiName, String responseData) {
		if(apiName.equals("category_meta_post")){
			handleGetMetaMsgBack(responseData);
		}else{
			handlePostMsgBack(responseData);
		}
	}

	@Override
	public void onNetworkFail(String apiName, ApiError error) {
		PostResultData data = null;
		if(error != null){
			data = parseResult(error.getServerResponse());
			if (data == null) {
				data = new PostResultData();
				if(error.getErrorCode() != null){
					data.error = Integer.valueOf(error.getErrorCode());
				}
				data.message = error.getMsg() == null ? "网络错误" : error.getMsg();
			}
		}

		int msgCode = apiName.equals("category_meta_post") ? PostCommonValues.MSG_GET_META_FAIL : PostCommonValues.MSG_POST_FAIL;
		sendMessage(msgCode, error == null ? "网络错误" : data);
	}
	
	private String verifyCode;
	public void onOutActionDone(int action, String data){
		if(action == PostCommonValues.ACTION_POST_NEED_LOGIN_DONE){
			sendMessage(PostCommonValues.MSG_CHECK_QUOTA_AFTER_LOGIN, null);
//			doPost();
		}else if(action == PostCommonValues.ACTION_POST_NEED_REVERIIFY){
			verifyCode = data;
			AccountService.getInstance().setActionListener(this);
			AccountService.getInstance().start(BaseAnonymousLogic.Status_Registered_UnVerified);
		}else if(action == PostCommonValues.ACTION_POST_CHECK_QUOTA_OK){
			doPost();
		}
	}

	@Override
	public void onActionDone(String action, ResponseData response) {
		// TODO Auto-generated method stub
		if(action.equals(AccountService.Action_Done)){
			doPost();
		}else{
			if(!response.success){
				if(action.equals(BaseAnonymousLogic.Action_Verify)){
					sendMessage(PostCommonValues.MSG_VERIFY_FAIL, null);
				}else{
					sendMessage(PostCommonValues.MSG_POST_EXCEPTION, response.message);
				}
			}
		}				
	}

	@Override
	public void beforeActionDone(String action, ApiParams outParams) {
		// TODO Auto-generated method stub
		if(action.equals(BaseAnonymousLogic.Action_Verify) && verifyCode != null){
			outParams.addParam("verifyCode", verifyCode);
			verifyCode = null;
		}		
	}
}