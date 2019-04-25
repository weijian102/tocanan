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
import com.isentia.ec.base.SKUCrawlers;
import com.isentia.entity.ECSKU;
import com.isentia.util.ECUtil;
import com.mysql.jdbc.exceptions.MySQLIntegrityConstraintViolationException;


public class GmarketSKUCrawlers implements SKUCrawlers{
	
	final static Logger logger = Logger.getLogger(GmarketSKUCrawlers.class);
	public static void main (String [] args){
		GmarketSKUCrawlers gmarketSKU = new GmarketSKUCrawlers();
		ECDAO ecdao = null;
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
		}catch(Exception ec){
			ec.printStackTrace();
		}
		try{
			ArrayList <ECSKU> skuList = gmarketSKU.crawlData("팸퍼스", 5L);
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
		for(int page = 1; page<8;page++){
			
			String urlCrawl = "http://search.gmarket.co.kr/subpage/SearchItemListView.aspx?page="+page+"&page_size=120&keyword=%uD338%uD37C%uC2A4&keywordSeq=11909709554&list_type=list&delivery_group_no=&searchType=main&gdlc=&gdmc=&gdsc=&InResult=&PrevKeyword=&exceptKeyword=&tabSearchType=ALL&sortfield=focus_accuracy&IsMileage=&IsDiscount=&IsStamp=&IsOversea=&IsOld=&IsFeeFree=&IsGuild=&IsVisit=&IsGift=&IsWithoutFee=&IsBookcash=&delFee=&TradWay=&OrderType=&PriceStart=2000&PriceEnd=234500&keywordOrg=&keywordCVT=&keywordCVTi=&SearchClassFormWord=&IsTabSearch=&anchor=list_top_anchor&IsNickName=&IsReturnFeeFree=&IsLotteItem=&IsBrandOnItem=&IsGlobalItem=&IsTpl=&SubdivYN=&IsBrandGd=&inventoryIndex=&categoryType=TS&callFrom=&selectedKeyword=&brfd_brand_no=&pp_sell_cust_no=&brandDirectMode=0&plusGoodsCount=12&smartShippingItemCount=4&smartBoxCategoryCd=&smartBoxBrandNo=&sel_attrib_1=&sel_attrib_2=&sel_attrib_3=&ValueIdName=&ajaxCall=Y";
			logger.debug("Url -->" + urlCrawl);
			Document doc = Jsoup.connect(urlCrawl).ignoreContentType(true).ignoreHttpErrors(true).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A").referrer("http://www.yahoo.com").timeout(0).get();
//			System.out.println(doc);
			
//			System.out.println(doc.select("div[id=divListAjaxContainer]"));
			Elements ul = doc.select("div[id=divListAjaxContainer] > ul"); 
//			System.out.println(ul);
			Elements li = ul.select("li");
			for (int i = 0; i < li.size(); i++) {
				if(li.get(i).select("a").attr("href").contains("item2.")){
					ECSKU ecsku = new ECSKU();
					ecsku.setUrl(li.get(i).select("a").attr("href"));
					List<org.apache.http.NameValuePair> params = URLEncodedUtils.parse(new URI(li.get(i).select("a").attr("href")), "UTF-8");
					for (org.apache.http.NameValuePair param : params) {
					  if(param.getName().equals("goodscode")){
						  ecsku.setProductId(param.getValue());
					  }
					}
					ecsku.setContent(li.get(i).select("span.title").text());
					ecsku.setChannelId(channelId);
					ecsku.setSearchTerm(keyword);
					ecsku.setPrice(Double.parseDouble(li.get(i).select("span.price").select("a").text().replace("원","").replace(",", "")));
					if(ECUtil.checkIsValidProduct(keyword,li.get(i).select("span.title").text())){
						ecskuList.add(ecsku);
					}else{
						logger.error("Fail to insert the following:" +ecsku.getUrl());
					}
				}
			}
		}
		return ecskuList;
	}
}
