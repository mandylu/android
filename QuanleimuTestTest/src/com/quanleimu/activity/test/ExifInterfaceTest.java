package com.quanleimu.activity.test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import test.util.TestUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.media.ExifInterface;
import android.os.Environment;
import android.test.AndroidTestCase;

public class ExifInterfaceTest extends AndroidTestCase {
	
//	File outputPath;// = Environment.getExternalStorageDirectory().getAbsolutePath() + "";
//	File outputPath2;
	List<File> filetoDelete = new ArrayList<File>();
	
	public void setUp() throws Exception {
		super.setUp();
	}
	
	public void tearDown() throws Exception {
		for (File f : filetoDelete) {
			f.delete();
		}
		
		super.tearDown();
	}
	
	
	private File createTempFile(String tempFileName) {
		File f =   new File(Environment.getExternalStorageDirectory(), tempFileName);
		filetoDelete.add(f);
		return f;
	}
	
	
	public void testCompress() throws Exception {
		File outputPath = createTempFile("testTarget.jpg");//new File(Environment.getExternalStorageDirectory(), "testTarget.jpg");
		
		Bitmap bp = BitmapFactory.decodeResource(this.getContext().getResources(), R.drawable.ic_launcher);
		bp.compress(CompressFormat.JPEG, 100, new FileOutputStream(outputPath));
		
		ExifInterface exif = new ExifInterface(outputPath.getAbsolutePath());
		assertEquals(ExifInterface.ORIENTATION_UNDEFINED, exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, Integer.MAX_VALUE));
		
		exif.setAttribute(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_270 + "");
		exif.saveAttributes();
		
		
		File f2 = createTempFile("testTarget2");
		Bitmap newBp = BitmapFactory.decodeFile(outputPath.getAbsolutePath());
		newBp.compress(CompressFormat.JPEG, 100, new FileOutputStream(f2));
		
		ExifInterface ii = new ExifInterface(f2.getAbsolutePath());
		assertEquals(ExifInterface.ORIENTATION_UNDEFINED, ii.getAttributeInt(ExifInterface.TAG_ORIENTATION, Integer.MAX_VALUE));
		
		File f3 = createTempFile("testTarget3");
		TestUtil.copy(outputPath, f3);
		ExifInterface jj = new ExifInterface(f3.getAbsolutePath());
		assertEquals(ExifInterface.ORIENTATION_ROTATE_270, jj.getAttributeInt(ExifInterface.TAG_ORIENTATION, Integer.MAX_VALUE));
	}

}
