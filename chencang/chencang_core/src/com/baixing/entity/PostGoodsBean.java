package com.baixing.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class PostGoodsBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String unit;
	private String controlType;
	private int numeric;
	private int maxlength;
	private String required;
	private String displayName;
	private String name;
	private List<String> labels;
	private List<String> values;
	private HashMap<String, String> lvmap;
	private String subMeta;
	private int levelCount;
	private String defaultValue;
	
	public void setLevelCount(int count){
		levelCount = count;		
	}
	
	public int getLevelCount(){
		return levelCount;
	}
	
	public void setSubMeta(String sm){
		subMeta = sm;
	}
	
	public String getSubMeta(){
		return subMeta;
	}
	
	public int getMaxlength() {
		return maxlength;
	}

	public void setMaxlength(int maxlength) {
		this.maxlength = maxlength;
	}

	public String getUnit() {
		return unit;
	}
	public void setUnit(String unit) {
		this.unit = unit;
	}
	public String getControlType() {
		return controlType;
	}
	public void setControlType(String controlType) {
		this.controlType = controlType;
	}
	public int getNumeric() {
		return numeric;
	}
	public void setNumeric(int numeric) {
		this.numeric = numeric;
	}
	public String getRequired() {
		return required;
	}
	public void setRequired(String required) {
		this.required = required;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getLabels() {
		return labels;
	}
	public void setLabels(List<String> labels) {
		this.labels = labels;
	}
	public List<String> getValues() {
		return values;
	}
	public void setValues(List<String> values) {
		this.values = values;
	}
	
	public String getDefaultValue() {
		return defaultValue;
	}
	
	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
	
	public HashMap<String, String> getLvmap() {
		this.lvmap = new LinkedHashMap<String, String>();
		if(labels!=null && labels.size()!=0){
			for(int i=0;i<labels.size();i++){
				this.lvmap.put(labels.get(i), values.get(i));
			}
		}
		return this.lvmap;
	}
	
	@Override
	public String toString() {
		return "PostGoodsBean [unit=" + unit + ", controlType=" + controlType
				+ ", numeric=" + numeric + ", required=" + required
				+ ", displayName=" + displayName + ", name=" + name
				+ ", subMeta=" + subMeta
				+ ", labels=" + labels + ", values=" + values + ", lvmap="
				+ getLvmap() + "]";
	}
	
	
	
}
