package com.baixing.entity;

import java.io.Serializable;
import java.util.List;

public class Filterss implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private String displayName;
	private String unit;
	private String controlType;
	private List<values> valuesList;
	private List<labels> labelsList;
	private String numeric;
	private String required;
	private int levelCount = 0;
	
	public void setLevelCount(int levelCount){
		this.levelCount = levelCount;
	}
	
	public int getLevelCount(){
		return levelCount;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
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

	public List<values> getValuesList() {
		return valuesList;
	}

	public void setValuesList(List<values> valuesList) {
		this.valuesList = valuesList;
	}

	public List<labels> getLabelsList() {
		return labelsList;
	}

	public void setLabelsList(List<labels> labelsList) {
		this.labelsList = labelsList;
	}

	public String getNumeric() {
		return numeric;
	}

	public void setNumeric(String numeric) {
		this.numeric = numeric;
	}

	public String getRequired() {
		return required;
	}

	public void setRequired(String required) {
		this.required = required;
	}

	@Override
	public String toString() {
		return "Filterss [name=" + name + ", displayName=" + displayName
				+ ", unit=" + unit + ", controlType=" + controlType
				+ ", valuesList=" + valuesList + ", labelsList=" + labelsList
				+ ", numeric=" + numeric + ", required=" + required + "]";
	}

}
