package com.isentia.entity;

import java.util.HashMap;

public class AppleLayer {

	private String keyword;
	private HashMap<String,String> layer2KeywordMap;
	
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public HashMap<String, String> getLayer2KeywordMap() {
		return layer2KeywordMap;
	}
	public void setLayer2KeywordMap(HashMap<String, String> layer2KeywordMap) {
		this.layer2KeywordMap = layer2KeywordMap;
	}
	
	
}
