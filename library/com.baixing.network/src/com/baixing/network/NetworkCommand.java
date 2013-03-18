package com.baixing.network;

import com.baixing.network.api.PlainRespHandler;
import com.baixing.network.impl.GetRequest;
import com.baixing.network.impl.HttpNetworkConnector;
import com.baixing.network.impl.IHttpRequest;
import com.baixing.network.impl.PostRequest;

import android.content.Context;
import android.util.Pair;

public class NetworkCommand {
	private String requestUrl;
	private boolean isGet;
	
	protected NetworkCommand(String url, boolean isGet) {
		this.requestUrl = url;
		this.isGet = isGet;
	}

	public static String doGet(Context cxt, String url) {
		NetworkCommand cmd = new NetworkCommand(url, true); 
		return cmd.execute(cxt);
	}
	
	public static String doPost(Context cxt, String url) {
		NetworkCommand cmd = new NetworkCommand(url, true); 
		return cmd.execute(cxt);
	}
	
	private String execute(Context context) {
		HttpNetworkConnector connector = HttpNetworkConnector.connect();
		if (connector != null) {
			IHttpRequest request = isGet ? new GetRequest(requestUrl, null, false) : new PostRequest(requestUrl, null);
			Pair<Boolean, String> result = (Pair<Boolean, String>) connector.sendHttpRequestSync(context, request, new PlainRespHandler());

			return result == null ? null : result.second;
		}
		
		return null;
	}
	
}
