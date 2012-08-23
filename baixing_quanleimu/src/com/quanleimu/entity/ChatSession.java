package com.quanleimu.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author xumengyi
 */
public class ChatSession implements Serializable
{
	private static final long serialVersionUID = -2300264543803637316L;
	
	private String sessionId;//id
	private String oppositeId;
	private String oppositeNick;
	private String adId;//ad_id
	private String adTitle;//ad_title
	private int count;
	private String lastMsg;
	private String timeStamp;
	private String imageUrl;
	
	public static List<ChatSession> fromJson(String json)
	{
		
		if(json == null || json.equals("")) return null;
		try {
			JSONObject obj = new JSONObject(json);
			if(obj != null){
				if(obj.has("data")){
					JSONArray sessions = obj.getJSONArray("data");
					
					if(sessions != null && sessions.length() > 0){
						List<ChatSession> sessionList = new ArrayList<ChatSession>();
						for(int i = 0; i < sessions.length(); ++ i){
							JSONObject session = sessions.getJSONObject(i);
							if(session == null) continue;
							ChatSession sc = new ChatSession();
							if(session.has("session_id")){
								sc.setSessionId(session.getString("session_id"));
							}
							if(session.has("u_id_other")){
								sc.setOppositeId(session.getString("u_id_other"));
							}
							if(session.has("u_nick_other")){
								sc.setOppositeNick(session.getString("u_nick_other"));
							}
							if(session.has("ad_id")){
								sc.setAdId(session.getString("ad_id"));
							}
							if(session.has("ad_title")){
								sc.setAdTitle(session.getString("ad_title"));
							}
							if(session.has("count")){
								sc.setCount(session.getInt("count"));
							}
							if(session.has("lastmessage")){
								sc.setLastMsg(session.getString("lastmessage"));
							}
							if(session.has("timestamp")){
								sc.setTimeStamp(String.valueOf(session.getLong("timestamp")));
							}
							if(session.has("u_image_other")){
								sc.setImageUrl(session.getString("u_image_other"));
							}
							sessionList.add(sc);
						}
						return sessionList;
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public String getImageUrl(){
		return imageUrl;
	}
	
	public void setImageUrl(String url){
		imageUrl = url;
	}
	
	public String getLastMsg(){
		return lastMsg;
	}
	
	public void setLastMsg(String msg){
		lastMsg = msg;
	}
	
	public String getTimeStamp(){
		return timeStamp;
	}
	
	public void setTimeStamp(String time){
		timeStamp = time;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String id) {
		this.sessionId = id;
	}

	public String getOppositeId() {
		return oppositeId;
	}

	public void setOppositeId(String id) {
		this.oppositeId = id;
	}

	public String getOppositeNick() {
		return oppositeNick;
	}

	public void setOppositeNick(String nick) {
		this.oppositeNick = nick;
	}

	public String getAdId() {
		return adId;
	}

	public void setAdId(String id) {
		this.adId = id;
	}
	
	public void setAdTitle(String title){
		this.adTitle = title;
	}
	
	public String getAdTitle(){
		return this.adTitle;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
