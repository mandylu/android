package com.baixing.util;

import java.io.Serializable;

import android.content.Context;
import android.util.Log;

import com.baixing.entity.AdList;
import com.baixing.network.api.ApiError;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;

public class  VadListLoader implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4876054768912906374L;

	public interface HasMoreListener{
		public void onHasMoreStatusChanged();
	};
	
	private AdList mGoodsList = null;//only an outlet here: set yours (if not set, we'll create one empty for you), and pass it out to others
	private ApiParams params = null;
	private String mFields = "";
	private boolean mIsFirst = true;
	private boolean mHasMore = true;
	private int mCurIndex = 0;
	private int mRows = DEFAULT_ROWS;
	private static int DEFAULT_ROWS = 30;
//	private transient Handler mHandler = new Handler();
	private transient Callback callback;
	
	private static String mApiName = "ad_list";
	private static String mNearbyApiName = "ad_nearby";
	private static String mAroundApiName = "getAroundAds";
	
	private String mLastJson = null; //Should this be static?
	
	private transient GetListCommand currentCommand = null;
	
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
	
	public static interface Callback {
		public void onRequestComplete(final int respCode, Object data);
	}
	
	public void reset(){
		mGoodsList = null;
		this.mLastJson = null;
		this.callback = null;
	}
	
	public VadListLoader(ApiParams params, Callback callback, String fields, AdList goodsList){
		this.params = params;

		this.callback = callback;
		
		if(null != fields){
			mFields = fields;
		}
		
		if(null != goodsList){
			mGoodsList = goodsList;
		}
	}
	
	public void setCallback(Callback callback) {
		this.callback = callback;
	}
	
	public void setParams(ApiParams params){
		this.params = params;
	}
	
	public void setRows(int rows){
		mRows = rows;
	}
	
	public int getRows(){
		return mRows;
	}
	
	public String getLastJson(){
		return mLastJson;
	}
	
	public void setGoodsList(AdList list){
		mGoodsList = list;
	}
	
	public AdList getGoodsList(){
		if(null ==  mGoodsList){
			mGoodsList = new AdList();
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
		if(null != currentCommand){
			currentCommand.cancel();
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
	
	public static enum SEARCH_POLICY {
		SEARCH_LISTING, 
		SEARCH_NEARBY,
		SEARCH_AROUND
	}
	
	private SEARCH_POLICY mPolicy = SEARCH_POLICY.SEARCH_LISTING;
	
	public void setSearchType(SEARCH_POLICY policy){
		mPolicy = policy;
	}
	
	public SEARCH_POLICY getSearchType(){
		return mPolicy;
	}
	
//	private boolean isNearby = false;
//	public void setNearby(boolean isNearby){
//		this.isNearby = isNearby;
//	}
//	
	public void startFetching(Context context, boolean isFirst, boolean useCache){
		cancelFetching();
		
		mStatusListdataRequesting = (useCache ? E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE : E_LISTDATA_STATUS.E_LISTDATA_STATUS_ONLINE);
		
		mIsFirst = isFirst;

		currentCommand = new GetListCommand(context, mPolicy, isUserList, useCache);		
		currentCommand.run();
	}	
	
	public void startFetching(Context context, boolean isFirst, int msgGotFirst, int msgGotMore, int msgNoMore, boolean useCache){
		cancelFetching();
		
		mStatusListdataRequesting = (useCache ? E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE : E_LISTDATA_STATUS.E_LISTDATA_STATUS_ONLINE);
		
		mIsFirst = isFirst;
		currentCommand = new GetListCommand(context, msgGotFirst, msgGotMore, msgNoMore, useCache, mPolicy, isUserList);		
		currentCommand.run();
	}
	
	public void setRuntime(boolean rt){
		mRt = rt;
	}
	
	private boolean isUserList = false;
	
	public void setSearchUserList(boolean isUserList){
		this.isUserList = isUserList;
	}
	
	class GetListCommand implements Runnable, BaseApiCommand.Callback {
		private int msgFirst = VadListLoader.MSG_FINISH_GET_FIRST;
		private int msgMore = VadListLoader.MSG_FINISH_GET_MORE;
		private int msgNoMore = VadListLoader.MSG_NO_MORE;
//		private Communication.E_DATA_POLICY dataPolicy = Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL;
		private boolean useCache;
		private Context context;
		
		private boolean mCancel = false;
//		private boolean isNearby = false;
		private boolean isUserList = false;
		
		private SEARCH_POLICY policy = SEARCH_POLICY.SEARCH_LISTING;
		
		GetListCommand(Context cxt, SEARCH_POLICY policy, boolean isUserList, boolean useCache){
			this.useCache = useCache;
			this.policy = policy;
			this.isUserList = isUserList;
			this.context = cxt;
		}
		
		GetListCommand(Context cxt, int errFirst, int errMore, int errNoMore, boolean useCache, SEARCH_POLICY policy, boolean isUserList){
			msgFirst = errFirst;
			msgMore = errMore;
			msgNoMore = errNoMore;
			this.useCache = useCache;
			this.policy = policy;
			this.isUserList = isUserList;
			this.context = cxt;
		}
		
		public void cancel(){
			mCancel = true;
		}
		
		private void exit(){
			VadListLoader.this.currentCommand = null;
		}
		
		private boolean wantedExists(ApiParams list){
			Log.d("wantedExist", list.toString());
			return (list.getParam("wanted") != null);
		}
		
		public void start(){
			run();
		}
		
		private int skipCount() {
			AdList list = getGoodsList();
			if (list == null || list.getData() == null) {
				return 0;
			}
			else {
				return list.getData().size();//getCount();
			}
		}
		
		@Override
		public void run() {
			
			if(mCancel) {
				exit();
				return;
			}
			ApiParams list = new ApiParams();
			list.useCache = this.useCache;
			
			if(params != null){
				list.setParams(params.getParams());
			}
			
			if(null != mFields && mFields.length() > 0)
				list.addParam("fields",mFields);
			list.addParam("start",  "" + (mIsFirst ? 0 : skipCount()));
			if(mRt){
				list.addParam("rt","1");
			}
			if(mRows <= 0){
				mRows = DEFAULT_ROWS;
			}
			list.addParam("rows", "" + mRows);
			
			
			if(!this.wantedExists(list)){//如果没有显式的制定wanted，那么默认wanted＝0, 只显示 “转让信息”
				list.addParam("wanted","0");
			}
			
			if(mCancel) {
				exit();
				return;
			}
			
			String method = 
					this.policy == SEARCH_POLICY.SEARCH_LISTING ? mApiName 
							: (policy == SEARCH_POLICY.SEARCH_NEARBY ? mNearbyApiName : mAroundApiName);
//			ApiClient.getInstance().remoteCall(Api.createGet(method), list, this);
			BaseApiCommand.createCommand(method, true, list).execute(context, this);
		}
		
		public void onNetworkDone(String apiName, String responseData) {
			mLastJson = responseData;
			if (mLastJson != null && !mLastJson.equals("")) {
				if (!mIsFirst) {
					if(callback != null){
						callback.onRequestComplete(msgMore, mLastJson);
					}
				} else {
					mIsFirst = false;
					if(callback != null){
//						mHandler.sendEmptyMessage(msgFirst);
						callback.onRequestComplete(msgFirst, mLastJson);
					}
				}
				
				//only when data is valid, do we need to update listdata status //FIXME: need to chech refector result.
				VadListLoader.this.mStatusListdataExisting = useCache ? E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE
						: E_LISTDATA_STATUS.E_LISTDATA_STATUS_ONLINE;
//						(dataPolicy == Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_CACHEABLE/* || dataPolicy == Communication.E_DATA_POLICY.E_DATA_POLICY_NETWORK_UNCACHEABLE*/) ?
//														E_LISTDATA_STATUS.E_LISTDATA_STATUS_ONLINE : (dataPolicy == Communication.E_DATA_POLICY.E_DATA_POLICY_ONLY_LOCAL) ?
//														E_LISTDATA_STATUS.E_LISTDATA_STATUS_OFFLINE : 	VadListLoader.this.mStatusListdataExisting;
			} else {
				if(!mIsFirst){
					if(callback != null){
						callback.onRequestComplete(msgNoMore, null);
					}
				}else{
					if(callback != null){
						callback.onRequestComplete(MSG_FIRST_FAIL, null);
					}
				}
			}
			
		
		}
		
		public void onNetworkFail(String apiName, ApiError error) {
			if(!mCancel){
				if(callback != null){
					callback.onRequestComplete(ErrorHandler.ERROR_NETWORK_UNAVAILABLE, null);
				}
			}
		}
		
	}
	
}
