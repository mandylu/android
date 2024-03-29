//liuchong@baixing.com
package com.baixing.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import android.util.Log;

import android.os.Environment;

public class TraceUtil {
	public static final String FILE_NAME = "BX_TRACE.txt";
	
	public static final String TAB = "    |||    ";
	public static final String LINE = "\r\n";
	
	public static final boolean ENABLED = false;
	
	public static void trace(String tag, String info)
	{
		
		if (!ENABLED)
		{
			return;
		}
		
		try
		{
			Date date = new Date();
			File f = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
			FileOutputStream fos = new FileOutputStream(f, true);
			fos.write((date.toGMTString() + TAB + tag + TAB + info + LINE).getBytes());
			fos.flush();
			fos.close();
		}
		catch(Throwable t)
		{
			Log.e("BXTrace", "error when do trace " + tag + " " + info);
		}
	}
}
