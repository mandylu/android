package com.quanleimu.entity;

import java.util.List;

public class CityHot {
	public String imgUrl;
	public String type;
	public List<CityData> citydataList;

	public List<CityData> getCitydataList() {
		return citydataList;
	}

	public void setCitydataList(List<CityData> citydataList) {
		this.citydataList = citydataList;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "CityHot [imgUrl=" + imgUrl + ", type=" + type
				+ ", citydataList=" + citydataList + "]";
	}

}
