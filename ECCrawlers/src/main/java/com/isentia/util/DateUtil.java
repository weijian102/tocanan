package com.isentia.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

	public static Date convertCorrectDate(String date, String dateFormatOld,String dateFormatExpected) throws ParseException{
		SimpleDateFormat sdf1 = new SimpleDateFormat (dateFormatOld);
	    Date d1 = sdf1.parse(date);
	    SimpleDateFormat sdf2 = new SimpleDateFormat (dateFormatExpected);
	    String correctDate =sdf2.format(d1);
	    Date d2 = sdf2.parse(correctDate);
	    return d2;
	}
	
	public static int getQuarter(Calendar calendar) {
	    int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
	    return weekOfYear < 13 ? 1
	         : weekOfYear < 25 ? 2
	         : weekOfYear < 38 ? 3
	         : 4;
	}
}
