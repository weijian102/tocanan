package com.isentia.ec.report;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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
import com.isentia.entity.InstagramEntity;

public class IGReportTemplate {
	private static SimpleDateFormat monthFormat = new SimpleDateFormat("MMM-yy");
	public static void main (String [] args) throws Exception{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdfFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date alertTiming = new Date();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, 2);
		cal.set(Calendar.DATE, 1);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.SECOND,0);
		alertTiming = cal.getTime();
		String sStartDate = sdf.format(alertTiming);

		Date nowDate = new Date();
		Calendar calNow = Calendar.getInstance();
		calNow.set(Calendar.MONTH, 3);
		calNow.set(Calendar.DATE, 1);
		calNow.set(Calendar.MINUTE, 0);
		calNow.set(Calendar.HOUR_OF_DAY, 0);
		calNow.set(Calendar.SECOND,0);
		nowDate = calNow.getTime();
		String sEndDate = sdf.format(nowDate);

		System.out.println("Start Date --> " + sStartDate);
		System.out.println("End Date --> " + sEndDate);	

		ECDAO tdao = new ECDAO();
		tdao.createMasterTicketConnection();
		ArrayList<InstagramEntity> reList = tdao.getAllInstagramData(sStartDate, sEndDate);

		XSSFWorkbook workbook = new XSSFWorkbook();

		String fileName = "/tocanan/Pampers_Report-"+sdf.format(alertTiming)+"-"+sdf.format(nowDate)+".xlsx";

		FileOutputStream fileOut = new FileOutputStream(fileName);

		XSSFSheet worksheet = workbook.createSheet("IG-Data");
		XSSFRow row1 = worksheet.createRow((short) 0);
		XSSFFont font = workbook.createFont();
		font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
		XSSFCell cellA1 = row1.createCell((short) 0);
		cellA1.setCellValue("DateTime Posted");
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setFillForegroundColor(HSSFColor.GREY_50_PERCENT.index);
		cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		cellStyle.setFont(font);
		cellA1.setCellStyle(cellStyle);

		XSSFCell cellB1 = row1.createCell((short) 1);
		cellB1.setCellValue("Content");
		cellB1.setCellStyle(cellStyle);

		XSSFCell cellC1 = row1.createCell((short) 2);
		cellC1.setCellValue("URL");
		cellC1.setCellStyle(cellStyle);

		XSSFCell cellD1 = row1.createCell((short) 3);
		cellD1.setCellValue("Type");
		cellD1.setCellStyle(cellStyle);

		XSSFCell cellE1 = row1.createCell((short) 4);
		cellE1.setCellValue("Like No");
		cellE1.setCellStyle(cellStyle);

		XSSFCell cellF1 = row1.createCell((short) 5);
		cellF1.setCellValue("Comment No");
		cellF1.setCellStyle(cellStyle);

		XSSFCell engagementCell = row1.createCell((short) 6);
		engagementCell.setCellValue("View No");
		engagementCell.setCellStyle(cellStyle);

		XSSFCell cellG1 = row1.createCell((short) 7);
		cellG1.setCellValue("Keyword");
		cellG1.setCellStyle(cellStyle);

		XSSFCell cellh1 = row1.createCell((short) 8);
		cellh1.setCellValue("Language");
		cellh1.setCellStyle(cellStyle);
		
		XSSFCell celli1 = row1.createCell((short) 9);
		celli1.setCellValue("Profile Name");
		celli1.setCellStyle(cellStyle);

		int rowCount =1;
		for(InstagramEntity re: reList){
			writeExcel(re,rowCount,worksheet,sdfFormat);
			rowCount++;
		}
		workbook.write(fileOut);
		fileOut.flush();
		fileOut.close();
		
	}
	
	public static void writeExcel(InstagramEntity re,int rowCount,XSSFSheet  worksheet,SimpleDateFormat sdf){
		XSSFRow row = worksheet.createRow(rowCount);
		XSSFCell urlCell= row.createCell((short) 0);
		urlCell.setCellValue(sdf.format(re.getPostDate()));
		XSSFCell rtCount= row.createCell((short) 1);
		rtCount.setCellValue(re.getContent());
		XSSFCell titleV= row.createCell((short) 2);
		titleV.setCellValue(re.getUrl());
		XSSFCell contentV= row.createCell((short) 3);
		contentV.setCellValue(re.getType());
		XSSFCell channelV= row.createCell((short) 4);
		channelV.setCellValue(re.getLikeNo());
		XSSFCell ticketId= row.createCell((short) 5);
		ticketId.setCellValue(re.getCommentNo());
		XSSFCell viewNo= row.createCell((short) 6);
		viewNo.setCellValue(re.getViewNo());
		XSSFCell commentNo= row.createCell((short) 7);
		commentNo.setCellValue(re.getKeyword());
		XSSFCell likesNo= row.createCell((short) 8);
		likesNo.setCellValue(re.getLanguage());
		XSSFCell profileName= row.createCell((short) 9);
		profileName.setCellValue(re.getProfileName());
	}
}
