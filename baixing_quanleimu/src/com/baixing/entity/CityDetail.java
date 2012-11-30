package com.baixing.entity;

public class CityDetail {
	public String id;
	public String englishName;
	public String name;
	public String sheng;

	public String getEnglishName() {
		return englishName;
	}

	public void setEnglishName(String englishName) {
		this.englishName = englishName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSheng() {
		return sheng;
	}

	public void setSheng(String sheng) {
		this.sheng = sheng;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "CityDetail [id=" + id + ", englishName=" + englishName
				+ ", name=" + name + ", sheng=" + sheng + "]";
	}

}
