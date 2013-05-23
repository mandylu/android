package com.baixing.entity;

public class Quota extends Object{
	private boolean outOfQuota;
	private int limit;
	private int used;
	private String message;
	private String type;
	
	public Quota(boolean outOfQuota, int limit, int used, String message, String type){
		this.outOfQuota = outOfQuota;
		this.limit = limit;
		this.used = used;
		this.message = message;
		this.type = type;
	}
	
	public boolean isOutOfQuota(){
		return outOfQuota;
	}
	
	public int getLimit(){
		return limit;
	}
	
	public int getUsed(){
		return used;		
	}
	
	public String getMessage(){
		return message;
	}
	
	public String getType(){
		return type;
	}
}