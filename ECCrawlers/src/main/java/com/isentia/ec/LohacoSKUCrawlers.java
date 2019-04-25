package com.isentia.ec;

import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.isentia.dao.ECDAO;
import com.isentia.ec.base.SKUCrawlers;
import com.isentia.entity.ECSKU;
import com.isentia.util.ECUtil;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

public class LohacoSKUCrawlers implements SKUCrawlers{
	final static Logger logger = Logger.getLogger(LohacoSKUCrawlers.class);
	public static void main (String [] args) throws Exception{
		
		
		LohacoSKUCrawlers lohaCo = new LohacoSKUCrawlers();
		ECDAO ecdao = null;
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
		}catch(Exception ec){
			ec.printStackTrace();
		}
		try{
			ArrayList <ECSKU> skuList = lohaCo.crawlData(args[0], 31L);
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
		ArrayList<ECSKU> ecList = new ArrayList<ECSKU>();
			for(int page = 1; page<20;page++){
			String urlCrawl = "https://lohaco.jp/ksearch/?searchWord="+keyword+"&dgm=lk&resultCount=100&page="+page;
			Document doc = Jsoup.connect(urlCrawl).ignoreContentType(true).ignoreHttpErrors(true).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A").referrer("http://www.yahoo.com").timeout(0).get();
			logger.debug(doc);
			Elements ul = doc.select("ul.lineupItemList"); 
			Elements li = ul.select("li");
//			logger.debug(li.size());
			for (int i = 0; i < li.size(); i++) {
				if(li.get(i).select("p.itemName").text().trim().length()>0){
					ECSKU ecsku = new ECSKU();
					ecsku.setUrl("https://lohaco.jp" + li.get(i).getElementsByClass("itemName").select("a").attr("href"));
					ecsku.setProductId(li.get(i).getElementsByClass("itemName").select("a").attr("href").replaceAll("/product/", "").replaceAll("/\\?int_id=search_keywordsearch", ""));
					ecsku.setContent(li.get(i).getElementsByClass("itemName").text());
					ecsku.setChannelId(channelId);
					ecsku.setSearchTerm(keyword);
					ecsku.setPrice(Double.parseDouble(li.get(i).getElementsByClass("price").get(0).text().replace("￥","").replace(",", "").replaceAll("（税込）", "")));
					if(ECUtil.checkIsValidProduct(keyword,li.get(i).getElementsByClass("itemName").text())){
						ecList.add(ecsku);
					}else{
						logger.error("Fail to insert the following:" +ecsku.getUrl());
					}
				}
			}
		}
		return ecList;
	}
}
