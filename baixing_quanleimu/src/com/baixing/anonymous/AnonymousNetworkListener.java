//xumengyi@baixing.com
package com.baixing.anonymous;

import com.baixing.network.api.ApiParams;

public interface AnonymousNetworkListener{
	public static class ResponseData{
		public boolean success;
		public String message;
	}
	void onActionDone(String action, ResponseData response);
	void beforeActionDone(String action, ApiParams outParams);
//	void onNeedActionToBeDone(String action);
}