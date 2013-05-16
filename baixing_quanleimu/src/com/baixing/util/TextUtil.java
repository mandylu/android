//liuchong@baixing.com
package com.baixing.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.res.Resources;

import com.quanleimu.activity.R;

/**
 * 
 * @author liuchong
 * 
 */
public class TextUtil
{
    public static final long FULL_MINITE = 60L;

    public static final long FULL_HOUR = 60L * 60L;

    public static final long FULL_DAY = 60L * 60L * 24L;

    public static final long FULL_MONTH = 60L * 60L * 24L * 30L;

    public static final long FULL_YEAR = 60L * 60L * 24L * 30L * 12L;

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm");

    private static SimpleDateFormat shortDateFormat = new SimpleDateFormat("yyyy.MM.dd");

    public static final String getTimeDesc(long time)
    {
        return simpleDateFormat.format(new Date(time));
    }

    public static final String getShortTimeDesc(long time)
    {
        return shortDateFormat.format(new Date(time));
    }
    
    public static final String timeTillNow(long time, Context context)
    {
        long now = System.currentTimeMillis();
        long timeInterval = (now - time) / 1000;
        Resources res = context.getResources();
        String agoLabel = res.getString(R.string.common_time_ago);
        if (timeInterval <= 10)
        {
            return res.getString(R.string.common_time_justnow);
        }
        else if (timeInterval > 10 && timeInterval < FULL_MINITE)
        {
            return timeInterval + res.getString(R.string.common_time_second) + agoLabel;
        }
        else if (timeInterval > FULL_MINITE && timeInterval < FULL_HOUR)
        {
            return (timeInterval / FULL_MINITE) + res.getString(R.string.common_time_minute) + agoLabel;
        }
        else if (timeInterval >= FULL_HOUR && timeInterval < FULL_DAY)
        {
            return (timeInterval / FULL_HOUR) + res.getString(R.string.common_time_hour) + agoLabel;
        }
        else if (timeInterval >= FULL_DAY && timeInterval < FULL_MONTH)
        {
            return (timeInterval / FULL_DAY) + res.getString(R.string.common_time_day) + agoLabel;
        }
        else if (timeInterval >= FULL_MONTH && timeInterval < FULL_YEAR)
        {
            return (timeInterval / FULL_MONTH) + res.getString(R.string.common_time_month) + agoLabel;
        }
        else if (timeInterval >= FULL_YEAR)
        {
            return res.getString(R.string.common_time_one_year_ago);
        }
        else
        {
            return res.getString(R.string.common_time_unknow);
        }
    }
    
    public static boolean isNumberSequence(String target)
    {
    	if (target == null || target.trim().length() == 0)
    	{
    		return false;
    	}
    	target = target.trim();
    	
    	try
    	{
    		Long.parseLong(target); 
    		return true; //If nothing happen.
    	}
    	catch(Throwable t)
    	{
    		return false;
    	}
    	
    }

    public static String replaceAll(String string, String expr, String substitute)
    {
        if (string != null)
        {
            string = string.replaceAll(expr, substitute);
        }
        return string;
    }
    
    public static String filterString(String source, char[] filterList) {

		if (null == source || " ".equals(source)) {
			return source;
		}
		
		StringBuffer sb = new StringBuffer();
		int i = 0;
		while (i < source.length()) {
			if (contains(filterList, source.charAt(i))) {
				i++;
			} else {
				sb.append(source.charAt(i));
				i++;
			}
		}
		
		return sb.toString();
    }
    
    private static boolean contains(char[] list, char c) {
    	for (char l : list) {
    		if (l == c) {
    			return true;
    		}
    	}
    	
    	return false;
    }

    public static String decimalFormat(double data)
    {
        DecimalFormat decimal = new DecimalFormat("##.0");
        return decimal.format(data);
    }
    
    public static String decodeUnicode(String source) {
		if (null == source || " ".equals(source)) {
			return source;
		}
		StringBuffer sb = new StringBuffer();
		int i = 0;
		while (i < source.length()) {
			if (source.charAt(i) == '\\') {
				if (source.charAt(i + 1) == 'u') {
					int j = Integer
							.parseInt(source.substring(i + 2, i + 6), 16);
					sb.append((char) j);
					i += 6;
				} else {
					sb.append(source.charAt(i));
					i++;
					sb.append(source.charAt(i));
					i++;
				}
			} else {
				sb.append(source.charAt(i));
				i++;
			}
		}
		
		return sb.toString();
	}
    
    public static String filterXml(String message)
    {
    	String[] from = new String[] {"&amp;", "&lt;", "&gt;", "&quot;", "&apos;"};
    	String[] to	= new String[] {  "&", 		"<", 	">",	"\"",	  "'"};
    	String result = message;
    	for (int i=0; i<from.length; i++)
    	{
    		result = result.replaceAll(from[i], to[i]);
    	}
    	
    	return result;
    }

}