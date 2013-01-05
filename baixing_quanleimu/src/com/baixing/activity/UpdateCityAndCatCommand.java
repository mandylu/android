package com.baixing.activity;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.baixing.android.api.ApiError;
import com.baixing.android.api.ApiParams;
import com.baixing.android.api.cmd.BaseCommand;
import com.baixing.android.api.cmd.BaseCommand.Callback;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.CityList;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.util.MobileConfig;
import com.baixing.util.Util;

/**
 * 
 * @author liuchong
 *
 */
public class UpdateCityAndCatCommand implements Callback {
	
	public static final int NETWORK_REQ_GET_CITY_LIST = 1;
	public static final int NETWORK_REQ_GET_CATEGORY_LIST = 2;
	
	private Context context;
	
	private long cityUpdateTime;
	private long cateUpdateTime;
	
	UpdateCityAndCatCommand(Context cxt) {
		this.context = cxt;
	}

	public void execute(){
		//check and update city list.
		Pair<Long, String> pair = Util.loadJsonAndTimestampFromLocate(this.context, "cityjson");
		long timestamp = pair.first;
		String content = pair.second;
		
		 cityUpdateTime = MobileConfig.getInstance().getCityTimestamp();
		if (timestamp < cityUpdateTime || content == null || content.length() == 0) {
			BaseCommand.createCommand(NETWORK_REQ_GET_CITY_LIST, "city_list", null).execute(this);
		}
	
		// check and update category list
		Pair<Long, String> firstCatePair = Util.loadJsonAndTimestampFromLocate(context, "saveFirstStepCate");
		
		String categoryContent = firstCatePair.second;
		timestamp = firstCatePair.first;
		cateUpdateTime = MobileConfig.getInstance().getCategoryTimestamp();
		if (timestamp < cateUpdateTime || TextUtils.isEmpty(categoryContent)) {
			ApiParams params = new ApiParams();
			params.addParam("cityEnglishName", GlobalDataManager.getInstance().getCityEnglishName());
			
			BaseCommand.createCommand(NETWORK_REQ_GET_CATEGORY_LIST, "category_list", params).execute(this);
		}
	}

	@Override
	public void onNetworkDone(int requstCode, String responseData) {
		switch(requstCode) {
		case NETWORK_REQ_GET_CITY_LIST:
			if (!TextUtils.isEmpty(responseData)) 
			{
				CityList cityList = JsonUtil.parseCityListFromJson(responseData);
				Util.saveJsonAndTimestampToLocate(context, "cityjson", responseData, cityUpdateTime);							
				GlobalDataManager.getInstance().updateCityList(cityList);
			}
			break;
		case NETWORK_REQ_GET_CATEGORY_LIST:
			if (!TextUtils.isEmpty(responseData)) {
				Util.saveJsonAndTimestampToLocate(context, "saveFirstStepCate", responseData, cateUpdateTime);
			}

			break;
		}
	}

	@Override
	public void onNetworkFail(int requstCode, ApiError error) {
		Log.e("QLM", "fail to update " + (requstCode == NETWORK_REQ_GET_CATEGORY_LIST ? "categoty list" : "city list"));
	}

	
}
