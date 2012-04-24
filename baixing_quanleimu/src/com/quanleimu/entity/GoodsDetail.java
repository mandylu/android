package com.quanleimu.entity;

import java.io.Serializable;
import java.util.HashMap;

public class GoodsDetail implements Serializable{
	
	/**
	 * 序列化
	 */
	private static final long serialVersionUID = 1L;
	public String id;
	public String mobile="无";
	public String link;
	public String title="无标题";
	public String description="无";
	public String lat;
	public String lng;
	public String categoryEnglishName="无";
	public ImageList imageList = new ImageList();
	public String areaNames="无";
	public Long date;
	public double distance = 0;
	public HashMap<String,String> metaData = new HashMap<String, String>();
	
	public Long getDate() {
		return date;
	}
	public void setDate(Long date) {
		this.date = date;
	}
	public HashMap<String, String> getMetaData() {
		return metaData;
	}
	public void setMetaData(HashMap<String, String> metaData) {
		this.metaData = metaData;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getLat() {
		return lat;
	}
	public void setLat(String lat) {
		this.lat = lat;
	}
	public String getLng() {
		return lng;
	}
	public void setLng(String lng) {
		this.lng = lng;
	}
	public String getCategoryEnglishName() {
		return categoryEnglishName;
	}
	public void setCategoryEnglishName(String categoryEnglishName) {
		this.categoryEnglishName = categoryEnglishName;
	}
	public ImageList getImageList() {
		return imageList;
	}
	public void setImageList(ImageList imageList) {
		this.imageList = imageList;
	}
	public String getAreaNames() {
		return areaNames;
	}
	public void setAreaNames(String areaNames) {
		this.areaNames = areaNames;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	@Override
	public String toString() {
		return "GoodsDetail [id=" + id + ", mobile=" + mobile + ", link="
				+ link + ", title=" + title + ", description=" + description
				+ ", lat=" + lat + ", lng=" + lng + ", categoryEnglishName="
				+ categoryEnglishName + ", imageList=" + imageList
				+ ", areaNames=" + areaNames + ", date=" + date + ", distance="
				+ distance + ", metaData=" + metaData + "]";
	}
	
	
}
