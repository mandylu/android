package com.baixing.entity;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ImageList implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String square;
	private String small;
	private String big;
	private String resize180;
	public String getSquare() {
		return square;
	}
	public void setSquare(String square) {
		this.square = square;
	}
	public String getSmall() {
		return small;
	}
	
	@JsonIgnore
	public String[] getSmallArray() {
		return parseForArray(small);
	}
	
	public void setSmall(String small) {
		this.small = small;
	}
	
	public String getBig() {
		return big;
	}
	
	@JsonIgnore
	public String[] getBigArray() {
		return parseForArray(big);
	}
	
	private String[] parseForArray(String str) {
		if (str != null) {
			String[] list = str.split(",");
			return list;
		}
		
		return null;
	}
	
	public void setBig(String big) {
		this.big = big;
	}
	
	public String getResize180() {
		return resize180;
	}
	
	@JsonIgnore
	public String[] getResize180Array() {
		return parseForArray(resize180);
	}
	
	public void setResize180(String resize180) {
		this.resize180 = resize180;
	}
	@Override
	public String toString() {
		return "ImageList [square=" + square + ", small=" + small + ", big="
				+ big + ", resize180=" + resize180 + "]";
	}
	
}
