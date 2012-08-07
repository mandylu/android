package com.quanleimu.entity;

import java.io.Serializable;
import java.util.List;

public class SaveFirstStepCate implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<FirstStepCate> list;
	private Long time;
	public List<FirstStepCate> getList() {
		return list;
	}
	public void setList(List<FirstStepCate> list) {
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
		return "SaveFirstStepCate [list=" + list + ", time=" + time + "]";
	}
	
}
