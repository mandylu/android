package com.quanleimu.entity;

import java.io.Serializable;

import org.jivesoftware.smack.util.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author liuchong
 *
 */
public class ChatMessage implements Serializable, Cloneable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2495827266364235867L;
	
	private String from;//u_id_from
	private String to;//u_id_to
	private String id;//id
	private String adId;//ad_id
	private long timestamp;//timestamp
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
			if (obj.has("ad_id"))
			{
				chatMsg.setAdId(obj.getString("ad_id"));
			}
			chatMsg.setFrom(obj.getString("u_id_from"));
			chatMsg.setTo(obj.getString("u_id_to"));
			chatMsg.setId(obj.getString("id"));
			chatMsg.setTimestamp(obj.getLong("timestamp"));
			chatMsg.setMessage(obj.getString("message"));
			chatMsg.setSession(obj.getString("session_id"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return chatMsg;
	}
	
	public String toJson()
	{
		JSONObject obj = new JSONObject();
		try
		{
			if (adId != null)
			{
				obj.put("ad_id", getAdId());
			}
			obj.put("u_id_from", getFrom());
			obj.put("u_id_to", getTo());
			obj.put("id", getId());
			obj.put("timestamp", "" + getTimestamp());
			obj.put("message", getMessage());
			obj.put("session_id", getSession());
		}
		catch(Throwable t)
		{
			
		}
		
		return obj.toString();
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
