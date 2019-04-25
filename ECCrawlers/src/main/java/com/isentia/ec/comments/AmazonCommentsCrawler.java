package com.isentia.ec.comments;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;

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
import com.isentia.entity.ECComments;
import com.isentia.entity.ECSKU;
import com.isentia.util.DateUtil;

public class AmazonCommentsCrawler {
	final static Logger logger = Logger.getLogger(AmazonCommentsCrawler.class);
	public static void main (String [] args) throws Exception{
		
		AmazonCommentsCrawler lcc = new AmazonCommentsCrawler();
		ECDAO ecdao = null; 
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
			long channelId = 34L;
			ArrayList <ECSKU> allSKU = ecdao.getAllSKU(args[0], channelId);
			System.out.println(allSKU.size());
			
			ArrayList<ECComments> allComments = new ArrayList<ECComments>();
			
			for(ECSKU sku: allSKU){
				allComments = lcc.getAllCommentsToCrawl(sku.getProductId(),sku.getContent());
				System.out.println("Total Crawled Comments" + allComments.size());
				if(allComments.size()>0){
					for(ECComments eccom: allComments){
						eccom.setChannelId(channelId);
						try{
							ecdao.insertIntoComments(eccom);
						}catch (Exception e){
							logger.error("DB Insert Error: " + e.getMessage());
						}
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	public ArrayList<ECComments> getAllCommentsToCrawl(String productId,String content) throws IOException{
		ArrayList <ECComments> allECComments= new ArrayList<ECComments>(); 
		try{
			for(int i =1;i<10;i++){
				int itemRetreive = 0;
				String url = "https://www.amazon.co.jp/product-reviews/"+productId+"/ref=cm_cr_arp_d_viewopt_srt?ie=UTF8&reviewerType=all_reviews&showViewpoints=1&sortBy=recent&pageNumber="+i;
				logger.debug(url);
				Document doc = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").referrer("http://www.yahoo.com").timeout(10000).get();
				for(Element e: doc.select("div.a-section.review")){
					itemRetreive++;
					ECComments ecCom = new ECComments();
					ecCom.setProductId(productId);
					Date dd=DateUtil.convertCorrectDate(e.select("span.a-size-base.a-color-secondary.review-date").text(),"yyyy年MM月dd日","yyyy-MM-dd hh:mm:ss");
					ecCom.setDatetimePost(dd);
					ecCom.setContent(e.select("span.a-size-base.review-text").text());
					ecCom.setVoiceName(e.select("a.a-size-base.a-link-normal.author").text());
					ecCom.setRating(Integer.parseInt(e.getElementsByClass("a-icon-alt").get(0).text().replaceAll("5つ星のうち", "").replaceAll(".0", "")));
					allECComments.add(ecCom);
				}
				if(itemRetreive==0){
					return allECComments;
				}
			}
		}catch(Exception e){
			logger.error(e.getMessage());
		}
		return allECComments;
	}
}
