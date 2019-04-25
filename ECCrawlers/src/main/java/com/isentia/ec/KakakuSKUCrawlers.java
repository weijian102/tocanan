package com.isentia.ec;

import java.io.File;
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
import com.isentia.ec.base.SKUCrawlers;
import com.isentia.entity.ECSKU;
import com.isentia.util.ECUtil;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

public class KakakuSKUCrawlers implements SKUCrawlers{
	final static Logger logger = Logger.getLogger(KakakuSKUCrawlers.class);
	public static void main (String [] args) throws Exception {
		
		KakakuSKUCrawlers kakaKu = new KakakuSKUCrawlers();
		ECDAO ecdao = null;
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
		}catch(Exception ec){
			ec.printStackTrace();
		}
		try{
			ArrayList <ECSKU> skuList = kakaKu.crawlData(args[0], 32L);
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
		for(int page = 1; page<10;page++){
			String urlCrawl = "http://kakaku.com/search_results/"+unicode+"/?sort=popular&nameonly=off&lid=ksearch_searchbutton&l=l&act=Input&n=120&page="+page;
			logger.debug("Url -->" + urlCrawl);
			Document doc = Jsoup.connect(urlCrawl).timeout(0).get();
//			File a = new File("/Wei Jian/isentia/kakaku.html");
//			Document doc = Jsoup.parse(a,"UTF-8");
			
			for(Element ele: doc.select("div.c-list1_cell.p-result_item")){
				String url  = ele.select("p.p-result_item_btn").select("a").attr("href");
				if(!url.contains("redirect")){
					try{
						ECSKU ecsku = new ECSKU();
						ecsku.setUrl(url);			
						ecsku.setContent(ele.select("p.p-item_name.s-biggerlinkHover_underline").text());
						url = url.substring(url.indexOf("https://kakaku.com/item/"), url.indexOf("/?lid=pc_ksearch_kakakuitem"));
						url = url.replace("https://kakaku.com/item/", "");
						String price = ele.select("p.p-item_price").select("span").text();
						price = price.replaceAll("¥","");
						price = price.replaceAll(" 〜","");
						price = price.replaceAll(",","");
						ecsku.setPrice(Double.parseDouble(price));
						ecsku.setChannelId(channelId);
						ecsku.setSearchTerm(keyword);
						ecsku.setProductId(url);
						if(ECUtil.checkIsValidProduct(keyword,ele.select("p.p-item_name.s-biggerlinkHover_underline").text())){
							ecskuList.add(ecsku);
						}else{
							logger.error("Fail to insert the following:" +ecsku.getContent());
						}
					}catch(Exception e){
						e.printStackTrace();
					}
				}
			}
		}
		return ecskuList;
	}
}
