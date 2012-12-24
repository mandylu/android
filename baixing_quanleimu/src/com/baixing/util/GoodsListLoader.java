//liuchong@baixing.com
package com.baixing.util;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.baixing.entity.GoodsList;
import com.baixing.android.api.ApiClient;
import com.baixing.android.api.ApiError;
import com.baixing.android.api.ApiListener;
import com.baixing.android.api.ApiParams;
import com.baixing.activity.QuanleimuApplication;

public class GoodsListLoader implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4876054768912906374L;

	public interface HasMoreListener{
		public void onHasMoreStatusChanged();
	};
	
	private GoodsList mGoodsList = null;//only an outlet here: set yours (if not set, we'll create one empty for you), and pass it out to others
	private ApiParams params = null;
	private String mFields = "";
	private boolean mIsFirst = true;
	private boolean mHasMore = true;
	private int mCurIndex = 0;
	private int mRows = 30;
	private transient Handler mHandler = new Handler();
	
	private static String mApiName = "ad_list";
	private static String mNearbyApiName = "ad_nearby";
	
	private static String mLastJson = null;
	
	private transient GetGoodsListThread mCurThread = null;
	
	private transient HasMoreListener hasMoreListener = null;
	
	public enum E_LISTDATA_STATUS{
		E_LISTDATA_STATUS_OFFLINE,
		
		E_LISTDATA_STATUS_ONLINE
	};
	private E_LISTDATA_STATUS mStatusListdataExisting = E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE;
	private E_LISTDATA_STATUS mStatusListdataRequesting = E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE;
	
	public final static int MSG_FINISH_GET_FIRST = 0;
	public final static int MSG_FINISH_GET_MORE = 1;
	public final static int MSG_NO_MORE = 2;
	public final static int MSG_FIRST_FAIL = 0x0FFFFFFF;
	public final static int MSG_EXCEPTION = 0xFFFFFFFF;
	private boolean mRt = true;
	
	public void reset(){
		mGoodsList = null;
		this.mLastJson = null;
		this.mHandler = null;
	}
	
	public GoodsListLoader(ApiParams params, Handler handler, String fields, GoodsList goodsList){
		this.params = params;
		
		mHandler = handler;
		
		if(null != fields){
			mFields = fields;
		}
		
		if(null != goodsList){
			mGoodsList = goodsList;
		}
	}
	
	public void setParams(ApiParams params){
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
		if(mHasMore != hasMore){
			mHasMore = hasMore;
			if(null != hasMoreListener){
				hasMoreListener.onHasMoreStatusChanged();
			}
		}
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
	
	public void setHasMoreListener(HasMoreListener listener){
		hasMoreListener = listener;
	}
	
	public E_LISTDATA_STATUS getDataStatus(){
		return mStatusListdataExisting;
	}
	
	public E_LISTDATA_STATUS getRequestDataStatus(){
		return mStatusListdataRequesting;
	}
	
	private boolean isNearby = false;
	public void setNearby(boolean isNearby){
		this.isNearby = isNearby;
	}
	
	public void startFetching(boolean isFirst, Communication.E_DATA_POLICY dataPolicy_){
		cancelFetching();
		
		mStatusListdataRequesting = ((dataPolicy_==Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL) ? E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE : E_LISTDATA_STATUS.E_LISTDATA_STATUS_ONLINE);
		
		mIsFirst = isFirst;

		mCurThread = new GetGoodsListThread(dataPolicy_, isNearby, isUserList);		
		new Thread(mCurThread).start();	
	}	
	
	public void startFetching(boolean isFirst, int msgGotFirst, int msgGotMore, int msgNoMore, Communication.E_DATA_POLICY dataPolicy_){
		cancelFetching();
		
		mStatusListdataRequesting = ((dataPolicy_==Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL) ? E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE : E_LISTDATA_STATUS.E_LISTDATA_STATUS_ONLINE);
		
		mIsFirst = isFirst;
		mCurThread = new GetGoodsListThread(msgGotFirst, msgGotMore, msgNoMore, dataPolicy_, isNearby, isUserList);		
		
		new Thread(mCurThread).start();	
	}
	
	public void setRuntime(boolean rt){
		mRt = rt;
	}
	
	private boolean isUserList = false;
	
	public void setSearchUserList(boolean isUserList){
		this.isUserList = isUserList;
	}
	
	class GetGoodsListThread implements Runnable, ApiListener {
		private int msgFirst = GoodsListLoader.MSG_FINISH_GET_FIRST;
		private int msgMore = GoodsListLoader.MSG_FINISH_GET_MORE;
		private int msgNoMore = GoodsListLoader.MSG_NO_MORE;
		private Communication.E_DATA_POLICY dataPolicy = Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL;
		
		private boolean mCancel = false;
		private HttpClient mHttpClient = null;
		private boolean isNearby = false;
		private boolean isUserList = false;
		
		private void initApi(){
			String udid = Util.getDeviceUdid(QuanleimuApplication.getApplication().getApplicationContext());
			//FIXME: move following code to app entry
			ApiClient.getInstance().init(QuanleimuApplication.getApplication().getApplicationContext(),
					udid, 
					QuanleimuApplication.version, 
					QuanleimuApplication.channelId,
					QuanleimuApplication.getApplication().cityEnglishName,
					QuanleimuApplication.getApplication());
		}
		
		GetGoodsListThread(Communication.E_DATA_POLICY dataPolicy_, boolean isNearby, boolean isUserList){
			dataPolicy = dataPolicy_;
			this.isNearby = isNearby;
			this.isUserList = isUserList;
			
			initApi();
		}
		
		GetGoodsListThread(int errFirst, int errMore, int errNoMore, Communication.E_DATA_POLICY dataPolicy_, boolean isNearby, boolean isUserList){
			msgFirst = errFirst;
			msgMore = errMore;
			msgNoMore = errNoMore;
			dataPolicy = dataPolicy_;
			this.isNearby = isNearby;
			this.isUserList = isUserList;
			
			initApi();
		}
		
		public void cancel(){
			mCancel = true;
		}
		
		private void exit(){
			GoodsListLoader.this.mCurThread = null;
		}
		
		private boolean wantedExists(ApiParams list){
			Log.d("wantedExist", list.toString());
			return (list.getParam("wanted") != null);
		}
		
		public void start(){
			run();
		}
		
		@Override
		public void run() {
			
			if(mCancel) {
				exit();
				return;
			}
			ApiParams list = new ApiParams();
			if(this.dataPolicy == Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL){
				list.useCache = true;
			}
			if(params != null){
				list.setParams(params.getParams());
			}
			
			if(null != mFields && mFields.length() > 0)
				list.addParam("fields",mFields);
			list.addParam("start",  "" + (mIsFirst ? 0 : mGoodsList.getData().size()));
			if(mRt){
				list.addParam("rt","1");
			}
			if(mRows > 0)
				list.addParam("rows", "" + mRows);
			
			
			if(!this.wantedExists(list)){//如果没有显式的制定wanted，那么默认wanted＝0, 只显示 “转让信息”
				list.addParam("wanted","0");
			}
			
			if(mCancel) {
				exit();
				return;
			}
			
			String method = this.isNearby ? mNearbyApiName : (isUserList ? "ad_user_list" : mApiName);
			ApiClient.getInstance().remoteCall(method, list, this);
		}
		
		
		public void onComplete(JSONObject json, String rawData){
			mLastJson = rawData;
			if (mLastJson != null && !mLastJson.equals("")) {
				if (!mIsFirst) {
					if(mHandler != null){
						mHandler.sendEmptyMessage(msgMore);
					}
				} else {
					mIsFirst = false;
					if(mHandler != null){
						mHandler.sendEmptyMessage(msgFirst);
					}
				}
				
				//only when data is valid, do we need to update listdata status
				GoodsListLoader.this.mStatusListdataExisting = (dataPolicy == Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE/* || dataPolicy == Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_UNCACHEABLE*/) ?
														E_LISTDATA_STATUS.E_LISTDATA_STATUS_ONLINE : (dataPolicy == Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL) ?
														E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE : 	GoodsListLoader.this.mStatusListdataExisting;
			} else {
				if(!mIsFirst){
					if(mHandler != null){
						mHandler.sendEmptyMessage(msgNoMore);
					}
				}else{
					if(mHandler != null){
						mHandler.sendEmptyMessage(MSG_FIRST_FAIL);
					}
				}
			}
			
		}
		public void onError(ApiError error){
			
		}
		public void onException(Exception e){
			if(!mCancel){
				if(mHandler != null){
					mHandler.sendEmptyMessage(ErrorHandler.ERROR_NETWORK_UNAVAILABLE);
				}
			}
		}
	}
	
	
}
