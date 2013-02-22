package com.baixing.network.test.func;

import java.io.File;

import com.baixing.network.api.FileDownloadCommand;

import android.os.Environment;
import android.test.AndroidTestCase;

/**
 * 
 * @author liuchong
 *
 */
public class FileDownloadRequstTest extends AndroidTestCase {
	
	
	public void testDownload() {
		String url = "http://static.baixing.net/images/logo_v2_S.png";
		
		File targetFile = new File(Environment.getExternalStorageDirectory(), "test.png");
		targetFile.delete();
		assertFalse(targetFile.exists());
		
		FileDownloadCommand cmd = new FileDownloadCommand(url);
		boolean succed = cmd.doDownload(getContext(), targetFile);
		assertTrue(succed);
		assertTrue(targetFile.exists());
		assertTrue(targetFile.length() > 0);
	}
	
}
