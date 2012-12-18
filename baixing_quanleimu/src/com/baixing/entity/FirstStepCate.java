package com.baixing.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FirstStepCate extends Object implements Serializable {
	private static final long serialVersionUID = 1L;
	public String name;
	public String englishName;
	public List<SecondStepCate> children = new ArrayList<SecondStepCate>();
	public String parentEnglishName = "";
	
	
	public String getParentEnglishName() {
		return parentEnglishName;
	}
	public void setParentEnglishName(String parentEnglishName) {
		this.parentEnglishName = parentEnglishName;
	}
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
	public List<SecondStepCate> getChildren() {
		return children;
	}
	public void setChildren(List<SecondStepCate> children) {
		this.children = children;
	}
	@Override
	public String toString() {
//		return "FirstStepCate [name=" + name + ", englishName=" + englishName
//				+ ", children=" + children + ", parentEnglishName="
//				+ parentEnglishName + "]";
		return name;
	}
	
	
}
