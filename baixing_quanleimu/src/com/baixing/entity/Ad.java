//liuchong@baixing.com
package com.baixing.entity;

import java.io.Serializable;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
public class Ad implements Serializable{
	
	/**
	 * 序列化
	 */
	private static final long serialVersionUID = 1L;
	public ImageList imageList = new ImageList();
	public double distance = 0;
	public ArrayList<String> metaData = new ArrayList<String>();
	public HashMap<String, String> data = new HashMap<String, String>();
	public enum EDATAKEYS{
		EDATAKEYS_TITLE("title"),
		EDATAKEYS_DESCRIPTION("description"),
		EDATAKEYS_LAT("lat"),
		EDATAKEYS_LON("lng"),
		EDATAKEYS_DATE("createdTime"),
        EDATAKEYS_POST_TIME("insertedTime"),
		EDATAKEYS_ID("id"),
		EDATAKEYS_CATEGORYENGLISHNAME("categoryEnglishName"),
		EDATAKEYS_CITYENGLISHNAME("cityEnglishName"),
		EDATAKEYS_AREANAME("areaNames"),
		EDATAKEYS_MOBILE("mobile"),
		EDATAKEYS_MOBILE_AREA("mobileArea"),
		EDATAKEYS_WANTED("wanted"),
		EDATAKEYS_CONTACT("contact"),
		EDATAKEYS_LINK("link"),
		EDATAKEYS_PRICE("价格");
		
		public String metaKey;
		private EDATAKEYS(String key)
		{
			this.metaKey = key;
		}
	}
	@JsonIgnore
	public Set<String> getKeys(){
		if(null == data) return null;
		return data.keySet();
	}
	@JsonIgnore
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
        case EDATAKEYS_POST_TIME:
            key = "insertedTime";
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
		case EDATAKEYS_PRICE:
			key ="价格";
			break;
		default:
			break;
		}
		return key;
	}
	@JsonIgnore
	public String getValueByKey(EDATAKEYS e){
		String key = e.metaKey; //getStringByEnum(e);
		if(data.containsKey(key)){
			return data.get(key);
		}
		return  "";
	}
	@JsonIgnore
	public String getValueByKey(String key){
		if(key.equals("")) return "";
//		EDATAKEYS e =  getEnumByString(key);
//		if(e == null) return "";
		if(data.containsKey(key)){
			return data.get(key);
		}
		return  "";
	}	
	@JsonIgnore
	public void setValueByKey(EDATAKEYS e, String value){
		String key =  getStringByEnum(e);
		if(key.equals("")) return;
		data.put(key, value);
	}
	@JsonIgnore
	public void setValueByKey(String key, String value){
		if(key == null || key.equals("")) return;
//		EDATAKEYS e =  getEnumByString(key);
//		if(e == null) return;
		if(value != null && !value.equals("")){
			String strDecoded = value.replaceAll("&amp;", "&").replace("&#039;", "'").replaceAll("&quot;", "\"");
			data.put(key, strDecoded);
		}
			
	}
	@JsonIgnore
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
	
	@Override
	public boolean equals (Object o){
		if(o instanceof Ad){
			String idOfthis = getValueByKey(EDATAKEYS.EDATAKEYS_ID);
			String idOfo = ((Ad)o).getValueByKey(EDATAKEYS.EDATAKEYS_ID);
			boolean bRet = idOfthis.equals(idOfo);
			
			return bRet;
		}
		
		return false;
	}
	
	@JsonIgnore
	public boolean isValidMessage() {
		return !this.getValueByKey("status").equals("4") && !this.getValueByKey("status").equals("20");
	}
	
}
