package com.isentia.ec;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.isentia.dao.ECDAO;
import com.isentia.entity.ECSKU;
import com.isentia.util.ECUtil;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;


public class AuctionKRSKYUCrawlers {

	final static Logger logger = Logger.getLogger(AuctionKRSKYUCrawlers.class);
	public static void main (String [] args){
		AuctionKRSKYUCrawlers gmarketSKU = new AuctionKRSKYUCrawlers();
		ECDAO ecdao = null;
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
		}catch(Exception ec){
			ec.printStackTrace();
		}
		try{
			ArrayList <ECSKU> skuList = gmarketSKU.crawlData("팸퍼스", 9L);
			logger.debug("Total SKU Crawled: " + skuList.size());
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
		for(int page = 0; page<1;page++){
			String urlCrawl = "http://stores.auction.co.kr/cnlglobal2/List?Category=09190000&CategoryType=General&SortType=AuctionRank&DisplayType=SmallImage&Page=0&PageIndex=0&PageSize=60&IsFreeShipping=False&HasDiscount=False&HasStamp=False&MinPrice=18700&MaxPrice=73800";
			logger.debug("Url -->" + urlCrawl);
			Document doc = Jsoup.connect(urlCrawl).ignoreContentType(true).ignoreHttpErrors(true).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A").referrer("http://www.yahoo.com").timeout(0).get();
			Elements ul = doc.select("div.prod_list > ul"); 
			Elements li = ul.select("li");
			for (int i = 0; i < li.size(); i++) {
				ECSKU ecsku = new ECSKU();
				List<org.apache.http.NameValuePair> params = URLEncodedUtils.parse(new URI(li.get(i).getElementsByClass("prd_name").select("a").attr("href")), "UTF-8");
				for (org.apache.http.NameValuePair param : params) {
				  if(param.getName().equals("itemno")){
					  logger.debug(param.getValue());
					  ecsku.setProductId(param.getValue());
				  }
				}
				ecsku.setUrl(li.get(i).getElementsByClass("prd_name").select("a").attr("href"));
				ecsku.setContent(li.get(i).getElementsByClass("prd_name").select("a").text());
				String value = li.get(i).getElementsByClass("prd_price").select("strong").text();
				value = value.replace(",", "");
				value = value.replace("원", "");
				ecsku.setPrice(Double.parseDouble(value));
				ecsku.setChannelId(channelId);
				ecsku.setSearchTerm(keyword);
//				ecskuList.add(ecsku);
				if(ECUtil.checkIsValidProduct(keyword,li.get(i).getElementsByClass("prd_name").text())){
					ecskuList.add(ecsku);
				}else{
					logger.error("Fail to insert the following:" +ecsku.getUrl());
					logger.error("Fail to insert the following:" +ecsku.getContent());
				}
			}
		}
		return ecskuList;
	}
}
