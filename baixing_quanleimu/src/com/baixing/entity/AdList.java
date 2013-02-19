package com.baixing.entity;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class AdList implements Serializable {
	private static final long serialVersionUID = -2158869923050057462L;
	private int count = 0;
	public AdList(){}
	public AdList(List<Ad> data__){
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
	public List<Ad> getData() {
		return data;
	}
	public void setData(List<Ad> data) {
		this.data = data;
	}
	private List<Ad> data;
	
	public Object clone(){
		List<Ad> temp = new ArrayList<Ad>();
		temp.addAll(data);
		return new AdList(temp);
	}
}
