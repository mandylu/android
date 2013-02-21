package com.baixing.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import android.os.Environment;

public class PerformanceTracker{
	private ArrayList<String> records = new ArrayList<String>();
	
	private static final int MAX_RECORDS = 50;
	
	private PerformanceTracker(){
		
	}
	
//	public static int getLineNumber(){
//		return ;
//	}
//	
//	static public String getFileName(){
//		return ;
//	}
//	
	private static PerformanceTracker instance;
	
	static public PerformanceTracker getInstance(){
		if(instance == null){
			instance =  new PerformanceTracker();
		}
		return instance;
	}
	
	private void record(String record){
		synchronized(getInstance()){
			records.add(record);
		}
		if(records.size() >= MAX_RECORDS){
			((new Thread(new Runnable(){
				@Override
				public void run(){
					flush();
				}
			}))).start();
		}
	}
	
	public static void flush(){
		synchronized(getInstance()){
			FileOutputStream os = null;
			try {
				File file = new File(Environment.getExternalStorageDirectory(), "/performance.txt");
				os = new FileOutputStream(file, true);
				for(int i = 0; i < getInstance().records.size(); ++ i){
					try {
						os.write(getInstance().records.get(i).getBytes());
						os.write("\n".getBytes());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch(Exception e){
				e.printStackTrace();
			}finally{
				if(os != null){
					try {
						os.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			getInstance().records.clear();
		}
	}
	
	public static void stamp(PerformEvent.Event event){//, String file, int line, long timestamp){
		getInstance().record(event + "," + Thread.currentThread().getStackTrace()[3].getFileName() + "," + Thread.currentThread().getStackTrace()[3].getLineNumber() + "," + String.valueOf(System.currentTimeMillis()));
	}
}