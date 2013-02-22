package com.baixing.network.test.func;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.baixing.network.api.ApiConfiguration;
import com.baixing.network.api.FileUploadCommand;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.test.AndroidTestCase;

/**
 * 
 * test for uploading image.
 * 
 * @author liuchong
 *
 */
public class FileUploadTest extends AndroidTestCase {
	
	private String jpegFile;
	private String gifFile;
	private String pngFile;
	public void setUp() {
		try {
			super.setUp();
		} catch (Exception e) {
			fail(e.getMessage());
		}
		
		try {
			Context testContext = getContext().createPackageContext("com.baixing.network.test",
	                Context.CONTEXT_IGNORE_SECURITY);
			
			jpegFile = null;
			jpegFile = copyTestFile(testContext, "frog.jpg", "frog.jpg");
			
			gifFile = null;
			gifFile = copyTestFile(testContext, "frog.gif", "frog.gif");
			
			pngFile = null;
			pngFile = copyTestFile(testContext, "frog.png", "frog.png");
			
		} catch (Throwable e) {
			fail(e.getMessage());
		}
		
		ApiConfiguration.config("shanghai.liuchong.baixing.com", null, "", "");
	}
	
	public void tearDown() {
		
		if (jpegFile != null) {
			new File(jpegFile).delete();
		}
		
		try {
			super.tearDown();
		} catch (Exception e) {
		}
	}

	public void testUploadJpeg() {
		if (jpegFile == null) {
			fail("file does not copy to destnation.");
		}
		
		FileUploadCommand cmd = FileUploadCommand.create(jpegFile);
		String s = cmd.doUpload(getContext());
		assertTrue(s.contains("图片上传成功"));
	}
	
	public void testUploadGif() {
		if (gifFile == null) {
			fail("file not copy to destnation");
		}
		
		FileUploadCommand cmd = FileUploadCommand.create(gifFile);
		String s = cmd.doUpload(getContext());
		assertTrue(s.contains("图片上传成功"));
	}
	
	public void testUploadPng() {
		if (pngFile == null) {
			fail("file not copy to destnation");
		}
		
		FileUploadCommand cmd = FileUploadCommand.create(pngFile);
		String s = cmd.doUpload(getContext());
//		fail(s);
		assertTrue(s.contains("图片上传成功"));
	}
	
	private static String copyTestFile(Context context, String sourceFile, String destFile) throws Throwable {
		InputStream ins = context.getAssets().open(sourceFile, AssetManager.ACCESS_STREAMING);
		File f = new File(Environment.getExternalStorageDirectory(), destFile);
		FileOutputStream os = new FileOutputStream(f);
		
		byte[] all = new byte[ins.available()];
		ins.read(all);
		os.write(all);
		
		os.close();
		ins.close();
		
		return f.getAbsolutePath();
	}
}
