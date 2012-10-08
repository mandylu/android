package com.quanleimu.entity;

import java.io.Serializable;
import java.util.List;

public class GoodsList implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2158869923050057462L;
	private int count = 0;
	
	public GoodsList(){}
	
	public GoodsList(List<GoodsDetail> data__){
		data = data__;
		if(null != data)
			count = data.size();
	}
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public List<GoodsDetail> getData() {
		return data;
	}
	public void setData(List<GoodsDetail> data) {
		this.data = data;
	}
	private List<GoodsDetail> data;
	
}
