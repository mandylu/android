package com.baixing.android.api;

public class ApiClient {
	final private String apikey = "";
	final private String secrect = "";
	private static ApiClient _instance = null;
	
	protected ApiClient(){
		
	}
	
	public static ApiClient getInstance(){
		if(_instance != null){
			return _instance;
		}
		_instance = new ApiClient();
		return _instance;
	}
	/*
	 * asynchronized remote method invoke
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
		
		new Thread() {
			@Override
			public void run() {
				invokeApi(method, params, listener);
			}
		}.start();
		
		
	}
	
	private void invokeApi(final String method, final ApiParams params, final ApiListener listener){
		//1. compose appropriate url
		//2. setup network connection
		//3. send data and parse response to JSON object if no exception found
		//4. invoke listener
	}
		
}
