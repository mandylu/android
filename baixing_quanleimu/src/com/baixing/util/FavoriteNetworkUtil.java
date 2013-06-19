package com.baixing.util;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.baixing.data.GlobalDataManager;
import com.baixing.entity.Ad;
import com.baixing.entity.Ad.EDATAKEYS;
import com.baixing.entity.AdList;
import com.baixing.entity.UserBean;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.network.api.ApiError;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.network.api.BaseApiCommand.Callback;

public class FavoriteNetworkUtil{
	public static final int MSG_CANCEL_FAVORITE_SUCCESS = 0x55550001;
	public static final int MSG_CANCEL_FAVORITE_FAIL = 0x55550002;
	public static final int MSG_ADD_FAVORITE_SUCCESS = 0x55550003;
	public static final int MSG_ADD_FAVORITE_FAIL = 0x55550004;
	
	public static class ReplyData extends Object{
		public String id;
		public String response;
		public ReplyData(String id, String response){
			this.id = id;
			this.response = response;
		}
	}
	
	public static void addFavorite(Context ctx, final String ids, UserBean user, final Handler outHandler){
    	ApiParams params = new ApiParams();
    	params.addParam("adIds", ids);
    	params.addParam("rt", 1);
    	if(user != null && user.getPhone() != null && !user.getPhone().equals("")){
    		params.appendAuthInfo(user.getPhone(), user.getPassword());//.appendUserInfo(user);
		}
    	
    	BaseApiCommand.createCommand("add_favourites", false, params).execute(ctx, new Callback() {
			
			@Override
			public void onNetworkFail(String apiName, ApiError error) {
				Message msg = Message.obtain();
				msg.what = MSG_ADD_FAVORITE_FAIL;
				msg.obj = error == null ? "" : error.getMsg();
				if(outHandler != null){
					outHandler.sendMessage(msg);
				}
			}
			
			@Override
			public void onNetworkDone(String apiName, String responseData) {
				Message msg = Message.obtain();
				msg.obj = new ReplyData(ids, responseData);
				msg.what = MSG_ADD_FAVORITE_SUCCESS;
				if(outHandler != null){
					outHandler.sendMessage(msg);
				}
			}
		});
	}
	
    public static void cancelFavorite(Context ctx, final String id, UserBean user, final Handler outHandler){
    	ApiParams params = new ApiParams();
    	params.addParam("adId", id);
    	params.addParam("rt", 1);
    	if(user != null && user.getPhone() != null && !user.getPhone().equals("")){
    		params.appendAuthInfo(user.getPhone(), user.getPassword());//.appendUserInfo(user);
		}
    	
    	BaseApiCommand.createCommand("remove_favourites", false, params).execute(ctx, new Callback() {
			
			@Override
			public void onNetworkFail(String apiName, ApiError error) {
				Message msg = Message.obtain();
				msg.what = MSG_CANCEL_FAVORITE_FAIL;
				msg.obj = error == null ? "" : error.getMsg();
				outHandler.sendMessage(msg);
			}
			
			@Override
			public void onNetworkDone(String apiName, String responseData) {
				Message msg = Message.obtain();
				msg.obj = new ReplyData(id, responseData);
				msg.what = MSG_CANCEL_FAVORITE_SUCCESS;
				outHandler.sendMessage(msg);
			}
		});
    }
    
    static public void retreiveFavorites(Context ctx, UserBean user){
    	ApiParams params = new ApiParams();
    	params.addParam("userId", user.getId());
    	BaseApiCommand.createCommand("get_favourites", false, params).execute(ctx, new Callback(){

			@Override
			public void onNetworkDone(String apiName, String responseData) {
				// TODO Auto-generated method stub
		    	AdList gl = JsonUtil.getGoodsListFromJson(responseData);
		    	if(gl != null){
		    		GlobalDataManager.getInstance().updateFav(gl.getData());
		    	}
			}

			@Override
			public void onNetworkFail(String apiName, ApiError error) {
				// TODO Auto-generated method stub
				
			}
    		
    	});
    }
    
	static public void syncFavorites(final Context ctx, UserBean user){
    	Ad[] listStore = (Ad[]) Util.loadDataFromLocate(ctx, "listMyStore", Ad[].class);
    	if(listStore == null || listStore.length == 0 || user == null || TextUtils.isEmpty(user.getPhone())) return;
    	String ids = "";
    	for(int i = 0; i < listStore.length; ++ i){
    		ids += listStore[i].getValueByKey(EDATAKEYS.EDATAKEYS_ID);
    		if(i != listStore.length - 1){
    			ids += ",";
    		}
    	}
    	
    	FavoriteNetworkUtil.addFavorite(ctx, ids, user, new Handler(ctx.getMainLooper()){
    		@Override
    		public void handleMessage(Message msg){
    			if(msg.what == MSG_ADD_FAVORITE_SUCCESS){
    				Util.deleteDataFromLocate(ctx, "listMyStore");
    			}
    		}
    	});
//    	Looper.loop();
    }
}