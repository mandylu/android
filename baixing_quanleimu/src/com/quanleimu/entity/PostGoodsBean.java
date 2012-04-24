package com.quanleimu.entity;

import java.io.Serializable;
import java.util.List;

public class PostGoodsBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String unit;
	private String controlType;
	private int numeric;
	private String required;
	private String displayName;
	private String name;
	private List<String> labels;
	private List<String> values;
	
	
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
	@Override
	public String toString() {
		return "PostGoodsBean [unit=" + unit + ", controlType=" + controlType
				+ ", numeric=" + numeric + ", required=" + required
				+ ", displayName=" + displayName + ", name=" + name
				+ ", labels=" + labels + ", values=" + values + "]";
	}
	
	
	
}
