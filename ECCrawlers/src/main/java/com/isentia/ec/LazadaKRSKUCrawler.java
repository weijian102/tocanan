package com.isentia.ec;

import java.net.URLDecoder;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.isentia.dao.ECDAO;
import com.isentia.entity.ECSKU;
import com.isentia.util.ECUtil;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

public class LazadaKRSKUCrawler {
	final static Logger logger = Logger.getLogger(LazadaKRSKUCrawler.class);
	public static void main (String [] args){
		LazadaKRSKUCrawler lazada = new LazadaKRSKUCrawler();
		ECDAO ecdao = null;
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
		}catch(Exception ec){
			ec.printStackTrace();
		}
		try{
			ArrayList <ECSKU> skuList = lazada.crawlData("bobby", 11L);
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
		for(int page = 1; page<=7;page++){
			String urlCrawl = "http://www.lazada.vn/ta-giay/"+keyword+"/?spm=a2o4n.category-180303000000.0.0.lQv9P4&page="+page+"&searchredirect=diapers";
			logger.debug("Url -->" + urlCrawl);
			Document doc = Jsoup.connect(urlCrawl).ignoreContentType(true).ignoreHttpErrors(true).userAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36").referrer("http://www.yahoo.com").timeout(10000).get();
			int count= 0;
			System.out.println(doc.getElementsByClass("c-product-card__description").size());
			System.out.println(doc.getElementsByClass("c-product-card__price").size());
			for(Element ele: doc.getElementsByClass("c-product-card__description")){
				ECSKU ecsku = new ECSKU();
				String url  = "http://www.lazada.vn"+ele.select("a").attr("href");
				ecsku.setUrl(url);			
				ecsku.setContent(ele.text());
				String price =doc.getElementsByClass("c-product-card__price-final").get(count).text();
				price = price.replaceAll("VND","");
				price = price.replaceAll("\\.","");
				ecsku.setPrice(Double.parseDouble(price));
				ecsku.setChannelId(channelId);
				ecsku.setSearchTerm(keyword);
				Document doc2 = Jsoup.connect(url).timeout(0).get();
				String productId = doc2.getElementsByClass("selection_title").select("span").text();
				if(productId.length()<1){
					productId = doc2.getElementsByClass("specification-table__value").text().substring(0,doc2.getElementsByClass("specification-table__value").text().indexOf("-"));
				}
				ecsku.setProductId(productId);
				logger.debug(ecsku.toString());
				ecskuList.add(ecsku);
//				if(ECUtil.checkIsValidProduct(keyword,ele.getElementsByClass("itemnameN").text())){
//					ecskuList.add(ecsku);
//				}else{
//					logger.error("Fail to insert the following:" +ecsku.getContent());
//				}
				count++;
			}
		}
		return ecskuList;
	}
}
