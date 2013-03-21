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

import com.baixing.anonymous.AnonymousExecuter;
import com.baixing.anonymous.AnonymousLogic;
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
import com.tencent.mm.algorithm.Base64;

public class PostNetworkService implements Callback, AnonymousNetworkListener{
	private Handler handler;
	private String city;
	private String category;
	private AnonymousLogic anonyLogic;
	private AnonymousExecuter anonyExecuter;
	private Pair<String, String> nextActionAndStatus;
	private String currentStatus;
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
	
	private void initExecuter(){
		if(anonyExecuter == null){
			anonyExecuter = new AnonymousExecuter();
			anonyExecuter.setCallback(this);
		}		
	}
	
	public String getAccountStatus(String mobile){
		initExecuter();
		return anonyExecuter.retreiveAccountStatusSync(mobile);
	}
	
	private boolean isUserLoginned(){
		UserBean user = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
		if(user != null && user.getPhone() != null && user.getPhone().equals(mobile)){
			return true;
		}
		return false;
	}
	
	public void doRegisterAndVerify(final String mobile){
		initExecuter();
		this.mobile = mobile;
		String accountStatus = getAccountStatus(mobile);
		String deviceNumber = Util.getDevicePhoneNumber();
		String phoneStatus = (deviceNumber != null && deviceNumber.contains(mobile)) ? 
				AnonymousLogic.Status_Number_Available : AnonymousLogic.Status_Number_UnAvailable;
		anonyLogic = new AnonymousLogic(accountStatus, phoneStatus);
		nextActionAndStatus = anonyLogic.getActionAndNextStatus();
		currentStatus = anonyLogic.getCurrentStatus();
		if(phoneStatus.equals(AnonymousLogic.Status_Number_Available)){
			this.mobile = deviceNumber;
		}
		if(nextActionAndStatus != null){
			if(nextActionAndStatus.first.equals(AnonymousLogic.Action_Post)){
				doPost();
			}else if(nextActionAndStatus.first.equals(AnonymousLogic.Action_Login)){
				if(isUserLoginned()){
					ResponseData response = new ResponseData();
					response.success = true;
					response.message = "登陆成功";
					this.onActionDone(AnonymousLogic.Action_Login, response);
				}else{
					sendMessage(PostCommonValues.MSG_POST_NEED_LOGIN, null);
				}
			}else{
				anonyExecuter.executeAction(nextActionAndStatus.first, this.mobile);
			}
		}
	}	
	
	private void doPost(){
		if(postParams == null) return;
		UserBean user = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
		if(user == null || user.getPhone() == null || !user.getPhone().equals(mobile)){
			doLoginAfterPostSucceedSync();
		}
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
	
    static private byte[] decript(byte[] encryptedData, byte[] key)
            throws NoSuchAlgorithmException, NoSuchPaddingException,
            InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
	    Cipher c = Cipher.getInstance("AES/ECB/ZeroBytePadding");
	    SecretKeySpec k = new SecretKeySpec(key, "AES");
	    c.init(Cipher.DECRYPT_MODE, k);
	    return c.doFinal(encryptedData);
    }
    
//	secret "c6dd9d408c0bcbeda381d42955e08a3f" android
//	secret "f93bfd64405a641a7c8447fc50e55d6e" ios

    static private String getDecryptedPassword(String encryptedPwd){
		try{
			String key = "c6dd9d408c0bcbeda381d42955e08a3f";
			key = key.substring(0, 16);
			byte[] pwd = decript(Base64.decode(encryptedPwd), key.getBytes("utf-8"));
			String str = new String(pwd);
			return str;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
    }
	
	private void doLoginAfterPostSucceedSync(){
		if(mobile == null || mobile.length() == 0) return;
		UserBean bean = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
		if(bean != null && bean.getPhone() != null && bean.getPhone().equals(mobile)){
			return;
		}
		ApiParams param = new ApiParams();
		param.addParam("mobile", this.mobile);
//		ApiClient.getInstance().remoteCall(Api.createPost(apiName), param, this);
		String json = BaseApiCommand.createCommand("getUser", false, param).executeSync(GlobalDataManager.getInstance().getApplicationContext());
		try{
			JSONObject obj = new JSONObject(json);
			if(obj != null){
				JSONObject error = obj.getJSONObject("error");
				if(error != null){
					String code = error.getString("code");
					if(code != null && code.equals("0")){
						String password = obj.getString("password");
						String nickname = obj.getString("nickname");
						String id = obj.getString("id");
						UserBean loginBean = new UserBean();
						loginBean.setId(id);
						loginBean.setPhone(mobile);
						String decPwd = getDecryptedPassword(password);
						loginBean.setPassword(decPwd, false);
						Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "user", loginBean);
						UserProfile profile = new UserProfile();
						profile.mobile = mobile;
						profile.nickName = nickname;
						profile.userId = id;
						Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "userProfile", profile);
						BxMessageCenter.defaultMessageCenter().postNotification(IBxNotificationNames.NOTIFICATION_LOGIN, loginBean);
					}
				}
			}
		}catch(JSONException e){
			e.printStackTrace();
		}

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
	
	public void onOutActionDone(int action, String data){
		if(action == PostCommonValues.ACTION_POST_NEED_LOGIN_DONE){
			UserBean ub = GlobalDataManager.getInstance().getAccountManager().getCurrentUser();
			ResponseData response = new ResponseData();
			if(ub != null && ub.getPhone() != null && ub.getPhone().equals(this.mobile)){
				response.success = true;
				onActionDone(AnonymousLogic.Action_Login, response);
			}else{
				response.success = false;
			}				
		}else if(action == PostCommonValues.ACTION_POSt_NEED_REVERIIFY){
			if(data != null && data.length() > 0){
				mobile = data;
			}
			this.doRegisterAndVerify(mobile);
		}
	}

	@Override
	public void onActionDone(String action, ResponseData response) {
		// TODO Auto-generated method stub
		if(response.success){
			if(nextActionAndStatus != null && nextActionAndStatus.second != null){
				currentStatus = nextActionAndStatus.second;
				anonyLogic.setCurrentStatus(currentStatus);
				nextActionAndStatus = this.anonyLogic.getActionAndNextStatus();
				if(nextActionAndStatus != null){
					if(nextActionAndStatus.first.equals(AnonymousLogic.Action_Post)){
						doPost();
					}else{
						this.anonyExecuter.executeAction(nextActionAndStatus.first, mobile);
					}
				}
			}
		}else{
			sendMessage(PostCommonValues.MSG_VERIFY_FAIL, mobile);
			anonyLogic.setCurrentStatus(AnonymousLogic.Status_Initialization);
		}
	}
}