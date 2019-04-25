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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.isentia.dao.ECDAO;
import com.isentia.ec.base.CommentsCrawler;
import com.isentia.entity.ECComments;
import com.isentia.entity.ECSKU;
import com.isentia.util.DateUtil;

public class KakakuCommentsCrawler implements CommentsCrawler{

	public static void main (String [] args) throws Exception{		
		KakakuCommentsCrawler lcc = new KakakuCommentsCrawler();
		ECDAO ecdao = null; 
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
			long channelId = 32L;
			ArrayList <ECSKU> allSKU = ecdao.getAllSKU(args[0], channelId);
			ArrayList<ECComments> allComments = new ArrayList<ECComments>();
			for(ECSKU sku: allSKU){
				allComments = lcc.getAllCommentsToCrawl(sku.getProductId());
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
		public ArrayList<ECComments> getAllCommentsToCrawl(String productId){
			ArrayList <ECComments> allECComments= new ArrayList<ECComments>(); 
			try {
				Document doc = Jsoup.connect("http://review.kakaku.com/review/"+productId).timeout(0).get();
				for(Element e: doc.select("div.reviewBox.ver2013.boxGr")){
					ECComments ec = new ECComments();
					ec.setProductId(productId);
					try{
						ec.setVoiceName(e.select("span.userName").select("a").text());
						System.out.println(e.select("span.userName").select("a").text());
						String date = e.getElementsByClass("entryDate").text();
						date = date.substring(0,date.indexOf("[")-1);
						Date dd = DateUtil.convertCorrectDate(date,"yyyy年MM月dd日","dd-MM-yyyy");
						ec.setDatetimePost(dd);
						ec.setUrl(e.getElementsByClass("reviewTitle").select("span").select("a").attr("href"));
						ec.setContent(e.getElementsByClass("revEntryCont").text());
						String ratings = e.getElementsByClass("revRateBox").text();
						ratings = ratings.substring(ratings.indexOf("満足度")+3,ratings.indexOf("満足度")+6);
						ec.setRating(Integer.parseInt(ratings.trim()));
						allECComments.add(ec);
					}catch(Exception parseE){
						parseE.printStackTrace();
					}
				}
			}catch (Exception e) {
				e.printStackTrace();
			}
			return allECComments;
		}
}
