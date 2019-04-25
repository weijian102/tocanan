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

public class TikiSKUCrawlers {
	final static Logger logger = Logger.getLogger(TikiSKUCrawlers.class);
	public static void main (String [] args){
		TikiSKUCrawlers ada = new TikiSKUCrawlers();
		ECDAO ecdao = null;
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
		}catch(Exception ec){
			ec.printStackTrace();
		}
		try{
			ArrayList <ECSKU> skuList = ada.crawlData("bobby", 13L);
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
			String urlCrawl = "https://tiki.vn/ta-bim-cho-be/c2551/bobby?limit=100";
			logger.debug("Url -->" + urlCrawl);
			Document doc = Jsoup.connect(urlCrawl).ignoreContentType(true).ignoreHttpErrors(true).userAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36").referrer("http://www.yahoo.com").timeout(10000).get();
			for(Element d: doc.getElementsByClass("product-item")){
				ECSKU ecsku = new ECSKU();
				ecsku.setProductId(d.select("a").attr("data-id"));
				ecsku.setUrl(d.select("a").attr("href"));
				ecsku.setContent(d.select("a").attr("title"));
				ecsku.setPrice(Double.parseDouble(d.attr("data-price")));
				ecsku.setChannelId(channelId);
				ecsku.setSearchTerm(keyword);
				ecskuList.add(ecsku);
			}
		}
		return ecskuList;
	}
}
