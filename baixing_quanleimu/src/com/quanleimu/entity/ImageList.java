package com.quanleimu.entity;

import java.io.Serializable;

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
	public void setSmall(String small) {
		this.small = small;
	}
	public String getBig() {
		return big;
	}
	public void setBig(String big) {
		this.big = big;
	}
	public String getResize180() {
		return resize180;
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
