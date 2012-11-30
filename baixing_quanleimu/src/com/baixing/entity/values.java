package com.baixing.entity;

import java.io.Serializable;

public class values implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "values [value=" + value + "]";
	}

}
