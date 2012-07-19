package com.quanleimu.entity;

import java.io.Serializable;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
public class GoodsDetail implements Serializable{
	
	/**
	 * 序列化
	 */
	private static final long serialVersionUID = 1L;
	public ImageList imageList = new ImageList();
	public double distance = 0;
	public ArrayList<String> metaData = new ArrayList<String>();
	public HashMap<String, String> data = new HashMap<String, String>();
	public enum EDATAKEYS{
		EDATAKEYS_TITLE,
		EDATAKEYS_DESCRIPTION,
		EDATAKEYS_LAT,
		EDATAKEYS_LON,
		EDATAKEYS_DATE,
		EDATAKEYS_ID,
		EDATAKEYS_CATEGORYENGLISHNAME,
		EDATAKEYS_CITYENGLISHNAME,
		EDATAKEYS_AREANAME,
		EDATAKEYS_MOBILE,
		EDATAKEYS_WANTED,
		EDATAKEYS_CONTACT,
		EDATAKEYS_LINK
	}
	public Set<String> getKeys(){
		if(null == data) return null;
		return data.keySet();
	}
//	private EDATAKEYS getEnumByString(String key){
//		EDATAKEYS e = null;
//		if(key.equals("title")){
//			e = EDATAKEYS.EDATAKEYS_TITLE;
//		}
//		else if(key.equals("description")){
//			e = EDATAKEYS.EDATAKEYS_DESCRIPTION;
//		}
//		else if(key.equals("lat")){
//			e = EDATAKEYS.EDATAKEYS_LAT;
//		}
//		else if(key.equals("lng")){
//			e = EDATAKEYS.EDATAKEYS_LON;
//		}
//		else if(key.equals("createdTime")){
//			e = EDATAKEYS.EDATAKEYS_DATE;
//		}
//		else if(key.equals("id")){
//			e = EDATAKEYS.EDATAKEYS_ID;
//		}
//		else if(key.equals("categoryEnglishName")){
//			e = EDATAKEYS.EDATAKEYS_CATEGORYENGLISHNAME;
//		}
//		else if(key.equals("cityEnglishName"))
//		{
//			e = EDATAKEYS.EDATAKEYS_CITYENGLISHNAME;
//		}
//		else if(key.equals("areaNames")){
//			e = EDATAKEYS.EDATAKEYS_AREANAME;
//		}
//		else if(key.equals("mobile")){
//			e = EDATAKEYS.EDATAKEYS_MOBILE;
//		}
//		else if(key.equals("wanted")){
//			e = EDATAKEYS.EDATAKEYS_WANTED;
//		}
//		else if(key.equals("contact")){
//			e = EDATAKEYS.EDATAKEYS_CONTACT;
//		}
//		else if(key.equals("link")){
//			e = EDATAKEYS.EDATAKEYS_LINK;
//		}
//		return e;
//	}
	private String getStringByEnum(EDATAKEYS e){
		String key = "";
		switch(e){
		case EDATAKEYS_TITLE:
			key = "title";
			break;
		case EDATAKEYS_DESCRIPTION:
			key = "description";
			break;
		case EDATAKEYS_LAT:
			key = "lat";
			break;
		case EDATAKEYS_LON:
			key = "lng";
			break;
		case EDATAKEYS_DATE:
			key = "createdTime";
			break;
		case EDATAKEYS_ID:
			key = "id";
			break;
		case EDATAKEYS_CATEGORYENGLISHNAME:
			key = "categoryEnglishName";
			break;
		case EDATAKEYS_CITYENGLISHNAME:
			key = "cityEnglishName";
			break;
		case EDATAKEYS_AREANAME:
			key = "areaNames";
			break;
		case EDATAKEYS_MOBILE:
			key = "mobile";
			break;
		case EDATAKEYS_WANTED:
			key = "wanted";
			break;
		case EDATAKEYS_CONTACT:
			key = "contact";
			break;
		case EDATAKEYS_LINK:
			key = "link";
			break;
		default:
			break;
		}
		return key;
	}
	public String getValueByKey(EDATAKEYS e){
		String key = getStringByEnum(e);
		if(data.containsKey(key)){
			return data.get(key);
		}
		return  "";
	}
	
	public String getValueByKey(String key){
		if(key.equals("")) return "";
//		EDATAKEYS e =  getEnumByString(key);
//		if(e == null) return "";
		if(data.containsKey(key)){
			return data.get(key);
		}
		return  "";
	}	
	
	public void setValueByKey(EDATAKEYS e, String value){
		String key =  getValueByKey(e);
		if(key.equals("")) return;
		data.put(key, value);
	}
	
	public void setValueByKey(String key, String value){
		if(key.equals("")) return;
//		EDATAKEYS e =  getEnumByString(key);
//		if(e == null) return;
		data.put(key, value);
	}
	
	public String getMetaValueByKey(String key){
		for(int i = 0; i < metaData.size(); ++ i){
			String[] meta = metaData.get(i).split(" ");
			if(meta.length >= 2){
				if(meta[0].equals(key)){
					return meta[1];
				}
			}
		}
		return "";
	}
	public ArrayList<String> getMetaData() {
		return metaData;
	}
	public void setMetaData(ArrayList<String> metaData) {
		this.metaData = metaData;
	}
	public ImageList getImageList() {
		return imageList;
	}
	public void setImageList(ImageList imageList) {
		this.imageList = imageList;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	/*
	@Override
	public String toString() {
		return "GoodsDetail [id=" + id + ", mobile=" + mobile + ", link="
				+ link + ", title=" + title + ", wanted=" + wanted
				+ ", description=" + description + ", lat=" + lat + ", lng="
				+ lng + ", categoryEnglishName=" + categoryEnglishName
				+ ", imageList=" + imageList + ", areaNames=" + areaNames
				+ ", date=" + date + ", distance=" + distance + ", metaData="
				+ metaData + "]";
	}*/
	
	
}
