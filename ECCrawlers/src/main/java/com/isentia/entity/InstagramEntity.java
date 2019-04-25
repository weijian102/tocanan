package com.isentia.entity;

import java.util.Date;

public class InstagramEntity {

	private Date postDate;
	private String content;
	private String url;
	private String type;
	private long likeNo;
	private long commentNo;
	private long viewNo;
	private String keyword;
	private String language;
	private String profileName;
	
	
	public String getProfileName() {
		return profileName;
	}
	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}
	public Date getPostDate() {
		return postDate;
	}
	public void setPostDate(Date postDate) {
		this.postDate = postDate;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public long getLikeNo() {
		return likeNo;
	}
	public void setLikeNo(long likeNo) {
		this.likeNo = likeNo;
	}
	public long getCommentNo() {
		return commentNo;
	}
	public void setCommentNo(long commentNo) {
		this.commentNo = commentNo;
	}
	public long getViewNo() {
		return viewNo;
	}
	public void setViewNo(long viewNo) {
		this.viewNo = viewNo;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	
	
	
}
