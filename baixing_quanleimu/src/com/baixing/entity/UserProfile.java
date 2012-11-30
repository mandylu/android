package com.baixing.entity;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class UserProfile implements Cloneable, Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5763571391927393475L;
	public String userId = "";
	public String mobile = "";
	public String nickName = "";
	public String gender = "";
	public String hometown = "";
	public String location = "";
	public String detailLocation = "";
	public String qq = "";
	public String createTime = "";
	// public String description = "";
	public String type = "";
	public String squareImage = "";
	public String smallImage = "";
	public String bigImage = "";
	public String resize180Image = "";

	public static UserProfile from(String jsonData) {
		UserProfile up = new UserProfile();
		if (jsonData == null || jsonData.equals(""))
			return up;
		JSONObject jsonObj;
		try {
			jsonObj = new JSONObject(jsonData);
			if (jsonObj.has("id")) {
				up.userId = jsonObj.getString("id");
			}
			if (jsonObj.has("mobile")) {
				up.mobile = jsonObj.getString("mobile");
			}
			if (jsonObj.has("nickname")) {
				up.nickName = jsonObj.getString("nickname");
			}
			if (jsonObj.has("gender")) {
				up.gender = jsonObj.getString("gender");
			}
			if (jsonObj.has("家乡")) {
				up.hometown = jsonObj.getString("家乡");
			}
			if (jsonObj.has("所在地")) {
				up.location = jsonObj.getString("所在地");
			}
			if (jsonObj.has("具体地点")) {
				up.detailLocation = jsonObj.getString("具体地点");
			}
			if (jsonObj.has("qq")) {
				up.qq = jsonObj.getString("qq");
			}
			if (jsonObj.has("createdTime")) {
				up.createTime = jsonObj.getString("createdTime");
			}
			if (jsonObj.has("type")) {
				up.type = jsonObj.getString("type");
			}
			if (jsonObj.has("images")) {
				JSONObject images = jsonObj.getJSONObject("images");
				if (images != null) {
					up.squareImage = images.getString("square");
					up.bigImage = images.getString("big");
					up.smallImage = images.getString("small");
					up.resize180Image = images.getString("resize180");
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
		return up;
	}
}