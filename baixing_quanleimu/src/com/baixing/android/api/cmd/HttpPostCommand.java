//liuchong@baixing.com
package com.baixing.android.api.cmd;

import com.baixing.android.api.ApiClient.Api;
import com.baixing.android.api.ApiParams;

public class HttpPostCommand extends BaseCommand {

	protected HttpPostCommand(int requestCode, Api api, ApiParams params) {
		super(requestCode, api, params);
	}
	
	public static BaseCommand createCommand(int requestCode, String apiName, ApiParams params) {
		return new HttpPostCommand(requestCode, Api.createPost(apiName), params);
	}

}
