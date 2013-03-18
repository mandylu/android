//liuchong@baixing.com
package com.baixing.data;

import java.util.ArrayList;
import java.util.List;

import com.baixing.network.ICacheProxy;
import com.baixing.util.Util;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Pair;

public class NetworkCacheManager implements ICacheProxy {
	
	protected final List<Pair<String, String>> storeList = new ArrayList<Pair<String,String>>();
	private BXDatabaseHelper dbManager = null;
	
	static NetworkCacheManager createInstance(Context context) {
		return new NetworkCacheManager(context);
	}
	
	private NetworkCacheManager(Context context) {
		
		dbManager = new BXDatabaseHelper(context, "network.db", null, 2);
		
		/**
    	 * do IO on network request will prolong user's time waiting network. This thread do simple IO work on a separate thread.
    	 */
    	Thread t = new Thread(new Runnable() {
			
			public void run() {
				while(true) {
					Pair<String, String> item = null;
					synchronized (storeList) {
						if (storeList.size() > 0)
						{
							item = storeList.remove(0);
						}
						else
						{
							try {
								storeList.wait(5 * 60 * 1000);
//								Log.d("QLMAPP", "wakeup to handle store");
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					
					if (item != null)
					{
//						Log.d("QLMAPP", "oh yeah, store it.");
						storeCacheNetworkRequest(item.first, item.second);
					}
				}
			}
		});
    	t.start();
	}
	
	public void ClearCache(){
		SQLiteDatabase db = dbManager.getWritableDatabase();
		try{
			db.execSQL("DELETE from " + BXDatabaseHelper.TABLENAME, new String[]{});
			
		}catch(SQLException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		db.close();		
	}
	
	public void putCacheNetworkRequest(String request, String result){
		request = Util.extractUrlWithoutSecret(request);
		synchronized (storeList) {
			storeList.add(Pair.create(request, result));
			storeList.notifyAll();
		}
	}
	
	private void storeCacheNetworkRequest(String request, String result){
		synchronized(dbManager){
			SQLiteDatabase db = null; 
			try{
				db = dbManager.getWritableDatabase();
				String timestamp = String.valueOf(System.currentTimeMillis()/1000);
				db.execSQL("insert into " + BXDatabaseHelper.TABLENAME + "(url, response, timestamp) values(?,?,?)", new String[]{request, result, timestamp});
			}catch(SQLException e){
				e.printStackTrace();
			}
			if(db != null){
				db.close();
			}
		}
	}
	
	public String getCacheNetworkRequest(String request){
		
		request = Util.extractUrlWithoutSecret(request);
		synchronized(dbManager){
			String response = null;
			SQLiteDatabase db = null;
			try{
				db = dbManager.getReadableDatabase();
				
				Cursor c = db.rawQuery("SELECT * from " + BXDatabaseHelper.TABLENAME + " WHERE url=?", new String[]{request});
				
				while(c.moveToNext()){
					int index = c.getColumnIndex("response");
					if(index >= 0){
						response = c.getString(index);
						break;
					}
				}
				c.close();
			}catch(SQLException e){
				e.printStackTrace();
			}catch(Throwable e){
				e.printStackTrace();
			}
			if(db != null){
				db.close();
			}
			return response;
		}
		
	}
	
	public void deleteOldRecorders(int intervalInSec){
		SQLiteDatabase db = null;
		try{
			db = dbManager.getWritableDatabase();
			db.execSQL("DELETE from " + BXDatabaseHelper.TABLENAME + " WHERE timestamp<?", new String[]{String.valueOf(System.currentTimeMillis()/1000 - intervalInSec)});
			
		}catch(SQLException e){
			e.printStackTrace();
		}catch(Exception e){
			e.printStackTrace();
		}
		if(db != null){
			db.close();
		}
	}
	

	//from ApiClient.CacheProxy
	public void onSave(String url, String jsonStr){
		this.putCacheNetworkRequest(url, jsonStr);
	}
	public String onLoad(String url){
		return this.getCacheNetworkRequest(url);		
	}
	
	
}
