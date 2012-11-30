package com.baixing.entity;

public class HotData {

	public String keyword;
	public String title;
	public String weburl;
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getWeburl() {
		return weburl;
	}
	public void setWeburl(String weburl) {
		this.weburl = weburl;
	}
	@Override
	public String toString() {
		return "HotData [keyword=" + keyword + ", title=" + title + ", weburl="
				+ weburl + "]";
	}
	
}

