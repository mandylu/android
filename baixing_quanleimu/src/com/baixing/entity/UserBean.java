package com.baixing.entity;

import java.io.Serializable;

import com.baixing.network.NetworkUtil;

public class UserBean implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String id;
	private String phone;
	private String password;
	
	public String getPassword() {
		return password;
	}
	public void setPassword(String password, boolean raw) {
		this.password = raw ? NetworkUtil.getMD5(password) : password;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	@Override
	public String toString() {
		return "UserBean [id=" + id + ", phone=" + phone + ", password="
				+ password + "]";
	}
}
