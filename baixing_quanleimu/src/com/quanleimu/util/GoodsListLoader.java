package com.quanleimu.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import com.quanleimu.entity.GoodsList;


public class GoodsListLoader {
	
	private GoodsList mGoodsList = null;//only an outlet here: set yours (if not set, we'll create one empty for you), and pass it out to others
	private List<String> params = null;
	private String mFields = "";
	private boolean mIsFirst = true;
	private boolean mHasMore = true;
	private int mCurIndex = 0;
	private int mRows = 30;
	private Handler mHandler = new Handler();
	
	private static String mApiName = "ad_list";
	
	private static String mLastJson = null;
	
	private GetmGoodsListThread mCurThread = null;
	
	
	public final static int MSG_FINISH_GET_FIRST = 0;
	public final static int MSG_FINISH_GET_MORE = 1;
	public final static int MSG_NO_MORE = 2;
	public final static int MSG_EXCEPTION = 0xFFFFFFFF;
	
	public GoodsListLoader(List<String> params, Handler handler, String fields, GoodsList goodsList){
		this.params = params;
		
		mHandler = handler;
		
		if(null != fields){
			mFields = fields;
		}
		
		if(null != mGoodsList){
			mGoodsList = goodsList;
		}
	}
	
	public void setParams(List<String> params){
		this.params = params;
	}
	
	public void setHandler(Handler handler){
		if(mHandler != handler){
			cancelFetching();
			mHandler = handler;
		}
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
	
	public void cancelFetching(){
		if(null != mCurThread){
			mCurThread.cancel();
		}
	}
	
	public void startFetching(boolean isFirst){
		mIsFirst = isFirst;

		mCurThread = new GetmGoodsListThread();		
		new Thread(mCurThread).start();	
	}	
	
	public void startFetching(boolean isFirst, int msgGotFirst, int msgGotMore, int msgNoMore){
		mIsFirst = isFirst;
		mCurThread = new GetmGoodsListThread(msgGotFirst, msgGotMore, msgNoMore);		
		
		new Thread(mCurThread).start();	
	}
	
	class GetmGoodsListThread implements Runnable {
		private int msgFirst = GoodsListLoader.MSG_FINISH_GET_FIRST;
		private int msgMore = GoodsListLoader.MSG_FINISH_GET_MORE;
		private int msgNoMore = GoodsListLoader.MSG_NO_MORE;
		
		private boolean mCancel = false;
		
		GetmGoodsListThread(){}
		
		GetmGoodsListThread(int errFirst, int errMore, int errNoMore){
			msgFirst = errFirst;
			msgMore = errMore;
			msgNoMore = errNoMore;
		}
		
		public void cancel(){
			mCancel = true;
		}
		
		private void exit(){
			GoodsListLoader.this.mCurThread = null;
		}
		
		@Override
		public void run() {
			if(mCancel) {
				exit();
				return;
			}
			
			ArrayList<String> list = new ArrayList<String>();

			if(null != mFields && mFields.length() > 0)
				list.add("fields=" + URLEncoder.encode(mFields));
			
			if(params != null){
				list.addAll(params);
			}
			
			list.add("start=" + (mIsFirst ? 0 : mGoodsList.getData().size()));
			list.add("rt=1");
			if(mRows > 0)
				list.add("rows=" + mRows);

			if(mCancel) {
				exit();
				return;
			}
			
			String url = Communication.getApiUrl(mApiName, list);
			try {
				if(mCancel) {
					exit();
					return;
				}
				
				mLastJson = Communication.getDataByUrl(url);

				if(mCancel) {
					exit();
					return;
				}
				
				if (mLastJson != null) {
					if (!mIsFirst) {
						mHandler.sendEmptyMessage(msgMore);
					} else {
						mIsFirst = false;
						mHandler.sendEmptyMessage(msgFirst);
					}

				} else {
					mHandler.sendEmptyMessage(msgNoMore);
				}
				
				exit();
				return;
			} catch (UnsupportedEncodingException e) {
			} catch (IOException e) {
				mHandler.sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
				exit();
				return;
			} catch (Communication.BXHttpException e){
				
			}
			
			mHandler.sendEmptyMessage(MSG_EXCEPTION);
			exit();
		}
	}
}
