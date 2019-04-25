package com.isentia.ec.report;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.isentia.dao.ECDAO;
import com.isentia.ec.ProductRatingCrawler;
import com.isentia.entity.ECReportEntity;
import com.isentia.util.DateUtil;
import com.isentia.util.ECUtil;
import com.isentia.util.EmailWithAttachment;

public class ECReportTemplate {
	private static SimpleDateFormat monthFormat = new SimpleDateFormat("MMM-yy");
	public static void main (String [] args) throws Exception{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date alertTiming = new Date();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, -1);
		cal.set(Calendar.MONTH, 0);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.SECOND,0);
		alertTiming = cal.getTime();
		String sStartDate = sdf.format(alertTiming);

		Date nowDate = new Date();
		Calendar calNow = Calendar.getInstance();
		calNow.set(Calendar.MINUTE, 0);
		calNow.set(Calendar.HOUR_OF_DAY, 0);
		calNow.set(Calendar.SECOND,0);
		nowDate = calNow.getTime();
		String sEndDate = sdf.format(nowDate);

		System.out.println("Start Date --> " + sStartDate);
		System.out.println("End Date --> " + sEndDate);	

		ECDAO tdao = new ECDAO();
		tdao.createMasterTicketConnection();
		ArrayList<ECReportEntity> reList = tdao.getAllPampersData(sStartDate, sEndDate);

		XSSFWorkbook workbook = new XSSFWorkbook();

		String fileName = "/Wei Jian/isentia/Pampers_Report-"+sdf.format(alertTiming)+"-"+sdf.format(nowDate)+".xlsx";

		FileOutputStream fileOut = new FileOutputStream(fileName);

		XSSFSheet worksheet = workbook.createSheet("EC-Data");
		XSSFRow row1 = worksheet.createRow((short) 0);
		XSSFFont font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		XSSFCell cellA1 = row1.createCell((short) 0);
		cellA1.setCellValue("Ticket ID");
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFillForegroundColor(HSSFColor.GREY_50_PERCENT.index);
		cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellStyle.setFont(font);
		cellA1.setCellStyle(cellStyle);

		XSSFCell cellB1 = row1.createCell((short) 1);
		cellB1.setCellValue("Product ID");
		cellB1.setCellStyle(cellStyle);

		XSSFCell cellC1 = row1.createCell((short) 2);
		cellC1.setCellValue("DateTime Posted");
		cellC1.setCellStyle(cellStyle);

		XSSFCell cellD1 = row1.createCell((short) 3);
		cellD1.setCellValue("URL");
		cellD1.setCellStyle(cellStyle);

		XSSFCell cellE1 = row1.createCell((short) 4);
		cellE1.setCellValue("Voice Name");
		cellE1.setCellStyle(cellStyle);

		XSSFCell cellF1 = row1.createCell((short) 5);
		cellF1.setCellValue("Title");
		cellF1.setCellStyle(cellStyle);

		XSSFCell engagementCell = row1.createCell((short) 6);
		engagementCell.setCellValue("Content");
		engagementCell.setCellStyle(cellStyle);

		XSSFCell cellG1 = row1.createCell((short) 7);
		cellG1.setCellValue("Ratings");
		cellG1.setCellStyle(cellStyle);

		XSSFCell cellh1 = row1.createCell((short) 8);
		cellh1.setCellValue("Subject Name");
		cellh1.setCellStyle(cellStyle);

		XSSFCell celli1 = row1.createCell((short) 9);
		celli1.setCellValue("Channel Name");
		celli1.setCellStyle(cellStyle);
		
		XSSFCell monthInCal = row1.createCell((short) 10);
		monthInCal.setCellValue("MonthInCalendar");
		monthInCal.setCellStyle(cellStyle);
		
		XSSFCell quadInCal = row1.createCell((short) 11);
		quadInCal.setCellValue("QuarterInCalendar");
		quadInCal.setCellStyle(cellStyle);
		
		XSSFCell prodRating = row1.createCell((short) 12);
		prodRating.setCellValue("Product Ratings");
		prodRating.setCellStyle(cellStyle);
		
		int rowCount =1;
		for(ECReportEntity re: reList){
			writeExcel(re,rowCount,worksheet,sdfFormat);
			rowCount++;
		}
		workbook.write(fileOut);
		fileOut.flush();
		fileOut.close();
		
	}
	
	public static void writeExcel(ECReportEntity re,int rowCount,XSSFSheet  worksheet,SimpleDateFormat sdf){
		XSSFRow row = worksheet.createRow(rowCount);
		XSSFCell urlCell= row.createCell((short) 0);
		urlCell.setCellValue(re.getTicketId());
		XSSFCell rtCount= row.createCell((short) 1);
		rtCount.setCellValue(re.getProductId());
		XSSFCell titleV= row.createCell((short) 2);
		titleV.setCellValue(sdf.format(re.getDatetimePost()));
		XSSFCell contentV= row.createCell((short) 3);
		contentV.setCellValue(re.getUrl());
		XSSFCell channelV= row.createCell((short) 4);
		channelV.setCellValue(re.getVoiceName());
		XSSFCell ticketId= row.createCell((short) 5);
		ticketId.setCellValue(re.getTitle());
		XSSFCell viewNo= row.createCell((short) 6);
		viewNo.setCellValue(re.getContent());
		XSSFCell commentNo= row.createCell((short) 7);
		commentNo.setCellValue(re.getRatings());
		XSSFCell likesNo= row.createCell((short) 8);
		likesNo.setCellValue(re.getSearchTerms());
		XSSFCell fragranceNo= row.createCell((short) 9);
		fragranceNo.setCellValue(ECUtil.getChannelName(re.getChannelId()));
		double rating = 0.0;
//		try{
//			rating = ProductRatingCrawler.getRating(re.getUrl(), ECUtil.getChannelName(re.getChannelId()));
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//		
		XSSFCell articleIdCell = row.createCell((short) 10);
    	articleIdCell.setCellValue(monthFormat.format(re.getDatetimePost()));
		
    	Calendar cal=Calendar.getInstance();
    	cal.setTime(re.getDatetimePost());
    	
    	XSSFCell quatarInCalander = row.createCell((short) 11);
    	quatarInCalander.setCellValue("Q" + DateUtil.getQuarter(cal) + " " + cal.get(Calendar.YEAR));
    	
		XSSFCell ratingCell= row.createCell((short) 12);
		ratingCell.setCellValue(rating);
	}
}
