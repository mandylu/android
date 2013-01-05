//liuchong@baixing.com
package com.baixing.android.api.cmd;

import com.baixing.android.api.ApiClient.Api;
import com.baixing.android.api.ApiParams;

public class HttpGetCommand extends BaseCommand {

	protected HttpGetCommand(int requestCode, Api api, ApiParams params) {
		super(requestCode, api, params);
	}
	
	public static BaseCommand createCommand(int requestCode, String apiName, ApiParams params) {
		return new HttpGetCommand(requestCode, Api.createGet(apiName), params);
	}

}
