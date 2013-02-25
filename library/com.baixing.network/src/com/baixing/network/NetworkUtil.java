package com.baixing.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtil {
	public static String getMD5(String targetStr) {
		byte[] source = targetStr.getBytes();
		String s = null;
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			java.security.MessageDigest md = java.security.MessageDigest
					.getInstance("MD5");
			md.update(source);
			byte tmp[] = md.digest();
			char str[] = new char[16 * 2];
			int k = 0;
			for (int i = 0; i < 16; i++) {
				byte byte0 = tmp[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			s = new String(str);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return s;
	}
	
	public static boolean isWifiConnection(Context context) {
		 if (context == null) {
			 return false;
		 }
		 
		ConnectivityManager connectivityManager = 
				(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null
				&& activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
			return true;
		}
		
		return false;
	}
	
	public static boolean isNetworkActive(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
		if (activeNetInfo != null) {
			return true;
		}
		return false;
	}
	
}
