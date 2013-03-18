package com.baixing.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NetworkProfiler {
	public static final String TAB = "    ";
	public static final int BUFFER_SIZE = 100;
	private static Map<String, ProfileRecord> recordMapper = new HashMap<String, ProfileRecord>();
	private static ArrayList<ProfileRecord> records = new ArrayList<ProfileRecord>();
	private static boolean enable = false;
	private static String outputPath;
	
	static class ProfileRecord {
		private String url;
		private long startTime;
		private long endTime;
		private String[] extralInfo;
		private int requestSize;
		private int responseSize;
		private boolean isDeprecated = false;
		
		ProfileRecord(long startTime, boolean deprecate) {
			this.startTime = startTime;
			isDeprecated = deprecate;
		}
		
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append(isDeprecated ? url + "_deprecated" : url).append(TAB).append(startTime).append(TAB).append(endTime).append(TAB).append(requestSize).append(TAB).append(responseSize).append(TAB);
			
			if (extralInfo != null) {
				for (String extra : extralInfo) {
					buf.append(extra).append(",");
				}
				
				if (buf.length() > 0) {
					buf.deleteCharAt(buf.length()-1);
				}
				
				buf.append(TAB);
			}
			
			buf.append("\r\n");
			
			return buf.toString();
		}
	}
	
	public static final void endable(String outputPath) {
		enable = true;
		NetworkProfiler.outputPath = outputPath;
	}
	
	public static final void disable() {
		enable = false;
		records.clear();
		recordMapper.clear();
	}
	
	public static void flush() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					ArrayList<ProfileRecord> list = new ArrayList<ProfileRecord>(records);
					records.clear();

					FileOutputStream fos = new FileOutputStream(new File(outputPath), true);
					writeTo(list, fos);
					fos.close();
				} catch (Throwable t) {

				}
			}

		});
		t.start();
	}
	
	public static final void startUrl(String url) {
		if (!enable) {
			return;
		}
		recordMapper.put(url, new ProfileRecord(System.currentTimeMillis(), recordMapper.containsKey(url)));
	}
	
	public static final void endUrl(String url, int requestSize, int responseSize, String... extralInfo) {
		if (!enable) {
			return;
		}
		
		ProfileRecord r = recordMapper.remove(url);
		if (r != null) {
			r.endTime = System.currentTimeMillis();
			r.requestSize = requestSize;
			r.responseSize = responseSize;
			r.extralInfo = extralInfo;
			r.url = url;
			records.add(r);
		}
		
		if (records.size() > BUFFER_SIZE) { 
			flush();
		}
	}
	
	public static void writeTo(OutputStream os, boolean clear) {
		writeTo(records, os);
		if (clear) {
			records.clear();
		}
	}
	
	private static void writeTo(List<ProfileRecord> list, OutputStream os) {
		for (ProfileRecord r : list) {
			try {
				os.write(r.toString().getBytes());
			} catch (IOException e) {
				//Ignor
			}
		}
	}
}
