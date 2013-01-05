//liuweili@baixing.com
package com.baixing.android.api;

import java.security.MessageDigest;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.baixing.data.GlobalDataManager;
import com.baixing.entity.UserBean;
import com.baixing.message.BxMessageCenter;
import com.baixing.message.IBxNotificationNames;
import com.baixing.util.TextUtil;
import com.baixing.util.Util;

public class ApiClient {
	
	private enum METHOD_TYPE {
		HTTP_POST, HTTP_GET
	}
	
	/**
	 * 
	 * @author liuchong
	 *
	 */
	public static final class Api {
		private METHOD_TYPE type;
		private String apiName;
		public Api (METHOD_TYPE methodType, String apiName) {
			this.type = methodType;
			this.apiName = apiName;
		}
		
		public METHOD_TYPE getMethodType() {
			return type;
		}
		
		public String getApiName() {
			return apiName;
		}
		
		public static Api createPost(String apiName) {
			return new Api(METHOD_TYPE.HTTP_POST, apiName);
		}
		
		public static Api createGet(String apiName) {
			return new Api(METHOD_TYPE.HTTP_GET, apiName);
		}
	}
	
	private static final String LOG_TAG = "ApiClient";
	private static final String apiKey = "api_mobile_android";
	private static final String apiSecrect = "c6dd9d408c0bcbeda381d42955e08a3f";
	private static final String apiUrl = "http://www.baixing.com/api/mobile.";
	private static ApiClient _instance = null;
	private ApiParams commonParams = new ApiParams();
	private Context context = null;
	private int connectTimeout = 10000;// 10秒
	private int readTimeout = 30000;// 30秒
	private Vector pendingListeners = new Vector();
	private CacheProxy cache = null;
	
	protected ApiClient(){
		
	}
	//singleton
	public static ApiClient getInstance(){
		if(_instance == null){
			_instance = new ApiClient();
		}
		return _instance;
	}
	
	
	//init with mandatory parameters
	public void init(Context context, String udid, String version, String channel, String city, CacheProxy cache){
		this.context = context;
		commonParams.addParam(ApiParams.KEY_UDID, udid);
		commonParams.addParam(ApiParams.KEY_VERSION, version);
		commonParams.addParam(ApiParams.KEY_CHANNEL, channel);
		commonParams.addParam(ApiParams.KEY_CITY, city);
		commonParams.addParam(ApiParams.KEY_APIKEY, ApiClient.apiKey);
		
		this.cache = cache;
	}
	
	private boolean isMandatoryReady(){
		return (commonParams.getParam(ApiParams.KEY_UDID) != null 
				&& commonParams.getParam(ApiParams.KEY_VERSION) != null
				&& commonParams.getParam(ApiParams.KEY_CHANNEL) != null
				&& commonParams.getParam(ApiParams.KEY_CITY) != null
				&& commonParams.getParam(ApiParams.KEY_APIKEY) != null);
	}
	//accept parameters shared by multiple api calls
	public void addCommonParam(String key, String value){
		commonParams.addParam(key, value);
	}
	
	public void getCommonParam(String key){
		commonParams.getParam(key);
	}
	
	public void removeCommonParam(String key){
		commonParams.removeParam(key);
	}
	
	public void cancel(final ApiListener listener){
		unregisterListener(listener);
	}
	
	public void cancelAll(){
		pendingListeners.clear();
	}
	
	private void unregisterListener(final ApiListener listener){
		if(pendingListeners.contains(listener)){
			pendingListeners.remove(listener);
		}
	}
	
	private void registerListener(final ApiListener listener){
		if(!pendingListeners.contains(listener)){
			pendingListeners.add(listener);
		}
	}
	
	private boolean onPending(final ApiListener listener){
		return pendingListeners.contains(listener);
	}
	/*
	 * asynchronized remote method invoked by method specific params
	 */
	public void remoteCall(final Api method, final ApiParams params, final ApiListener listener){
		if (method == null) {
			throw new IllegalArgumentException("method must not null.");
		}
		if (params == null) {
			throw new IllegalArgumentException("params must not null.");
		}
		if (listener == null) {
			throw new IllegalArgumentException("listener must not null.");
		}
		
		if(!isMandatoryReady()){
			throw new IllegalArgumentException("invoke init before remote call");
		}
		
		this.registerListener(listener);
		
		new Thread() {
			@Override
			public void run() {
				invokeApi(method, params, listener);
			}
		}.start();
		
	}
	
	private static String getMD5(String str) {
		byte[] source = str.getBytes();
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(source);

			StringBuilder sb = new StringBuilder();
			for (byte b : md5.digest()) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String loadCache(String fullUrl){
		Log.d("loadCache", fullUrl);
		if(this.cache != null){
			return this.cache.onLoad(fullUrl);
		}
		return null;
	}
	
	private void saveCache(String fullUrl, String jsonStr){
		Log.d("saveCache", fullUrl);
		if(this.cache != null){
			this.cache.onSave(fullUrl, jsonStr);
		}
	}
	
	private final String invokeApi(final Api method, final ApiParams params, boolean skipRegisterDevice) throws Exception {

		Map<String, String> map = this.commonParams.getParams();
		if (map != null) {
			Set<Entry<String, String>> set = map.entrySet();
			for (Entry<String, String> entry : set) {
				if (!params.hasParam(entry.getKey())) { 
					params.addParam(entry.getKey(), entry.getValue());
				}
			}
		}
		
		
		params.addParam(ApiParams.KEY_TIMESTAMP, (System.currentTimeMillis() / 1000) + "");
		String md5String = getMD5(params.toString() + ApiClient.apiSecrect);//FIXME:fake access token generation
		params.addParam(ApiParams.KEY_ACCESSTOKEN, md5String);
		
		Log.d("invokeApi", params.toString());
		
		String url = apiUrl + method.getApiName() + "/";
		String jsonStr = null; 
		String fullUrl = WebUtils.getFullUrl(url,params);
		if(params.useCache){
			//fetch data from database directly
			jsonStr = loadCache(fullUrl);
		}
		
		if(jsonStr == null){//no hit from cache or cache not enabled
			jsonStr = method.getMethodType() == METHOD_TYPE.HTTP_POST ? WebUtils.doPost(context, url,
				params.getParams(),
				null,//no header specified
				null,//no file item
				connectTimeout, readTimeout) :WebUtils.doGet(context, url, params.getParams());
		}
		Log.d(LOG_TAG, jsonStr);
		
		//if(params.useCache){ //anyway, save it to cache
		this.saveCache(fullUrl, jsonStr);
		
		if (!skipRegisterDevice) {
			registerDevice();
		}
		
		return jsonStr;
	
	}
	
	public final String invokeApi(final Api method, final ApiParams params) throws Exception {
		return invokeApi(method, params, false);
	}
	
	private final void invokeApi(final Api method, final ApiParams params, final ApiListener listener){
		try {
			handleApiResponse(listener, invokeApi(method, params));
		} catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage(), e);
			
			if(!this.onPending(listener)){
				return;
			}
			this.unregisterListener(listener);
			listener.onException(e);
		}
	}
	
	
	private void handleApiResponse(ApiListener listener, String jsonStr)
			throws JSONException {
		Log.d("handleApiResponse", jsonStr);
		
		if(!this.onPending(listener)){
			return;
		}
		this.unregisterListener(listener);
		
		JSONObject json = null;
		ApiError error = null;
		try{
			json = new JSONObject(jsonStr);
			error = this.parseError(json);
		}catch(JSONException e){
			
		}
		if (error != null) {// failed
			Log.e(LOG_TAG, jsonStr);
			listener.onError(error);
		} else {
			listener.onComplete(json,jsonStr);
		}
	}

	private ApiError parseError(JSONObject json) throws JSONException {
		
		return null;//for now error definition not unified, disable the feature
		/*JSONObject resp = json.optJSONObject("error_response");
		if (resp == null) {
			return null;
		}
		String code = resp.optString("code");
		String msg = resp.optString("msg");
		String sub_code = resp.optString("sub_code");
		String sub_msg = resp.optString("sub_msg");
		ApiError error = null;
		if (!TextUtils.isEmpty(code) || !TextUtils.isEmpty(sub_code)) {
			error = new ApiError();
			error.setErrorCode(code);
			error.setMsg(msg);
			error.setSubCode(sub_code);
			error.setSubMsg(sub_msg);
		}
		return error;
		*/
	}
	
	private void registerDevice(){
		UserBean currentUser = (UserBean) Util.loadDataFromLocate(GlobalDataManager.getInstance().getApplicationContext(), "user", UserBean.class);
		if(currentUser == null){
			UserBean anonymousUser = (UserBean) Util.loadDataFromLocate(GlobalDataManager.getInstance().getApplicationContext(), "anonymousUser", UserBean.class);
			if(anonymousUser != null){
				Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "user", anonymousUser);
				BxMessageCenter.defaultMessageCenter().postNotification(IBxNotificationNames.NOTIFICATION_USER_CREATE, anonymousUser);
				return;
			}
		}else{
			return;
		}
		
		String apiName = "user_autoregister";
//		ArrayList<String> list = new ArrayList<String>();
		ApiParams params = new ApiParams();
		
		try {
			String json_response = this.invokeApi(Api.createPost(apiName), params, true);
			
			if (json_response != null) {
				JSONObject jsonObject = new JSONObject(json_response);

				JSONObject userObj = null;
				try {
					userObj = jsonObject.getJSONObject("user");
				} catch (Exception e) {
//					userObj = ";
					e.printStackTrace();
				}
				JSONObject json = jsonObject.getJSONObject("error");
//				String message = json.getString("message");

				if (userObj != null) {
					
					// 登录成功
					UserBean user = new UserBean();
//					JSONObject jb = jsonObject.getJSONObject("id");
					user.setId(userObj.getString("id"));
//					user.
//					user.setPhone(userObj.getString("mobile"));
					
					Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "user", user);
					Util.saveDataToLocate(GlobalDataManager.getInstance().getApplicationContext(), "anonymousUser", user);
					BxMessageCenter.defaultMessageCenter().postNotification(IBxNotificationNames.NOTIFICATION_USER_CREATE, user);
				} 
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}

	
	public interface CacheProxy{
		public void onSave(String url, String jsonStr);
		public String onLoad(String url);
	}
	
    static public String generateUsertoken(String password){
		String password1 =TextUtil.getMD5(password.trim());//Communication.getMD5(password.trim());
	password1 += apiSecrect;
	return TextUtil.getMD5(password1);
    }
		
}
