package com.isentia.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.isentia.entity.ECComments;
import com.isentia.entity.ECReportEntity;
import com.isentia.entity.ECSKU;
import com.isentia.entity.HexagonContent;
import com.isentia.entity.InstagramEntity;
import com.isentia.util.ECUtil;

public class ECDAO {
	final static Logger logger = Logger.getLogger(ECDAO.class);
	private Connection master_con_ticket;
	
	public ECDAO()  throws ClassNotFoundException, SQLException{
		this.createMasterTicketConnection();
	}
	
	public void createMasterTicketConnection() throws ClassNotFoundException, SQLException {
		ResourceBundle rb = ResourceBundle.getBundle("database");
		Class.forName(rb.getString("driver"));
		master_con_ticket = DriverManager.getConnection(rb.getString("comment_url"),rb.getString("username"),rb.getString("password"));
	}
	
	public void insertIntoSKU(ECSKU product) throws ClassNotFoundException, SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "Insert into ec_article(Product_ID,URL,Channel_ID,DateTime_Crawled,Search_Term,Content,Price,Voice_Name) values (?,?,?,now(),?,?,?,?)";
		int i =0;
		try{
			ps = master_con_ticket.prepareStatement(query);
			ps.setString(++i, product.getProductId());
			ps.setString(++i, product.getUrl());
			ps.setLong(++i, product.getChannelId());
			ps.setString(++i, product.getSearchTerm());
			ps.setString(++i, product.getContent());
			ps.setDouble(++i, product.getPrice());
			ps.setString(++i, product.getVoiceName());
			int s = ps.executeUpdate();
		}catch(SQLException sqle){
			sqle.printStackTrace();
			throw sqle;
		}finally{
			if(rs!=null){
				rs.close();
			}
			if(ps!=null){
				ps.close();
			}
		}
	}
	
	public void insertIntoComments(ECComments comments) throws ClassNotFoundException, SQLException {
		if(!checkIfCommentExist(comments)){
			PreparedStatement ps = null;
			ResultSet rs = null;
			String query = "Insert into ec_comment(Product_ID,URL,Channel_ID,DateTime_Crawled,DateTime_Posted,Content,Voice_Name,ratings) values (?,?,?,now(),?,?,?,?)";
			int i =0;
			try{
				ps = master_con_ticket.prepareStatement(query);
				ps.setString(++i, comments.getProductId());
				ps.setString(++i, comments.getUrl());
				ps.setLong(++i, comments.getChannelId());
				ps.setTimestamp(++i,new java.sql.Timestamp(comments.getDatetimePost().getTime()));
				ps.setString(++i, comments.getContent());
				ps.setString(++i, comments.getVoiceName());
				ps.setInt(++i,comments.getRating());
				int s = ps.executeUpdate();
			}catch(SQLException sqle){
				throw sqle;
			}finally{
				if(rs!=null){
					rs.close();
				}
				if(ps!=null){
					ps.close();
				}
			}
		}else{
			logger.debug("Comment Already Exist");
		}
	}
	
	private boolean checkIfCommentExist(ECComments ec) throws ClassNotFoundException, SQLException {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean isExist = false;
		String query = "select id from ec_comment where Product_ID = ? and Content = ? and datetime_posted =? and voice_name = ?";
		try{
			ps = master_con_ticket.prepareStatement(query);
			ps.setString(1, ec.getProductId());
			ps.setString(2, ec.getContent());
			ps.setTimestamp(3,new java.sql.Timestamp(ec.getDatetimePost().getTime()));
			ps.setString(4,ec.getVoiceName());
			rs = ps.executeQuery();
			if(rs.next()){
				isExist = true;
			}
		}catch(SQLException sqle){
			throw sqle;
		}finally{
			if(rs!=null){
				rs.close();
			}
			if(ps!=null){
				ps.close();
			}
		}
		return isExist;
	}
	
	
	public ArrayList<ECSKU> getAllSKU(String searchTerm,long channelId) throws ClassNotFoundException, SQLException {
		ArrayList<ECSKU> skuList = new ArrayList<ECSKU>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "Select product_id,url,channel_id,content from ec_article where Search_Term = ? and channel_id = ?" ;
		int i =0;
		try{
			ps = master_con_ticket.prepareStatement(query);
			ps.setString(1, searchTerm);
			ps.setLong(2, channelId);
			rs = ps.executeQuery();
			while(rs.next()){
				ECSKU ecsku = new ECSKU();
				ecsku.setProductId(rs.getString("product_id"));
				ecsku.setUrl(rs.getString("url"));
				ecsku.setChannelId(rs.getLong("channel_id"));
				ecsku.setContent(rs.getString("content"));
				skuList.add(ecsku);
			}
		}catch(SQLException sqle){
			throw sqle;
		}finally{
			if(rs!=null){
				rs.close();
			}
			if(ps!=null){
				ps.close();
			}
		}
		return skuList;
	}
	
	public ArrayList<ECReportEntity> getAllPampersData(String startDate,String endDate) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "select eca.id,eca.product_id,ecc.datetime_posted,eca.url,ecc.voice_name,eca.content,ecc.content,ecc.ratings,eca.search_term,eca.channel_id from ec_article eca, ec_comment ecc where eca.product_id = ecc.product_id  and ecc.datetime_posted >= '"+startDate+"' and ecc.datetime_posted < '"+endDate+"'";
		ArrayList<ECReportEntity> allCommentList = new ArrayList<ECReportEntity>();
		System.out.println(query);
		try{
			ps = master_con_ticket.prepareStatement(query);
			rs = ps.executeQuery();
			while(rs.next()){
				ECReportEntity c = new ECReportEntity();
				c.setTicketId(rs.getLong(1));
				c.setProductId(rs.getString(2));
				c.setDatetimePost(rs.getTimestamp(3));
				c.setUrl(rs.getString(4));
				c.setVoiceName(rs.getString(5));
				c.setTitle(rs.getString(6));
				c.setContent(rs.getString(7));
				c.setRatings(rs.getInt(8));
				c.setSearchTerms(rs.getString(9));
				c.setChannelId(rs.getLong(10));
				allCommentList.add(c);
			}
		}catch(SQLException sqle){
			throw sqle;
		}finally{
			if(rs!=null){
				rs.close();
			}
			if(ps!=null){
				ps.close();
			}
		}
		return allCommentList;
	}
	
	public ArrayList<InstagramEntity> getAllInstagramData(String startDate,String endDate) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "select p.postdate,p.content,p.url,p.type,p.likeNo,p.commentNo,p.viewNo,p.keyword,p.language,p.author from post p, poll pp where p.keyword = pp.keyword and pp.id in (1,2,3,4,5,6,7,8,9,10,11,39,40,41,42) and site = 'instagram' and language ='zh-Hant' and fansCount=0  and postdate >='"+startDate+"' and postdate < '"+endDate+"' ";
		ArrayList<InstagramEntity> allCommentList = new ArrayList<InstagramEntity>();
		System.out.println(query);
		try{
			ps = master_con_ticket.prepareStatement(query);
			rs = ps.executeQuery();
			while(rs.next()){
				InstagramEntity c = new InstagramEntity();
				c.setPostDate(rs.getTimestamp(1));
				c.setContent(rs.getString(2));
				c.setUrl(rs.getString(3));
				c.setType(rs.getString(4));
				c.setLikeNo(rs.getLong(5));
				c.setCommentNo(rs.getLong(6));
				c.setViewNo(rs.getLong(7));
				c.setKeyword(rs.getString(8));
				c.setLanguage(rs.getString(9));
				c.setProfileName(rs.getString(10));
				allCommentList.add(c);
			}
		}catch(SQLException sqle){
			throw sqle;
		}finally{
			if(rs!=null){
				rs.close();
			}
			if(ps!=null){
				ps.close();
			}
		}
		return allCommentList;
	}
	
	public ArrayList<InstagramEntity> checkForSpam(String startDate,String endDate,String limit,String keyword) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "select content,url,id from post where site = 'instagram' and postdate >='"+startDate+"' and postdate < '"+endDate+"' and fansCount is null limit "+limit;
		ArrayList<InstagramEntity> allCommentList = new ArrayList<InstagramEntity>();
		System.out.println(query);
		try{
			ps = master_con_ticket.prepareStatement(query);
//			ps.setString(1, keyword);
			rs = ps.executeQuery();
			int count = 0;
			int notSpam = 0;
			while(rs.next()){
				String content = rs.getString("content");
				if(ECUtil.checkQueryExist("\"請pm\" OR 水貨 OR WeChat OR 正品代购 OR \"微信:\" OR \"1:1\" OR \"1比1\" OR beecrazy OR funshare OR groupon OR taobao OR wangwang OR 一比一 OR 一折 OR 小票 OR 支付宝 OR 支付寶 OR 代理 OR 代購 OR 代购 OR 出貨 OR 出货 OR \"加QQ\" OR 包邮 OR 包郵 OR 正品 OR 仿制品 OR 仿製品 OR 优惠 OR 回收 OR 团购 OR 收售 OR 有貨 OR 有货 OR 免邮 OR 免郵 OR 批发 OR 批發 OR 求購 OR 求购 OR 私信 OR 私聊 OR 到貨 OR 到货 OR 实体店 OR 直銷 OR 直销 OR 空间照片 OR 空間照片 OR 询价 OR 运费 OR 阿里旺旺 OR 复制 OR 复刻 OR 活动价 OR 活動價 OR 秒杀 OR 秒殺 OR 訂購 OR 海外 OR 海淘 OR 选购 OR 高仿 OR 專營 OR 淘宝 OR 淘寶 OR 貨到付款 OR 復刻 OR 搶購 OR 蜂買 OR 詢價 OR 運費 OR 零利潤 OR 零利润 OR 零售 OR 預約 OR 預訂 OR 團購 OR 實體店 OR 精仿 OR 編號 OR 複製 OR 優惠 OR 爆款 OR 专营 OR 抢购 OR 现货 OR 编号 OR 订购 OR 货到付款 OR 进口 OR 预约 OR 预订 OR whatsapp OR wechat OR 微信 OR \"100%\" OR \"99%\" OR \"98%\" OR \"95%\" OR \"90%\" OR \"80%\" OR 九成 OR \"9成\" OR 八成 OR \"8成\" OR 七成 OR \"7成\" OR \"有意pm\" OR 尘袋 OR 塵袋 OR 原装 OR 原裝 OR 收据 OR 收據 OR 交收 OR 有盒 OR 正品 OR 出售 OR seller OR 放售 OR 平售 OR 真品 OR 原单 OR 原單 OR 原價 OR \"adidas Sports Base\" OR \"adidas Training Academy\" OR \"adidas Yoga Class\" OR shoes OR trainer OR 課堂 OR 鞋 OR 訓練班 OR 訓練課程 OR \"#AIRFORCE\" OR \"#AIRMAX\" OR \"AIR FORCE\" OR \"AIR MAX\" OR \"Nike Training Club\" OR AIRFORCE OR AIRMAX OR footwear OR JORDAN OR REACT OR shoes OR VAPORMAX OR ZOOM OR 鞋 OR shoes OR 財務 OR 貸款 OR 耳機 OR 戲票 OR 戲院 OR 鞋", content)) {
					updateIsSpamDataDB(rs.getLong("id"),1);
					count++;
				}else {
					updateIsSpamDataDB(rs.getLong("id"),0);
					System.out.println(rs.getLong("id"));
					notSpam++;
				}
			}
			System.out.println("Spam:"  + count);
			System.out.println("Not Spam:"  + notSpam);
		}catch(SQLException sqle){
			throw sqle;
		}finally{
			if(rs!=null){
				rs.close();
			}
			if(ps!=null){
				ps.close();
			}
		}
		return allCommentList;
	}
	
	
	public ArrayList<InstagramEntity> checkForSpamEnglish(String startDate,String endDate,String limit,String language) throws Exception{
		PreparedStatement ps = null;
		ResultSet rs = null;
		String query = "select content,url,id from post where site = 'instagram' and postdate >='"+startDate+"' and postdate < '"+endDate+"' and fansCount = 0 and vType is null and language = ? limit "+limit;
		ArrayList<InstagramEntity> allCommentList = new ArrayList<InstagramEntity>();
		System.out.println(query);
		try{
			ps = master_con_ticket.prepareStatement(query);
			ps.setString(1, language);
			rs = ps.executeQuery();
			int count = 0;
			int notSpam = 0;
			while(rs.next()){
				String content = rs.getString("content");
				if(ECUtil.checkQueryExist("Japan OR Hongkong OR Korea", content)) {
					updateIsSpamDataDBEnglish(rs.getLong("id"),0);
					System.out.println(rs.getLong("id"));
					count++;
				}else {
					updateIsSpamDataDBEnglish(rs.getLong("id"),1);
					notSpam++;
				}
			}
			System.out.println("Spam:"  + notSpam);
			System.out.println("Not Spam:"  + count);
		}catch(SQLException sqle){
			throw sqle;
		}finally{
			if(rs!=null){
				rs.close();
			}
			if(ps!=null){
				ps.close();
			}
		}
		return allCommentList;
	}
	
	 public int updateIsSpamDataDB (long id,long isSpam) {
			PreparedStatement ps = null;
			int count = 0;
			String insertTableSQL = "update post set fansCount = ? where id = ?";;
			try {
				ps = master_con_ticket.prepareStatement(insertTableSQL);
				ps.setLong(1, isSpam);
				ps.setLong(2, id);
				ps.executeUpdate();
			}catch (SQLException e) {
				System.out.println(e.getErrorCode());
				e.printStackTrace();
			}
			return count;
		}
	 
	 public int updateIsSpamDataDBEnglish (long id,long isSpam) {
			PreparedStatement ps = null;
			int count = 0;
			String insertTableSQL = "update post set fansCount = ?,vType='1' where id = ?";;
			try {
				ps = master_con_ticket.prepareStatement(insertTableSQL);
				ps.setLong(1, isSpam);
				ps.setLong(2, id);
				ps.executeUpdate();
			}catch (SQLException e) {
				System.out.println(e.getErrorCode());
				e.printStackTrace();
			}
			return count;
		}
	 
	 private long checkDataForFacebook(HexagonContent hexPost) {
			PreparedStatement ps = null;
			String insertTableSQL = "select * from crimson where date = ? and url = ? and monitor_id = ? and filter_id = ?";
			long data = 0;
			try {
				ps = master_con_ticket.prepareStatement(insertTableSQL);
				int counter = 0;
				ps.setTimestamp(++counter, new Timestamp(hexPost.getDate().getTime()));
				ps.setString(++counter, hexPost.getUrl());
				ps.setString(++counter, hexPost.getClientAccountId());
				ps.setString(++counter, hexPost.getFilter());
				ResultSet rs = ps.executeQuery();
				if(rs.next()) {
					data = rs.getLong("id");
				}
//				int[] data = ps.executeBatch();
//				conn.commit();
			}catch (SQLException e) {
				System.out.println(e.getErrorCode());
				e.printStackTrace();
//				return false;
			}
			return data;
		}
	 
		public boolean insertCrimsonDataSocial(ArrayList<HexagonContent> hexagonPostList) {
			PreparedStatement ps = null;
			int count = 0;
			String insertTableSQL = "INSERT INTO crimson(url, date, author, content,title,site,location,language,sentiment,filter_id,gender,monitor_id,likes,comments,shares,views) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			try {
				ps = master_con_ticket.prepareStatement(insertTableSQL);
				for(HexagonContent hexPost:hexagonPostList) {
					long id = checkDataForFacebook(hexPost);
					if(id==0) {
						int counter = 0;
						ps.setString(++counter, hexPost.getUrl());
						ps.setTimestamp(++counter, new Timestamp(hexPost.getDate().getTime()));
						ps.setString(++counter, hexPost.getAuthor());
						ps.setString(++counter, hexPost.getContents());
						ps.setString(++counter, hexPost.getTitle());
						ps.setString(++counter, hexPost.getType());
						ps.setString(++counter, hexPost.getLocation());
						ps.setString(++counter, hexPost.getLanguage());
						ps.setString(++counter, hexPost.getSentiment());
						ps.setString(++counter, hexPost.getFilter());
						ps.setString(++counter, hexPost.getGender());
						ps.setString(++counter, hexPost.getClientAccountId());
						ps.setLong(++counter, hexPost.getLikeCount());
						ps.setLong(++counter, hexPost.getCommentCount());
						ps.setLong(++counter, hexPost.getShareCount());
						ps.setLong(++counter, hexPost.getViewCount());
						try {
							ps.executeUpdate();
							count ++;
						}catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				System.out.println(new Date() + " :: Inserted :: " + count);
			}catch (SQLException e) {
				System.out.println(e.getErrorCode());
				e.printStackTrace();
//				return false;
			}
			return true;
		}
	 
	
	public static void main(String [] args) throws Exception{
		ECDAO rcdao = new ECDAO();
		rcdao.checkForSpam(args[0],args[1],args[2],null);
	}
}
