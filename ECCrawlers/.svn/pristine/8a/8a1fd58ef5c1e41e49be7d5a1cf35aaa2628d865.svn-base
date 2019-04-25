package com.isentia.ec.comments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

import com.isentia.dao.ECDAO;
import com.isentia.ec.base.CommentsCrawler;
import com.isentia.entity.ECComments;
import com.isentia.entity.ECSKU;
import com.isentia.util.ECUtil;

public class GmarketCommentsCrawler implements CommentsCrawler{
	final static Logger logger = Logger.getLogger(GmarketCommentsCrawler.class);
	public static void main (String [] args){
		GmarketCommentsCrawler lcc = new GmarketCommentsCrawler();
		ECDAO ecdao = null; 
		try{
			ecdao = new ECDAO();
			ecdao.createMasterTicketConnection();
			long channelId = 5L;
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
			int totalPageToCrawl =1;
			try{
				do {
					String url = "http://item2.gmarket.co.kr/Item/detailview/ajax/ItemOpinionJson.aspx?page="+pageCount+"&page_size=10&goodscode="+productId+"&output=JSON&_=1496645085000";
					System.out.println(url);
					JSONObject json = ECUtil.readJsonFromUrl(url,"euc-kr");
					totalPageToCrawl = json.getInt("pageCount");
					logger.debug("totalPageToCrawl :" +totalPageToCrawl);
					JSONArray ja = json.getJSONArray("Opinions");
					for (int i = 0; i < ja.size(); i++) {
						ECComments ecCom = new ECComments();
						ecCom.setProductId(productId);
						JSONObject obj = (JSONObject) ja.get(i);
						if(obj.getString("OpinionContents").trim().equals("")){
							ecCom.setContent("적극 추천합니다 배송이 빠릅니다");
						}else{
							ecCom.setContent(obj.getString("OpinionContents"));
						}
						ecCom.setUrl(obj.getString("OrderOptionInfo"));
						String date = obj.getString("OpinionDate").trim();
						date = date.substring(date.indexOf("(")+1,date.indexOf(")"));
						Date convertedDate = new Date(Long.parseLong(date)); // 'epoch' in long
						ecCom.setDatetimePost(convertedDate);
						ecCom.setVoiceName(obj.getString("Nickname").trim());
						allECComments.add(ecCom);
					}
					pageCount++;
				}while(pageCount!=totalPageToCrawl+1);
			}catch(Exception e){
				logger.error(e.getMessage());
			}
			return allECComments;
		}
}
