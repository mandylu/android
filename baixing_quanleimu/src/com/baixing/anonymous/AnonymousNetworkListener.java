//xumengyi@baixing.com
package com.baixing.anonymous;
public interface AnonymousNetworkListener{
	public static class ResponseData{
		public boolean success;
		public String message;
	}
	void onActionDone(String action, ResponseData response);
	
//	void onNeedActionToBeDone(String action);
}