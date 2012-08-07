package com.quanleimu.entity;

import java.io.Serializable;

public class PostMu implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String json;
	private Long time;
	public String getJson() {
		return json;
	}
	public void setJson(String json) {
		this.json = json;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	@Override
	public String toString() {
		return "PostMu [json=" + json + ", time=" + time + "]";
	}
	
}
