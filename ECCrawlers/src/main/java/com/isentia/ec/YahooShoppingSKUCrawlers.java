package com.isentia.ec;

import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.isentia.dao.ECDAO;
import com.isentia.ec.base.SKUCrawlers;
import com.isentia.entity.ECSKU;
import com.isentia.util.ECUtil;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

public class YahooShoppingSKUCrawlers implements SKUCrawlers{
	final static Logger logger = Logger.getLogger(YahooShoppingSKUCrawlers.class);
	public static void main (String [] args){
		YahooShoppingSKUCrawlers yahooShop = new YahooShoppingSKUCrawlers();
		ECDAO ecdao = null;
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
		}catch(Exception ec){
			ec.printStackTrace();
		}
		try{
			ArrayList <ECSKU> skuList = yahooShop.crawlData(args[0], 33L);
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
		logger.debug(URLEncoder.encode(keyword));
		ArrayList<ECSKU> ecList = new ArrayList<ECSKU>();
		for(int pageView=1;pageView<=30;pageView++){
			Document doc = null;
			if(pageView!=1){
				String crawlURL = "http://search.shopping.yahoo.co.jp/search?p="+URLEncoder.encode(keyword)+"&n=100&tab_ex=commerce&uIv=on&used=0&seller=0&ei=UTF-8&xargs="+pageView+"&b="+((pageView-1)*100+1);
				logger.debug("CrawlURl : " + crawlURL);
				doc = Jsoup.connect(crawlURL).ignoreHttpErrors(true).userAgent("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.19) Gecko/20081217 KMLite/1.1.2").referrer("http://www.yahoo.com").timeout(0).get();
			}else{
				logger.debug("http://search.shopping.yahoo.co.jp/search?p="+keyword+"&n=100&tab_ex=commerce&uIv=on&used=0&seller=0&ei=UTF-8");
				doc = Jsoup.connect("http://search.shopping.yahoo.co.jp/search?p="+URLEncoder.encode(keyword)+"&n=100&tab_ex=commerce&uIv=on&used=0&seller=0&ei=UTF-8").ignoreHttpErrors(true).userAgent("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.19) Gecko/20081217 KMLite/1.1.2").referrer("http://www.yahoo.com").timeout(0).get();
			}
			Elements ul = doc.select("div.elList > ul"); 
			Elements li = ul.select("li"); // select all li from ul
			for (int i = 0; i < li.size(); i++) {
				ECSKU ecsku = new ECSKU();
				String url = li.get(i).select("dd.elName").select("h3").select("a").attr("href");
				String title = li.get(i).select("dd.elName").select("h3").select("a").select("span").text();
				String price = "";
				if(li.get(i).select("dd.elPrice").select("p").select("span").size()>1){
					price = li.get(i).select("dd.elPrice").select("p").select("span").get(0).text();
					ecsku.setUrl(url);
		    		ecsku.setContent(title);
					ecsku.setPrice(Double.parseDouble(price.replaceAll(",", "")));
					ecsku.setSearchTerm(keyword);
					ecsku.setProductId(li.get(i).select("div.elReview").select("a").attr("href"));
					ecsku.setChannelId(channelId);
					if(ECUtil.checkIsValidProduct(keyword,title)){
						if(url.contains("yahoo")){
							ecList.add(ecsku);
						}else{
							logger.error("Following not from yahoo:" +ecsku.getUrl());
						}
					}else{
						logger.error("Fail to insert the following:" +ecsku.getUrl());
					}
				}
	    
			}
		}
		return ecList;
	}
}
