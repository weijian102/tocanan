package com.isentia.entity;

import java.util.Date;

public class ECComments {

	public String productId;
	public String url;
	public long channelId;
	public Date datetimePost;
	public String content;
	public int rating;
	public String voiceName;
	
	public String getProductId() {
		return productId;
	}
	public void setProductId(String productId) {
		this.productId = productId;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public long getChannelId() {
		return channelId;
	}
	public void setChannelId(long channelId) {
		this.channelId = channelId;
	}
	public Date getDatetimePost() {
		return datetimePost;
	}
	public void setDatetimePost(Date datetimePost) {
		this.datetimePost = datetimePost;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getRating() {
		return rating;
	}
	public void setRating(int rating) {
		this.rating = rating;
	}
	public String getVoiceName() {
		return voiceName;
	}
	public void setVoiceName(String voiceName) {
		this.voiceName = voiceName;
	}
	
	
}
