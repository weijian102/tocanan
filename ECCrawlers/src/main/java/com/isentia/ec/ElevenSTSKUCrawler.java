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


public class ElevenSTSKUCrawler {
	final static Logger logger = Logger.getLogger(ElevenSTSKUCrawler.class);
	public static void main (String [] args){
		ElevenSTSKUCrawler gmarketSKU = new ElevenSTSKUCrawler();
		ECDAO ecdao = null;
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
		}catch(Exception ec){
			ec.printStackTrace();
		}
		try{
			ArrayList <ECSKU> skuList = gmarketSKU.crawlData("하기스", 8L);
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
		for(int page = 1; page<=22;page++){
			String urlCrawl = "http://search.11st.co.kr/SearchPrdEnAction.tmall?method=getTotalSearchSeller&kwd=%C7%CF%B1%E2%BD%BA&excptKwd=&pageNum="+page+"&pageSize=100&researchFlag=false&lCtgrNo=0&mCtgrNo=0&sCtgrNo=0&dCtgrNo=0&prdType=&prdTab=T&selMthdCd=&targetTab=T&viewType=L&minPrice=0&maxPrice=0&homeTypeCd=&minPrice_ctry=&maxPrice_ctry=&minPrice_kor=0&maxPrice_kor=0&custBenefit=&dlvType=&previousKwd=%C6%D4%C6%DB%BD%BA&previousExcptKwd=&goodsType=&buySatisfy=&sortCd=NP&isBack=N&selectedCtgrNoList=&ctgrNoDepth=lCtgrNo&firstInputKwd=%C6%D4%C6%DB%BD%BA&beforeThesaurusKwd=%C6%D4%C6%DB%BD%BA&beforeRelationKwd=&myPrdViewYN=Y&sellerCreditGradeType=%5B%5D&favorShopYn=&schFrom=#searchlist";
			logger.debug("Url -->" + urlCrawl);
			Document doc = Jsoup.connect(urlCrawl).ignoreContentType(true).ignoreHttpErrors(true).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A").referrer("http://www.yahoo.com").timeout(0).get();
			Elements ul = doc.select("div.listtype_wrap > ul"); 
			Elements li = ul.select("li");
			for (int i = 0; i < li.size(); i++) {
				ECSKU ecsku = new ECSKU();
				String prodId = li.get(i).getElementsByClass("pup_title").select("a").attr("href");
				prodId = prodId.substring(prodId.indexOf("(")+2,prodId.indexOf(")")-1);
				ecsku.setUrl("http://www.11st.co.kr/product/SellerProductDetail.tmall?method=getSellerProductDetail&prdNo="+prodId+"&trTypeCd=21&trCtgrNo=585021&lCtgrNo=1001344&mCtgrNo=1001502");
				ecsku.setProductId(prodId);
				ecsku.setContent(li.get(i).getElementsByClass("pup_title").text());
				String value = li.get(i).getElementsByClass("sale").text();
				value = value.replace(",", "");
				value = value.replace("￦", "");
				ecsku.setPrice(Double.parseDouble(value));
				ecsku.setChannelId(channelId);
				ecsku.setSearchTerm(keyword);
				if(ECUtil.checkIsValidProduct(keyword,li.get(i).getElementsByClass("pup_title").text())){
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
