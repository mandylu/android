package com.baixing.entity;

import java.util.ArrayList;
import java.util.List;

public class AllCates {

	public String name = "";
	public String englishName = "";
	public List<FirstStepCate> children = new ArrayList<FirstStepCate>();
	
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
	public List<FirstStepCate> getChildren() {
		return children;
	}
	public void setChildren(List<FirstStepCate> children) {
		this.children = children;
	}
}
