package com.isentia.ec;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.isentia.dao.ECDAO;
import com.isentia.entity.ECSKU;
import com.isentia.util.ECUtil;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

public class AdayroiSKUCrawlers {
	final static Logger logger = Logger.getLogger(AdayroiSKUCrawlers.class);
	public static void main (String [] args){
		AdayroiSKUCrawlers ada = new AdayroiSKUCrawlers();
		ECDAO ecdao = null;
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
		}catch(Exception ec){
			ec.printStackTrace();
		}
		try{
			ArrayList <ECSKU> skuList = ada.crawlData("bobby", 12L);
			for(ECSKU ec: skuList){
				try{
					ecdao.insertIntoSKU(ec);
				}catch(MySQLIntegrityConstraintViolationException e){
					e.printStackTrace();
					if(logger.isDebugEnabled()){
						logger.debug("Duplicated Entry" + ec.getUrl());
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public ArrayList<ECSKU> crawlData(String keyword,long channelId) throws Exception{
		ArrayList<ECSKU> ecskuList = new ArrayList<ECSKU>();
		String unicode = ECUtil.convertUnicode(keyword);
		for(int page = 1; page<=1;page++){
			String urlCrawl = "https://www.adayroi.com/ta-giay-cho-be-c769?p="+page+"&brand=2642";
			logger.debug("Url -->" + urlCrawl);
			Document doc = Jsoup.connect(urlCrawl).ignoreContentType(true).ignoreHttpErrors(true).userAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36").referrer("http://www.yahoo.com").timeout(10000).get();
			int count= 0;
			for(Element ele: doc.select("div.col-lg-3.col-xs-4")){
				ECSKU ecsku = new ECSKU();
				String url  = "https://www.adayroi.com"+ele.select("h4.post-title").select("a").attr("href");
				logger.debug(url);
				ecsku.setUrl(url);			
				ecsku.setContent(ele.select("h4.post-title").text());
				logger.debug(ele.select("h4.post-title").text());
				String price =ele.getElementsByClass("amount-1").text();
				price = price.replaceAll("đ","");
				price = price.replaceAll("\\.","");
				logger.debug(price);
				ecsku.setPrice(Double.parseDouble(price));
				ecsku.setChannelId(channelId);
				ecsku.setSearchTerm(keyword);
				logger.debug(ele.select("div.adr-coupon").attr("data-pid"));
				ecsku.setProductId(ele.select("div.adr-coupon").attr("data-pid"));
				ecskuList.add(ecsku);
				count++;
			}
		}
		return ecskuList;
	}
}
