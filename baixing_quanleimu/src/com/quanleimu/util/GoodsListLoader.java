package com.quanleimu.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import android.os.Handler;
import com.quanleimu.entity.GoodsList;

public class GoodsListLoader {
	private GoodsList mGoodsList = null;//only an outlet here: set yours (if not set, we'll create one empty for you), and pass it out to others
	private String mUrl = "";
	private String mFields = "";
	private boolean mIsFirst = true;
	private boolean mHasMore = true;
	private int mCurIndex = 0;
	private int mRows = 30;
	private Handler mHandler = new Handler();
	
	private static String mApiName = "ad_list";
	
	private static String mLastJson = null;
	
	
	public final static int ERROR_FIRST = 0;
	public final static int ERROR_MORE = 1;
	public final static int ERROR_NOMORE = 2;
	
	public GoodsListLoader(String url, Handler handler, String fields, GoodsList goodsList){
		mUrl = url;
		
		mHandler = handler;
		
		if(null != fields){
			mFields = fields;
		}
		
		if(null != mGoodsList){
			mGoodsList = goodsList;
		}
	}
	
	public void setUrl(String url){
		mUrl = url;
	}
	
	public void setHandler(Handler handler){
		mHandler = handler;
	}

	public void setRows(int rows){
		mRows = rows;
	}
	
	public String getLastJson(){
		return mLastJson;
	}
	
	public void setGoodsList(GoodsList list){
		mGoodsList = list;
	}
	
	public GoodsList getGoodsList(){
		if(null ==  mGoodsList){
			mGoodsList = new GoodsList();
		}
		
		return mGoodsList;
	}

	public void setHasMore(boolean hasMore){
		mHasMore = hasMore;
	}
	
	public boolean hasMore(){
		return mHasMore;
	}
	
	public int getSelection(){
		return mCurIndex;
	}
	
	public void setSelection(int selection){
		mCurIndex = selection;
	}
	
	public void startFetching(boolean isFirst){
		mIsFirst = isFirst;
		new Thread(new GetmGoodsListThread()).start();
	}	
	
	public void startFetching(boolean isFirst, int errFirst, int errMore, int errNoMore){
		mIsFirst = isFirst;
		new Thread(new GetmGoodsListThread(errFirst, errMore, errNoMore)).start();		
	}
	
	class GetmGoodsListThread implements Runnable {
		private int errorFirst = GoodsListLoader.ERROR_FIRST;
		private int errorMore = GoodsListLoader.ERROR_MORE;
		private int errorNoMore = GoodsListLoader.ERROR_NOMORE;
		
		GetmGoodsListThread(){}
		
		GetmGoodsListThread(int errFirst, int errMore, int errNoMore){
			errorFirst = errFirst;
			errorMore = errMore;
			errorNoMore = errNoMore;
		}
		
		@Override
		public void run() {
			ArrayList<String> list = new ArrayList<String>();

			if(null != mFields && mFields.length() > 0)
				list.add("fields=" + URLEncoder.encode(mFields));
			
			list.add(mUrl);			
			list.add("start=" + (mIsFirst ? 0 : mGoodsList.getData().size()));
			
			if(mRows > 0)
				list.add("rows=" + mRows);

			String url = Communication.getApiUrl(mApiName, list);
			try {
				mLastJson = Communication.getDataByUrl(url);

				if (mLastJson != null) {
					if (!mIsFirst) {
						mHandler.sendEmptyMessage(errorMore);
					} else {
						mIsFirst = false;
						mHandler.sendEmptyMessage(errorFirst);
					}

				} else {
					mHandler.sendEmptyMessage(errorNoMore);
				}
			} catch (UnsupportedEncodingException e) {
				mHandler.sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
			} catch (IOException e) {
				mHandler.sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
			} catch (Communication.BXHttpException e){
				
			}
		}
	}
}
