package com.isentia.ec;

import java.net.URLEncoder;
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
import org.jsoup.nodes.Element;

import com.isentia.dao.ECDAO;
import com.isentia.entity.ECSKU;
import com.isentia.util.ECUtil;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

public class RakutenSKUCrawlers {
	final static Logger logger = Logger.getLogger(RakutenSKUCrawlers.class);
	public static void main (String [] args)throws Exception{
		
		TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}

					public void checkClientTrusted(X509Certificate[] certs, String authType) {  }

					public void checkServerTrusted(X509Certificate[] certs, String authType) {  }

				}
		};

		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		// Install the all-trusting host verifier
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		
		
		RakutenSKUCrawlers rakutenCrawlers = new RakutenSKUCrawlers();
		ECDAO ecdao = null;
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
		}catch(Exception ec){
			ec.printStackTrace();
		}
		try{
			ArrayList <ECSKU> skuList = rakutenCrawlers.crawlData(args[0], 30L);
			logger.debug(skuList.size() + " : SKU Crawled");
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
	
	public ArrayList<ECSKU> crawlData(String keyword,long channelId){
		ArrayList<ECSKU> ecskuList = new ArrayList<ECSKU>();
		for(int page = 1; page<100;page++){
			try {
				String urlCrawl = "https://search.rakuten.co.jp/search/mall/"+URLEncoder.encode(keyword)+"/?p="+page;
				logger.debug("Url -->" + urlCrawl);
				Document doc = null;
				doc = Jsoup.connect(urlCrawl).timeout(0).get();
				int pageCount = 0;
				for(Element e:doc.select("div.dui-card.searchresultitem")){
					ECSKU ecsku = new ECSKU();
					ecsku.setUrl(e.select("div.content.title").select("a").attr("href"));
					ecsku.setContent(e.select("div.content.title").select("a").attr("title"));
					String itemCode = e.select("div.content.review").select("a").attr("href");
					itemCode = itemCode.replace("https://review.rakuten.co.jp/item/1/", "");
					itemCode = itemCode.replace("/1.1/","");
					String price = e.select("div.content.description.price").select("span.important").text();
					price = price.replaceAll("円", "");
					price = price.replaceAll(",", "");
					ecsku.setProductId(itemCode);
					ecsku.setChannelId(channelId);
					ecsku.setSearchTerm(keyword);
					if(ECUtil.checkIsValidProduct(keyword, e.select("div.content.title").select("a").attr("title"))){
						if(itemCode.length()>0){
							price = price.trim();
							price = price.replaceAll(" ", "");
							double dPrice = Double.parseDouble(price.trim());
							logger.debug("The Price is:" + dPrice);
							ecsku.setPrice(dPrice);
							ecskuList.add(ecsku);
						}else{
							logger.debug("This is Rejected Content With No Review: " + e.getElementsByClass("rsrSResultItemTxt").text());
						}
					}else{
						if(logger.isDebugEnabled()){
							logger.debug("This is Rejected Content: " + e.getElementsByClass("rsrSResultItemTxt").text());
						}
					}
					pageCount++;
				}
			}catch(Exception e) {
				e.printStackTrace();
				return ecskuList;
			}
		}
		return ecskuList;
	}
}
