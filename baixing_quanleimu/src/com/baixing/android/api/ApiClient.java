//liuweili@baixing.com
package com.baixing.android.api;

import java.security.MessageDigest;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.json.JSONException;
import org.json.JSONObject;

import com.baixing.util.Util;
import com.quanleimu.activity.QuanleimuApplication;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class ApiClient {
	private static final String LOG_TAG = "ApiClient";
	private static final String apiKey = "api_mobile_android";
	private static final String apiSecrect = "c6dd9d408c0bcbeda381d42955e08a3f";
	private static final String apiUrl = "http://www.baixing.com/api/mobile.";
	private static ApiClient _instance = null;
	private ApiParams commonParams = new ApiParams();
	private Context context = null;
	private int connectTimeout = 10000;// 10秒
	private int readTimeout = 30000;// 30秒
	
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
	public void init(Context context, String udid, String version, String channel, String city){
		this.context = context;
		commonParams.addParam(ApiParams.KEY_UDID, udid);
		commonParams.addParam(ApiParams.KEY_VERSION, version);
		commonParams.addParam(ApiParams.KEY_CHANNEL, channel);
		commonParams.addParam(ApiParams.KEY_CITY, city);
		commonParams.addParam(ApiParams.KEY_APIKEY, ApiClient.apiKey);
	}
	
	public boolean isMandatoryReady(){
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
	/*
	 * asynchronized remote method invoke by method specific params
	 */
	public void remoteCall(final String method, final ApiParams params, final ApiListener listener){
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
	
	private void invokeApi(final String method, final ApiParams params, final ApiListener listener){
		//insert common params
		Map<String, String> map = this.commonParams.getParams();
		if (map != null) {
			Set<Entry<String, String>> set = map.entrySet();
			for (Entry<String, String> entry : set) {
				params.addParam(entry.getKey(), entry.getValue());
			}
		}
		
		
		params.addParam(ApiParams.KEY_TIMESTAMP, (System.currentTimeMillis() / 1000) + "");
		String md5String = getMD5(params.toString() + ApiClient.apiSecrect);//FIXME:fake access token generation
		params.addParam(ApiParams.KEY_ACCESSTOKEN, md5String);
		
		Log.d("invokeApi", params.toString());
		//start url
		try {
			String jsonStr = WebUtils.doPost(context, this.apiUrl + method + "/",
					params.getParams(),
					null,//no header specified
					null,//no file item
					connectTimeout, readTimeout);
			Log.d(LOG_TAG, jsonStr);
			handleApiResponse(listener, jsonStr);
		} catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage(), e);
			listener.onException(e);
		}
	}
	
	
	private void handleApiResponse(ApiListener listener, String jsonStr)
			throws JSONException {
		Log.d("handleApiResponse", jsonStr);
		JSONObject json = new JSONObject(jsonStr);
		ApiError error = this.parseError(json);
		if (error != null) {// failed
			Log.e(LOG_TAG, jsonStr);
			listener.onError(error);
		} else {
			listener.onComplete(json);
		}
	}

	private ApiError parseError(JSONObject json) throws JSONException {
		JSONObject resp = json.optJSONObject("error_response");
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
	}

		
}
