package com.isentia.ec.comments;

import java.io.IOException;
import java.util.ArrayList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.isentia.dao.ECDAO;
import com.isentia.entity.ECComments;
import com.isentia.entity.ECSKU;
import com.isentia.util.DateUtil;
import com.isentia.util.ECUtil;

public class LazadaKRCommentsCrawler {
	final static Logger logger = Logger.getLogger(LazadaKRCommentsCrawler.class);
	public static void main (String [] args){
		LazadaKRCommentsCrawler lcc = new LazadaKRCommentsCrawler();
		ECDAO ecdao = null; 
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
			long channelId = 11L;
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
			int pageCount = 1;
			boolean toCrawl=true;
			try{
				while(toCrawl){
					String url = "http://www.lazada.vn/ajax/ratingreview/reviewspage?page="+pageCount+"&sort=relevance&sortDirection=desc&sku="+productId;
					logger.debug(url);
					JSONObject json = ECUtil.readJsonFromUrl(url,"UTF-8");
					if(json.getJSONObject("data").getJSONObject("paginator").getString("next").equals("false")){
						toCrawl=false;
					}
					JSONArray ja = json.getJSONObject("data").getJSONArray("ratingReviews");
					for (int i = 0; i < ja.size(); i++) {
						ECComments ecCom = new ECComments();
						ecCom.setProductId(productId);
						JSONObject obj = (JSONObject) ja.get(i);
						ecCom.setContent(obj.getString("detail"));
						String date = obj.getString("created_at").trim();
						ecCom.setDatetimePost(DateUtil.convertCorrectDate(date,"yyyy-MM-dd HH:mm:ss","yyyy-MM-dd HH:mm:ss"));
						ecCom.setVoiceName(obj.getString("nickname").trim());
						ecCom.setRating(obj.getInt("avg_rating"));
						allECComments.add(ecCom);
					}
					pageCount++;
				}
			}catch(Exception e){
				logger.error(e.getMessage());
			}
			return allECComments;
		}
}
