package com.baixing.network.impl;

import java.io.FileInputStream;
import java.io.OutputStream;

public class FileUploadRequest extends BaseHttpRequest {

	public static final String BOUNDARY = "---------------------------19861025304733";
	
	private String filePath;
	public FileUploadRequest(String url, String filePath) {
		super(url);
		this.filePath = filePath;
	}
	
	private static String getMimeType(byte[] fileContentBuf) {
		String suffix = getFileSuffix(fileContentBuf);

		return getMimeType(suffix);
	}
	
	private static String getMimeType(String suffix) {
		String mimeType;
		
		if ("JPG".equals(suffix)) {
			mimeType = "image/jpeg";
		} else if ("GIF".equals(suffix)) {
			mimeType = "image/gif";
		} else if ("PNG".equals(suffix)) {
			mimeType = "image/png";
		} else if ("BMP".equals(suffix)) {
			mimeType = "image/bmp";
		} else {
			mimeType = "application/octet-stream";
		}
		
		return mimeType;
	}
	
	private static String getFileSuffix(byte[] bytes) {
		if (bytes == null || bytes.length < 10) {
			return null;
		}

		if (bytes[0] == 'G' && bytes[1] == 'I' && bytes[2] == 'F') {
			return "GIF";
		} else if (bytes[1] == 'P' && bytes[2] == 'N' && bytes[3] == 'G') {
			return "PNG";
		} else if (bytes[6] == 'J' && bytes[7] == 'F' && bytes[8] == 'I'
				&& bytes[9] == 'F') {
			return "JPG";
		} else if (bytes[0] == 'B' && bytes[1] == 'M') {
			return "BMP";
		} else {
			return null;
		}
	}

	@Override
	public String getContentType() {
		return "multipart/form-data; boundary=" + BOUNDARY;
	}

	@Override
	public boolean isGetRequest() {
		return false;
	}

	@Override
	public int writeContent(OutputStream out) {

		StringBuffer boundary = new StringBuffer();
		boundary = boundary.append("--");
		boundary = boundary.append(BOUNDARY);
		boundary = boundary.append("\r\n");
		
		
		boolean firstTime = true;
		int contentLen = 0;
		try {
			FileInputStream ins = new FileInputStream(filePath);
			byte[] buffer = new byte[4096];
			int count = 0;
			do {
				count = ins.read(buffer);
				if (firstTime) {
					String fileSuffix = getFileSuffix(buffer);
					String fileMime = getMimeType(fileSuffix);
					boundary = boundary.append("Content-Disposition: form-data; name=\"file\"; filename=" + "\"iphonefile." + fileSuffix + "\"\r\n");//TODO: does file name matters?
					boundary = boundary.append("Content-Type: " + fileMime + "\r\n\r\n");
					
					byte[] boundaryStart = boundary.toString().getBytes();
					out.write(boundaryStart);
					firstTime = false;
					contentLen += boundaryStart.length;
				}
				
				if (count > 0) {
					out.write(buffer, 0, count);
					contentLen += count;
				}
			} while (count > 0);
			ins.close();
			
			byte[] boundaryEnd = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();
			out.write(boundaryEnd);
			contentLen += boundaryEnd.length;
		} catch (Throwable t) {
			//Ignor exceptions.
		}
		
		return contentLen;
	}

	@Override
	public CACHE_POLICY getCachePolicy() {
		return CACHE_POLICY.CACHE_NOT_CACHEABLE;
	}

}
