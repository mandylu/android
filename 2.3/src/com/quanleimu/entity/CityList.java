package com.quanleimu.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CityList implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	List<CityDetail> listDetails = new ArrayList<CityDetail>();

	public List<CityDetail> getListDetails() {
		return listDetails;
	}

	public void setListDetails(List<CityDetail> listDetails) {
		this.listDetails = listDetails;
	}

	

}
