package com.quanleimu.entity;

import java.io.Serializable;

public class labels implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String label;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return "label [label=" + label + "]";
	}

}
