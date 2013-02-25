package com.baixing.network.api;

import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.baixing.network.NetworkUtil;
import com.baixing.network.impl.GetRequest;
import com.baixing.network.impl.HttpNetworkConnector;
import com.baixing.network.impl.IHttpRequest;
import com.baixing.network.impl.IRequestStatusListener;
import com.baixing.network.impl.PostRequest;
import com.baixing.network.impl.ZippedPostRequest;

/**
 * 
 * @author liuchong
 *
 * @date Feb 17, 2013
 */
public final class BaseApiCommand implements IRequestStatusListener {
	private static final String TAG = "ApiCommand";
	
	static String API_KEY = "";
	static String API_SECRET = "";
	static String HOST = "www.baixing.com";
//	private static String apiUrl = "http://" + host + "/api/mobile.";
	
	public static interface Callback {
		public void onNetworkDone(String apiName, String responseData);
		
		public void onNetworkFail(String apiName, ApiError error);
	}
	
	private static ApiParams commonParams = new ApiParams();
	
	private String apiName;
	private ApiParams apiParams;
	private boolean isGet;
	
	private Callback callback;
	
	private IHttpRequest request;
	
	protected BaseApiCommand(String apiName, ApiParams params, boolean isGetRequest) {
		this.apiName = apiName;
		this.apiParams = params;
		this.isGet = isGetRequest;
		if (apiParams == null) {
			apiParams = new ApiParams();
		}
	}
	
	public static BaseApiCommand createCommand(String apiName, boolean isGet, ApiParams params) {
		return new BaseApiCommand(apiName, params, isGet);
	}
	
	private static String getApiUri(String apiName) {
		return "http://" + HOST + "/api/mobile." + apiName + "/?";
	}
	
	public static void init(String udid, String userId, String version, String channel, String city) {
		commonParams.addParam(ApiParams.KEY_UDID, udid);
		commonParams.addParam(ApiParams.KEY_USERID, userId);
		commonParams.addParam(ApiParams.KEY_VERSION, version);
		commonParams.addParam(ApiParams.KEY_CHANNEL, channel);
		commonParams.addParam(ApiParams.KEY_CITY, city);
		commonParams.addParam(ApiParams.KEY_APIKEY, API_KEY);
	}
	
	public static void addCommonParams(String key, String value) {
		commonParams.addParam(key, value);
	}
	
	public void execute(Context context, Callback callback) {
		HttpNetworkConnector connector = preSending();
		
		if (connector != null) {
			this.callback = callback;
			connector.sendHttpRequest(context, request, new PlainRespHandler(), this);
		}
	}
	
	public String executeSync(Context context) {
		HttpNetworkConnector connector = preSending();
		if (connector != null) {
			Pair<Boolean, String> result = (Pair<Boolean, String>) connector.sendHttpRequestSync(context, request, new PlainRespHandler());

			return result == null ? null : decodeUnicode(result.second);
		}
		
		return null;
	}
	
	private HttpNetworkConnector preSending() {
		if (request != null && !request.isCanceled()) {
			return null; //Do not make request if command is running.
		}
		
		Map<String, String> params = mergeParams();
		String url = getApiUri(apiName);
		request = isGet ? new GetRequest(url, params, apiParams.useCache) : 
										(apiParams.zipRequest ? new ZippedPostRequest(url, params) : new PostRequest(url, params));
		return HttpNetworkConnector.connect();
	}
	
	private Map<String, String> mergeParams() {
		ApiParams allParams = new ApiParams();
		
		Map<String, String> common = commonParams.getParams();
		
		allParams.addAll(this.apiParams.getParams());
		Iterator<String> keys = common.keySet().iterator();
		while (keys.hasNext()) {
			String key = keys.next();
			if (!allParams.hasParam(key)) {
				allParams.addParam(key, common.get(key));
			}
		}
		
		
		allParams.addParam(ApiParams.KEY_TIMESTAMP, (System.currentTimeMillis() / 1000) + "");
		String md5String = NetworkUtil.getMD5(allParams.toString() + API_SECRET);
		allParams.addParam(ApiParams.KEY_ACCESSTOKEN, md5String);
		
		return allParams.getParams();
	}
	
	public void cancel() {
		if (request != null) {
			request.cancel();
			request = null;
		}
	}
	
	@Override
	public void onConnectionStart() {
	}

	@Override
	public void onReceiveData(long cursor, long total) {
	}

	@Override
	public void onProcessingData() {
	}

	@Override
	public void onCancel() {
		request.cancel();
	}

	@Override
	public void onRequestDone(Object response) {
		
		if (request != null && request.isCanceled()) {
			return;
		}
		
		request = null; //Set request to null.
		
		Pair<Boolean, String> result = (Pair<Boolean, String>) response;
		final String decodedResponse = decodeUnicode(result.second);
		if (result.first) {
			ApiError error = parseForError(decodedResponse);
			if (error == null) {
				callback.onNetworkDone(apiName, decodedResponse);
			} else {
				callback.onNetworkFail(apiName, error);
			}
		} else {
			ApiError error = new ApiError();
			error.setMsg(decodedResponse);
			
			callback.onNetworkFail(apiName, error);
		}
		
	}
	
	private ApiError parseForError(String response) {
		if (!response.trim().startsWith("{\"error\"")) {
			return null;
		}
		
		JSONObject obj = null;
		ApiError error = null;
		try {
			obj = new JSONObject(response);
			JSONObject detail = obj.getJSONObject("error");
			if (detail == null) {
				return null;
			} else {
				error = new ApiError();
				error.setErrorCode(detail.getString("code"));
				error.setMsg(detail.getString("message"));
			}
			
		} catch (JSONException e) {
			Log.w(TAG, "fail to parse response " + response);
		}
		
		if (error != null && "0".equals(error.getErrorCode())) { //For API1.0, succed request response with {error:{code:0,message:"login success"}}
			return null;
		}
		
		return error;
	}
	
	public static final String decodeUnicode(String source) {
		if (null == source || " ".equals(source)) {
			return source;
		}
		StringBuffer sb = new StringBuffer();
		int i = 0;
		while (i < source.length()) {
			if (source.charAt(i) == '\\') {
				if (source.charAt(i + 1) == 'u') {
					int j = Integer
							.parseInt(source.substring(i + 2, i + 6), 16);
					sb.append((char) j);
					i += 6;
				} else {
					sb.append(source.charAt(i));
					i++;
					sb.append(source.charAt(i));
					i++;
				}
			} else {
				sb.append(source.charAt(i));
				i++;
			}
		}
		return sb.toString();

	}

}
