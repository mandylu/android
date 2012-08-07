package com.quanleimu.entity;

import java.io.Serializable;
import java.util.List;

public class SaveFilterss implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<Filterss> list;
	private Long time;
	public List<Filterss> getList() {
		return list;
	}
	public void setList(List<Filterss> list) {
		this.list = list;
	}
	public Long getTime() {
		return time;
	}
	public void setTime(Long time) {
		this.time = time;
	}
	@Override
	public String toString() {
		return "SaveFilterss [list=" + list + ", time=" + time + "]";
	}
	
}
