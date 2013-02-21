package com.baixing.network;

/**
 * 
 * @author liuchong
 *
 */
public interface ICacheProxy {
	public void onSave(String url, String data);
	public String onLoad(String url);
}
