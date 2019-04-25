package com.isentia.ec.comments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.isentia.dao.ECDAO;
import com.isentia.entity.ECComments;
import com.isentia.entity.ECSKU;
import com.isentia.util.DateUtil;

public class ShoptrethoCommentsCrawler {
	final static Logger logger = Logger.getLogger(ShoptrethoCommentsCrawler.class);
	public static void main (String [] args){
		ShoptrethoCommentsCrawler lcc = new ShoptrethoCommentsCrawler();
		ECDAO ecdao = null; 
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
			long channelId = 14L;
			ArrayList <ECSKU> allSKU = ecdao.getAllSKU("pampers", channelId);
			ArrayList<ECComments> allComments = new ArrayList<ECComments>();
			for(ECSKU sku: allSKU){
				//			for(int i=0;i<1;i++){
				allComments = lcc.getAllCommentsToCrawl(sku.getProductId());
				//				allComments = lcc.getAllCommentsToCrawl("5CFF3A6933E4");
				System.out.println("Total Crawled Comments" + allComments.size());
				if(allComments.size()>0){
					for(ECComments eccom: allComments){
						eccom.setChannelId(channelId);
						ecdao.insertIntoComments(eccom);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public ArrayList<ECComments> getAllCommentsToCrawl(String productId) throws IOException{
		ArrayList <ECComments> allECComments= new ArrayList<ECComments>(); 
		try{
			boolean nextPage = true;
			int pageCount =1;
			do{
				int count =0;
				String url = "https://shoptretho.com.vn/Desktop/Comment/GetCommentByProductId?proId="+productId+"&cPage="+pageCount+"&pSize=10&isSearch=False&type=";
				logger.debug(url);
				Document doc = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A").referrer("http://www.yahoo.com").timeout(0).get();
				for (Element e: doc.getElementsByClass("content-right")){
					ECComments ecCom = new ECComments();
					ecCom.setProductId(productId);
					ecCom.setVoiceName(e.getElementById("customerName").text());
					ecCom.setContent(e.getElementsByClass("content-main").text());
					String date = e.getElementsByClass("count-like").text().substring(e.getElementsByClass("count-like").text().indexOf(")")+1, e.getElementsByClass("count-like").text().length()).trim();
					Date dd=DateUtil.convertCorrectDate(date,"dd/MM/yyyy",  "yyyy-MM-dd");
					ecCom.setDatetimePost(dd);
					ecCom.setRating(Integer.parseInt(e.select("span").text().substring(1,2)));
					allECComments.add(ecCom);
					count++;
				}
				if(count!=10){
					nextPage = false;
				}else{
					pageCount++;
				}
			}while(nextPage);
		}catch(Exception e){
			logger.error(e.getMessage());
		}
		return allECComments;
	}
}
