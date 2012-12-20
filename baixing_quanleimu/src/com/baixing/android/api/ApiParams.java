package com.baixing.android.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApiParams {
	private String method;
	private List<String> fields=new ArrayList<String>();
	private Map<String,String> params=new HashMap<String,String>();
	/**
	 * 添加返回数据字段
	 * 
	 * @param value
	 */
	public void addFields(String... value){
		if(value!=null){
			for(String v:value){
				fields.add(v);
			}
		}
	}
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
	
	public String getMethod() {
		return method;
	}
	/**
	 * 设置准备调用的api名字
	 * @param method
	 */
	public void setMethod(String method) {
		this.method = method;
	}
	public List<String> getFields() {
		return fields;
	}
	public void setFields(List<String> fields) {
		this.fields = fields;
	}
	public Map<String, String> getParams() {
		return params;
	}
	public void setParams(Map<String, String> params) {
		this.params = params;
	}
	

}
