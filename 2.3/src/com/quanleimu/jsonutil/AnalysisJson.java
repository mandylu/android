package com.quanleimu.jsonutil;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AnalysisJson {
	public String returncode = "returncode";
	public String returnmessage = "returnmessage";
	public String total = "total";
	public String result = "result";
	
	//解析返回的code
	public int getReturncode(String json){
		JSONObject obj = null;
		try {
			obj = new JSONObject(json);
			return obj.getInt(returncode);
		} catch (JSONException e) {
			
		}
		return -1;
	}
	
	//解析返回的消息
	public String getReturnmessage(String json){
		JSONObject obj = null;
		try {
			obj = new JSONObject(json);
			return obj.getString(returnmessage);
		} catch (JSONException e) {
			
		}
		return "";
	}
	
	//解析信息的条数
	public int getTotal(String json){
		JSONObject obj = null;
		try {
			obj = new JSONObject(json);
			return obj.getInt(total);
		} catch (JSONException e) {
			
		}
		return -1;
	}
	
	//解析数据集
	public JSONArray getResult(String json){
		JSONObject obj = null;
		try {
			obj = new JSONObject(json);
			String resultData = obj.getString(result);
			return new JSONArray(resultData);
		} catch (JSONException e) {
			
		}
		return null;
	}
	
	//将for放到try外面
	public ArrayList<HashMap<String, String>> getAllData(String json, String valueKey[]){
		ArrayList<HashMap<String, String>> listMap = new ArrayList<HashMap<String,String>>();
		JSONArray array = getResult(json);
		
		if(array == null || array.length() == 0)
		{
			listMap = null;
		}
		else
		{
			for(int i=0; i<array.length(); i++){
				HashMap<String, String> map;
				try { 
					map = getMoreData(array.getString(i), valueKey);
					if(map != null){
						listMap.add(map);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return listMap;
	}
	

	public HashMap<String, String> getData(String json, String valueKey[]){
		ArrayList<HashMap<String, String>> listMap = new ArrayList<HashMap<String,String>>();
		JSONArray array = getResult(json);
		try {
			for(int i=0; i<array.length(); i++){
				HashMap<String, String> map = getMoreData(array.getString(i), valueKey);
				if(map != null){
					listMap.add(map);
				}
			}
		} catch (JSONException e) {
		}
		if(listMap.size() > 0){
			return listMap.get(0);
		}
		return null;
	}
	
	public HashMap<String, String> getMoreData(String json, String valueKey[]){
		JSONObject obj = null;
		try {
			obj = new JSONObject(json);
			String data = "";
			HashMap<String, String> map = new HashMap<String, String>();
			for(int i=0; i<valueKey.length; i++){
				try {
					data = obj.getString(valueKey[i]);
					if(data == null || data.equals(""))
					{
						data = "";
					}
				} catch (Exception e) {
					data = "";
					try {
						int a = 0;
						
						a = obj.getInt(valueKey[i]);
						data = a + "";
					} catch (Exception e2) {
						try {
							data = obj.getBoolean(valueKey[i])+"";
						} catch (Exception e3) {
							continue;
						}
					}
					
				}
				
				map.put(valueKey[i], data);
				
			}
			return map;
		} catch (JSONException e) {
			
		}
		
		return null;
	}
	
	//商场电话
	public static String getShopMallTel(String json){
		JSONArray array;
		String telNum = "";
		String a = "";
		try {
			array = new JSONArray(json);
			if(array == null || array.length() == 0)
			{
				telNum = "";
			}
			else
			{
				for(int i=0;i<array.length();i++)
				{
					JSONObject jsonTel = array.optJSONObject(i);
					try {
						a = jsonTel.getString("PhoneNumber");
					} catch (Exception e) {
						a = "";
					}
					if(a != null && !a.equals(""))
					{
						telNum = a;
						break;
					}
				}
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return telNum;
	}
	
	//商场图片
	public static String getShopMallImage(String json){
		JSONArray array;
		String shopmallImage = "";
		String a = "";
		try {
			array = new JSONArray(json);
			if(array == null || array.length() == 0)
			{
				shopmallImage = "";
			}
			else
			{
				for(int i=0;i<array.length();i++)
				{
					JSONObject jsonTel = array.optJSONObject(i);
					try {
						a = jsonTel.getString("ImageUrl");
					} catch (Exception e) {
						a = "";
					}
					if(a != null && !a.equals(""))
					{
						shopmallImage = a;
						break;
					}
				}
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return shopmallImage;
	}
	
	//商场图片
	public static String getShopMallBrands(String json){
		JSONArray array;
		String shopmallBrand = "";
		String a = "";
		try {
			array = new JSONArray(json);
			if(array == null || array.length() == 0)
			{
				shopmallBrand = "";
			}
			else
			{
				for(int i=0;i<array.length();i++)
				{
					JSONObject jsonTel = array.optJSONObject(i);
					try {
						a = jsonTel.getString("BrandName");
					} catch (Exception e) {
						a = "";
					}
					if(a != null && !a.equals(""))
					{
						if(shopmallBrand.equals(""))
						{
							shopmallBrand = a;
						}
						else
						{
							shopmallBrand = shopmallBrand + ","+a;
						}
					}
				}
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return shopmallBrand;
	}
}
