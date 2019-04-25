package com.isentia.ec.comments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.isentia.dao.ECDAO;
import com.isentia.entity.ECComments;
import com.isentia.entity.ECSKU;
import com.isentia.util.DateUtil;

public class AuctionKRCommentsCrawler {
	final static Logger logger = Logger.getLogger(AuctionKRCommentsCrawler.class);
	public static void main (String [] args){
		AuctionKRCommentsCrawler lcc = new AuctionKRCommentsCrawler();
		ECDAO ecdao = null; 
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
			long channelId = 9L;
			ArrayList <ECSKU> allSKU = ecdao.getAllSKU(args[0], channelId);
			ArrayList<ECComments> allComments = new ArrayList<ECComments>();
			for(ECSKU sku: allSKU){
				allComments = lcc.getAllCommentsToCrawl(sku.getProductId());
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
				for(int page=1;page<=20;page++){
					String url = "http://mitem.auction.co.kr/Vip/FeedbackFirstItems?itemNo="+productId+"&pageNo="+page;
					logger.debug(url);
					Document doc = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A").referrer("http://www.yahoo.com").timeout(0).get();
					  Elements links = doc.select("li");
				        for (Element element : links) {
				        	ECComments ecCom = new ECComments();
							ecCom.setProductId(productId);
							ecCom.setVoiceName(element.getElementsByClass("reg_writer").text());
							ecCom.setContent(element.getElementsByClass("comment_simple_text").text());
							ecCom.setUrl(element.getElementsByClass("option_selected").text());
							Date  dd=DateUtil.convertCorrectDate(element.getElementsByClass("reg_date").text(), "yyyy.MM.dd",  "yyyy-MM-dd");
							ecCom.setDatetimePost(dd);
							allECComments.add(ecCom);
				        }
				        if(allECComments.size()==0){
				        	return allECComments;
				        }
				}
			}catch(Exception e){
				logger.error(e.getMessage());
			}
			return allECComments;
		}
}
