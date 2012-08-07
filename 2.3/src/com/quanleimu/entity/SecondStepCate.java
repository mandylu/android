package com.quanleimu.entity;

import java.io.Serializable;

public class SecondStepCate extends Object implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String name = "";;
	public String englishName = "";;
	public String parentEnglishName = "";
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEnglishName() {
		return englishName;
	}
	public void setEnglishName(String englishName) {
		this.englishName = englishName;
	}
	public String getParentEnglishName() {
		return parentEnglishName;
	}
	public void setParentEnglishName(String parentEnglishName) {
		this.parentEnglishName = parentEnglishName;
	}
	@Override
	public String toString() {
		return name;
	}
}
