package com.isentia.ec.comments;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.isentia.dao.ECDAO;
import com.isentia.entity.ECComments;
import com.isentia.entity.ECSKU;
import com.isentia.util.DateUtil;

public class AdayroiCommentsCrawler{
	final static Logger logger = Logger.getLogger(AdayroiCommentsCrawler.class);
	public static void main (String [] args){
		AdayroiCommentsCrawler lcc = new AdayroiCommentsCrawler();
		ECDAO ecdao = null; 
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
			long channelId = 12L;
			ArrayList <ECSKU> allSKU = ecdao.getAllSKU("bobby", channelId);
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
		public ArrayList<ECComments> getAllCommentsToCrawl(String productId,String content) throws IOException{
			ArrayList <ECComments> allECComments= new ArrayList<ECComments>(); 
			try{
				boolean toCrawl = true;
				int pageNum =1;
				do{					
					String url = "https://comment.adayroi.com/Pages/CommAdayroiSys_v4_fe1.aspx?product="+productId+"&siteId=vine-1414640318&pageIndex="+pageNum+"&pageSize=10&fedata="+URLEncoder.encode(content)+"&domain=www.adayroi.com&time=1495974292494";
					logger.debug(url);
					Document doc = Jsoup.connect(url).ignoreContentType(true).ignoreHttpErrors(true).userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/7046A194A").referrer("http://www.yahoo.com").timeout(0).get();
					 String data = doc.getElementsByTag("script").get(2).toString();
					 JSONObject obj = JSONObject.fromObject(data.substring(data.indexOf("commentList")+14,data.indexOf(";//")));
					 JSONArray jArr = obj.getJSONArray("Comment");
					 for(int i=0;i<jArr.size();i++){
						 ECComments ecCom = new ECComments();
						 ecCom.setProductId(productId);
						 JSONObject a = jArr.getJSONObject(i);
						 ecCom.setRating(Integer.parseInt(a.getString("CommRate")));
						 ecCom.setVoiceName(a.getString("CommUserName"));
						 ecCom.setContent(a.getString("CommContent"));
						 Date dd=DateUtil.convertCorrectDate(a.getString("SCommDate"),"dd/MM/yyyy, hh:mm:ss",  "yyyy-MM-dd hh:mm:ss");
						 ecCom.setDatetimePost(dd);
						 allECComments.add(ecCom);
					 }
					 if(jArr.size()< 10){
						 logger.debug(jArr.size());
						 toCrawl = false;
					 }else{
						 pageNum++;
					 }
				}while(toCrawl);
			}catch(Exception e){
				logger.error(e.getMessage());
			}
			return allECComments;
		}
}
