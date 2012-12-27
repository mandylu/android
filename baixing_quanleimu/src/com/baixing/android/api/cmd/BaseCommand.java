//liuchong@baixing.com
package com.baixing.android.api.cmd;

import org.json.JSONObject;

import com.baixing.android.api.ApiClient;
import com.baixing.android.api.ApiError;
import com.baixing.android.api.ApiListener;
import com.baixing.android.api.ApiParams;

public class BaseCommand implements ApiListener {
	public static interface Callback {
		public void onNetworkDone(int requstCode, String responseData);
		
		public void onNetworkFail(int requstCode, ApiError error);
	}
	
	private int requestCode;
	private ApiParams apiParams;
	private String apiName;
	
	private Callback callback;
	
	private BaseCommand(final int requestCode, String apiName, ApiParams params) {
		this.requestCode = requestCode;
		this.apiName = apiName;
		this.apiParams = params;
	}
	
	public static BaseCommand createCommand(final int reqCode, String apiName, ApiParams params) {
		return new BaseCommand(reqCode, apiName, params);
	}
	
	public void execute(Callback callback) {
		this.callback = callback;
		ApiClient.getInstance().remoteCall(apiName, apiParams, this);
	}
	
	public void cancel() {
		ApiClient.getInstance().cancel(this);
	}

	@Override
	public void onComplete(JSONObject json, String rawData) {
		callback.onNetworkDone(this.requestCode, rawData);
	}

	@Override
	public void onError(ApiError error) {
		callback.onNetworkFail(this.requestCode, error);
	}

	@Override
	public void onException(Exception e) {
		callback.onNetworkFail(this.requestCode, null); //FIXME: should tell client what's wrong?
	}
}
