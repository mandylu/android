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

    public static String getMD5(String strMD5)
    {
        byte[] source = strMD5.getBytes();
        String s = null;
        char hexDigits[] =
        { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        try
        {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            md.update(source);
            byte tmp[] = md.digest();
            char str[] = new char[16 * 2];
            int k = 0;
            for (int i = 0; i < 16; i++)
            {
                byte byte0 = tmp[i];
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            s = new String(str);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return s;
    }

    public static String replaceAll(String string, String expr, String substitute)
    {
        if (string != null)
        {
            string = string.replaceAll(expr, substitute);
        }
        return string;
    }

    public static String decimalFormat(double data)
    {
        DecimalFormat decimal = new DecimalFormat("##.0");
        return decimal.format(data);
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