package com.quanleimu.entity;

import java.io.Serializable;

public class BXLocation implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 0L;
	
	public float fLat = 0.0f;//latitude
	public float fLon = 0.0f;//longitude
	
	public boolean geocoded = false;
	
	public String adminArea = "";
	public String cityName = "";
	public String subCityName ="";
	public String address = "";
	public String detailAddress = "";
	public String postCode = "";
	
	public float fGeoCodedLat = 0.0f;
	public float fGeoCodedLon = 0.0f;
	
	public BXLocation(boolean bDefault){
		if(bDefault){
			this.fLat = 31.198486f;
			this.fLon = 121.435018f;
			
			this.geocoded = true;
			
			this.fGeoCodedLat = 31.198486f;
			this.fGeoCodedLon = 121.435018f;				
			this.adminArea = "上海市";
			this.cityName = "上海";
			this.subCityName = "徐汇区";
			this.address = "广元西路55号";
			this.detailAddress = "上海市徐汇区广元西路55号";
			this.postCode = "200030";
			
		}
	}
};