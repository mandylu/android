package com.quanleimu.entity;

import java.io.Serializable;

public class HotList implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1494862624344532411L;
	
	public String imgUrl = "";
	public int type = -1;
	public HotData hotData = new HotData();
	
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public HotData getHotData() {
		return hotData;
	}
	public void setHotData(HotData hotData) {
		this.hotData = hotData;
	}
	@Override
	public String toString() {
		return "HotList [imgUrl=" + imgUrl + ", type=" + type + ", hotData="
				+ hotData + "]";
	}
	
	
}
