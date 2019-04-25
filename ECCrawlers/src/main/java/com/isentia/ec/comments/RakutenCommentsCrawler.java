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

public class RakutenCommentsCrawler{
	final static Logger logger = Logger.getLogger(RakutenCommentsCrawler.class);
	public static void main (String [] args){
		RakutenCommentsCrawler lcc = new RakutenCommentsCrawler();
		ECDAO ecdao = null; 
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
			long channelId = 30L;
			ArrayList <ECSKU> allSKU = ecdao.getAllSKU(args[0], channelId);
			System.out.println(allSKU.size());
			ArrayList<ECComments> allComments = new ArrayList<ECComments>();
			for(ECSKU sku: allSKU){
				allComments = lcc.getAllCommentsToCrawl(sku.getProductId(),sku.getContent());
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
	public ArrayList<ECComments> getAllCommentsToCrawl(String productId,String content) throws Exception{
		ArrayList <ECComments> allECComments= new ArrayList<ECComments>(); 
		
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
		try{
			for(int i =1;i<10;i++){
				String url = "https://review.rakuten.co.jp/item/1/"+productId+"/"+i+".1/sort6/";
				Document doc = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A").referrer("http://www.yahoo.com").timeout(0).get();
				for(Element e: doc.select("div.revRvwUserSec.hreview")){
					ECComments ecCom = new ECComments();
					ecCom.setProductId(productId);
					Date dd=DateUtil.convertCorrectDate(e.select("span.revUserEntryDate.dtreviewed").text(),"yyyy-MM-dd","yyyy-MM-dd hh:mm:ss");
					 ecCom.setDatetimePost(dd);
					 ecCom.setRating(Integer.parseInt(e.select("span.revUserRvwerNum.value").text()));
					 ecCom.setVoiceName(e.select("dt.revUserFaceName").text());
					 ecCom.setContent(e.select("dd.revRvwUserEntryCmt.description").text());
					 allECComments.add(ecCom);
				}
			}
		}catch(Exception e){
			logger.error(e.getMessage());
		}
		return allECComments;
	}
}
