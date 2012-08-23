package com.quanleimu.entity;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author liuchong
 *
 */
public class ChatMessage implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2495827266364235867L;
	
	private String from;//u_id_from
	private String to;//u_id_to
	private String id;//id
	private String senderNick;//u_nick_from
	private String receiverNick; //u_nick_to
	private String adId;//ad_id
	private long timestamp;//timestamp
	private String adTitle;//ad_title
	private String message;//message
	private String session;//session_id
	
	public ChatMessage()
	{
		
	}
	
	public static ChatMessage fromJson(String json)
	{
		try {
			JSONObject obj = new JSONObject(json);
			return fromJson(obj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return new ChatMessage();
	}
	
	public static ChatMessage fromJson(JSONObject obj)
	{
		ChatMessage chatMsg = new ChatMessage();
		try {
			chatMsg.setAdId(obj.getString("ad_id"));
			chatMsg.setFrom(obj.getString("u_id_from"));
			chatMsg.setTo(obj.getString("u_id_to"));
			chatMsg.setId(obj.getString("id"));
			chatMsg.setSenderNick(obj.getString("u_nick_from"));
			chatMsg.setReceiverNick(obj.getString("u_nick_to"));
			chatMsg.setTimestamp(obj.getLong("timestamp"));
			chatMsg.setAdTitle(obj.getString("ad_title"));
			chatMsg.setMessage(obj.getString("message"));
			chatMsg.setSession(obj.getString("session_id"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return chatMsg;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSenderNick() {
		return senderNick;
	}

	public void setSenderNick(String senderNick) {
		this.senderNick = senderNick;
	}

	public String getReceiverNick() {
		return receiverNick;
	}

	public void setReceiverNick(String receiverNick) {
		this.receiverNick = receiverNick;
	}

	public String getAdId() {
		return adId;
	}

	public void setAdId(String adId) {
		this.adId = adId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getAdTitle() {
		return adTitle;
	}

	public void setAdTitle(String adTitle) {
		this.adTitle = adTitle;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}
	
	
}
