package com.baixing.network.api;

import com.baixing.network.ICacheProxy;
import com.baixing.network.impl.HttpNetworkConnector;

public class ApiConfiguration {
	
	/**
	 * Do configuration before API invocation.
	 * @param apiHost target api server, "www.baixing.com"
	 * @param proxy cache proxy used to store/load http get request. pass null to disable cache.
	 * @param apiKey apikey used to invok baixing API service.
	 * @param apiSecret api secret used to invoke baixing API service.
	 */
	public static final void config(String apiHost, ICacheProxy proxy, String apiKey, String apiSecret) {
		BaseApiCommand.API_KEY = apiKey;//"api_mobile_android";
		BaseApiCommand.API_SECRET = apiSecret;//"c6dd9d408c0bcbeda381d42955e08a3f";
		
		setHost(apiHost);
		
		HttpNetworkConnector.cacheProxy = proxy;
	}
	
	public static final void setHost(String apiHost) {
		if (apiHost != null) {
			BaseApiCommand.HOST = apiHost;
			FileUploadCommand.HOST = apiHost;
		}
	}
	
	public static final String getHost() {
		return BaseApiCommand.HOST;
	}
}
