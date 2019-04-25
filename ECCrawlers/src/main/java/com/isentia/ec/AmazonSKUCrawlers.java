package com.isentia.ec;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.isentia.dao.ECDAO;
import com.isentia.entity.ECSKU;
import com.isentia.util.ECUtil;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;


public class AmazonSKUCrawlers {
	final static Logger logger = Logger.getLogger(AmazonSKUCrawlers.class);
	public static void main (String [] args) throws Exception{
		AmazonSKUCrawlers amzonCrawler = new AmazonSKUCrawlers();
		ECDAO ecdao = null;
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
		}catch(Exception ec){
			ec.printStackTrace();
		}
		try{
			ArrayList <ECSKU> skuList = amzonCrawler.crawlData(args[0], 34L);
			for(ECSKU ec: skuList){
				try{
					ecdao.insertIntoSKU(ec);
				}catch(MySQLIntegrityConstraintViolationException e){
					if(logger.isDebugEnabled()){
						logger.debug("Duplicated Entry");
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public ArrayList<ECSKU> crawlData(String keyword,long channelId) throws Exception{
		ArrayList<ECSKU> ecskuList = new ArrayList<ECSKU>();
		for(int productFlip =1;productFlip<=20;productFlip++){
			Document doc = Jsoup.connect("https://www.amazon.co.jp/s/page="+productFlip+"&keywords="+keyword+"&ie=UTF8").ignoreContentType(true).ignoreHttpErrors(true).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A").referrer("http://www.yahoo.com").timeout(10000).get();
			logger.debug("https://www.amazon.co.jp/s/page="+productFlip+"&keywords="+keyword+"&ie=UTF8");
			Elements ul = doc.select("div.a-row.s-result-list-parent-container > ul"); 
			Elements li = ul.select("li"); // select all li from ul
			for (int i = 0; i < li.size(); i++) {
				ECSKU ecsku = new ECSKU();
				try{
					String url  = "https://www.amazon.co.jp/dp/"+li.get(i).attr("data-asin");
					String itemCode  = li.get(i).attr("data-asin");
					String title  = li.get(i).select("a.a-link-normal.s-access-detail-page.a-text-normal").attr("h2", "data-attribute").text();
					logger.debug(title);
					if(li.get(i).select("span.a-size-base.a-color-price.s-price.a-text-bold").text().length()>0){
						logger.debug(li.get(i).select("span.a-size-base.a-color-price.s-price.a-text-bold"));
						String price  = li.get(i).select("span.a-size-base.a-color-price.s-price.a-text-bold").get(0).text();
						logger.debug(price);
						price = price.replaceAll(",", "");
						price = price.replaceAll("￥", "");
						ecsku.setPrice(Double.parseDouble(price));
						ecsku.setChannelId(channelId);
						ecsku.setSearchTerm(keyword);
						ecsku.setProductId(itemCode);
						ecsku.setUrl(url);			
						ecsku.setContent(title);
						if(ECUtil.checkIsValidProduct(keyword,li.get(i).select("a.a-link-normal.s-access-detail-page.a-text-normal").attr("h2", "data-attribute").text())){
							ecskuList.add(ecsku);
						}else{
							logger.error("Fail to insert the following:" +ecsku.getContent());
						}
					}
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		return ecskuList;
	}
}
