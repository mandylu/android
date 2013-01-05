//liuweili@baixing.com
package com.baixing.android.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
//liuweili@baixing.com
import java.util.Map.Entry;

import com.baixing.entity.UserBean;
import com.baixing.util.Communication;
import com.baixing.util.TextUtil;

/**
 * 
 * @history: by liuchong add <code>addParams</code> for primitive types.
 */
public class ApiParams implements Serializable {
	
	private static final long serialVersionUID = 6811845003931804312L;
	
	public static final String KEY_APIKEY = "api_key";
	public static final String KEY_UDID = "udid";
	public static final String KEY_VERSION = "version";
	public static final String KEY_TIMESTAMP = "timestamp";
	public static final String KEY_CHANNEL = "channel";
	public static final String KEY_USERID = "userId";
	public static final String KEY_CITY = "city";
	public static final String KEY_ACCESSTOKEN = "access_token";
	
	private Map<String,String> params=new HashMap<String,String>();
	public boolean useCache = false;
	
	/**
	 * 添加业务参数
	 * @param key
	 * @param value
	 */
	public void addParam(String key,String value){
		addParameter(key, value);
	}
	
	public boolean hasParam(String key) {
		return params.containsKey(key);
	}
	
	public void addAll(Map<String, String> all){
		params.putAll(all);
	}
	
	private void addParameter(String key, Object value)
	{
		if (value != null)
		{
			params.put(key, value.toString());
		}
	}
	
	public void addParam(String key, int value)
	{
		params.put(key, value + "");
	}
	
	public void addParam(String key, double value)
	{
		params.put(key, value + "");
	}
	
	public void addParam(String key, long value)
	{
		params.put(key, value + "");
	}
	
	public void addParam(String key, float value)
	{
		params.put(key, value + "");
	}
	
	public void addParam(String key, short value)
	{
		params.put(key, value + "");
	}
	
	public void addParam(String key, boolean value)
	{
		this.addParameter(key, Boolean.valueOf(value));
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
	
	public void appendUserInfo(UserBean user) {
		if (user == null) {
			return;
		}
		this.addParam("mobile", user.getPhone());
		this.addParam("userToken", ApiClient.generateUsertoken(user.getPassword()));
	}
	
    static public void makeupUserInfoParams(UserBean user, List<String> params){
		if(user != null && params != null){
			params.add("mobile=" + user.getPhone());
			params.add("userToken=" + ApiClient.generateUsertoken(user.getPassword()));
		}    	
    }

}
