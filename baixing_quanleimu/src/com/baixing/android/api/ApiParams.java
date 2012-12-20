package com.baixing.android.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class ApiParams {
	public static final String KEY_APIKEY = "api_key";
	public static final String KEY_UDID = "udid";
	public static final String KEY_VERSION = "version";
	public static final String KEY_TIMESTAMP = "timestamp";
	public static final String KEY_CHANNEL = "channel";
	public static final String KEY_USERID = "userId";
	public static final String KEY_CITY = "city";
	public static final String KEY_ACCESSTOKEN = "access_token";
	
	private Map<String,String> params=new HashMap<String,String>();
	/**
	 * 添加业务参数
	 * @param key
	 * @param value
	 */
	public void addParam(String key,String value){
		params.put(key, value);
	}
	/**
	 * 获取已添加的业务参数
	 * @param key
	 * @return
	 */
	public String getParam(String key){
		return params.get(key);
	}
	/**
	 * 删除已添加的业务参数
	 * @param key
	 */
	public void removeParam(String key){
		params.remove(key);
	}
	
	public Map<String, String> getParams() {
		return params;
	}
	public void setParams(Map<String, String> params) {
		this.params = params;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer();
		Map<String, String> map = this.params;
		if (map != null) {
			Set<Entry<String, String>> set = map.entrySet();
			for (Entry<String, String> entry : set) {
				sb.append(entry.getKey() + ":" + entry.getValue());
				sb.append('\n');
			}
		}
		
		return sb.toString();
	}
	

}
