package com.isentia.ec;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.isentia.dao.ECDAO;
import com.isentia.entity.ECSKU;
import com.isentia.util.ECUtil;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;

public class ShoptrethoSKUCrawlers {
	final static Logger logger = Logger.getLogger(ShoptrethoSKUCrawlers.class);
	public static void main (String [] args){
		ShoptrethoSKUCrawlers ada = new ShoptrethoSKUCrawlers();
		ECDAO ecdao = null;
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
		}catch(Exception ec){
			ec.printStackTrace();
		}
		try{
			ArrayList <ECSKU> skuList = ada.crawlData("huggies", 14L);
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
			String urlCrawl = "https://shoptretho.com.vn/Desktop/ProductCategory/AjaxFilter?categoryId=62356334&page=1&permalink=bim-va-ta-giay&provider="+keyword;
//			System.out.println(urlCrawl);
			JSONObject json = ECUtil.readJsonFromUrl(urlCrawl,"UTF-8");
			String prodFilter =json.getString("ProductFilter");
			Document doc = Jsoup.parse(prodFilter);
			Elements e = doc.getElementsByClass("m-product-item");
			for(Element d:e){
				ECSKU ecsku = new ECSKU();
				logger.debug(d.attr("data-original"));
				String url = "https://shoptretho.com.vn"+d.attr("data-original");
				List<org.apache.http.NameValuePair> params = URLEncodedUtils.parse(new URI(url), "UTF-8");
				for (org.apache.http.NameValuePair param : params) {
				  if(param.getName().equals("productId")){
					  logger.debug(param.getValue());
					  ecsku.setProductId(param.getValue());
				  }
				}
				Document prodDoc = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true).userAgent("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36").referrer("http://www.yahoo.com").timeout(10000).get();
				ecsku.setContent(prodDoc.getElementsByClass("desc-pro").text());
				ecsku.setUrl("https://shoptretho.com.vn"+prodDoc.select("div.m-product-item-hover.fl-left").select("a").attr("href"));
				String price =prodDoc.getElementsByClass("m-product-price-1").text();
				price = price.replaceAll("đ","");
				price = price.replaceAll("\\.","");
				ecsku.setPrice(Double.parseDouble(price));
				ecsku.setChannelId(channelId);
				ecsku.setSearchTerm(keyword);
				ecskuList.add(ecsku);
				
			}
		}
		return ecskuList;
	}
}
