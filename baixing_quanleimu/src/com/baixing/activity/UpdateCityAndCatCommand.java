package com.baixing.activity;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.baixing.data.GlobalDataManager;
import com.baixing.entity.CityList;
import com.baixing.jsonutil.JsonUtil;
import com.baixing.network.api.ApiError;
import com.baixing.network.api.ApiParams;
import com.baixing.network.api.BaseApiCommand;
import com.baixing.network.api.BaseApiCommand.Callback;
import com.baixing.util.MobileConfig;
import com.baixing.util.PerformEvent.Event;
import com.baixing.util.PerformanceTracker;
import com.baixing.util.Util;

/**
 * 
 * @author liuchong
 *
 */
public class UpdateCityAndCatCommand implements Callback {
	
	private Context context;
	
	private long cityUpdateTime;
	private long cateUpdateTime;
	
	UpdateCityAndCatCommand(Context cxt) {
		this.context = cxt;
	}

	public void execute(){
		//check and update city list.
		PerformanceTracker.stamp(Event.E_Begin_UpdateCityAndCat);
		Pair<Long, String> pair = Util.loadJsonAndTimestampFromLocate(this.context, "cityjson");
		long timestamp = pair.first;
		String content = pair.second;
		
		 cityUpdateTime = MobileConfig.getInstance().getCityTimestamp();
		if (timestamp < cityUpdateTime || content == null || content.length() == 0) {
			BaseApiCommand.createCommand("city_list", true, null).execute(this.context, this);
		}
	
		// check and update category list
		Pair<Long, String> firstCatePair = Util.loadJsonAndTimestampFromLocate(context, "saveFirstStepCate");
		
		String categoryContent = firstCatePair.second;
		timestamp = firstCatePair.first;
		cateUpdateTime = MobileConfig.getInstance().getCategoryTimestamp();
		if (timestamp < cateUpdateTime || TextUtils.isEmpty(categoryContent)) {
			ApiParams params = new ApiParams();
			params.addParam("cityEnglishName", GlobalDataManager.getInstance().getCityEnglishName());
			
			BaseApiCommand.createCommand("category_list", true, params).execute(context, this);
		}
	}


	@Override
	public void onNetworkDone(String apiName, String responseData) {
		if ("city_list".equals(apiName)) {
			if (!TextUtils.isEmpty(responseData)) {
				CityList cityList = JsonUtil
						.parseCityListFromJson(responseData);
				Util.saveJsonAndTimestampToLocate(context, "cityjson",
						responseData, cityUpdateTime);
				GlobalDataManager.getInstance().updateCityList(cityList);
			}
			PerformanceTracker.stamp(Event.E_UpdateCity_Done);
		}
		else if ("category_list".equals(apiName)) {
			if (!TextUtils.isEmpty(responseData)) {
				Util.saveJsonAndTimestampToLocate(context, "saveFirstStepCate",
						responseData, cateUpdateTime);
			}
			PerformanceTracker.stamp(Event.E_UpdateCat_Done);
		}
	}

	@Override
	public void onNetworkFail(String apiName, ApiError error) {
		Log.e("QLM", "fail to update " + apiName);
		PerformanceTracker.stamp(Event.E_UpdateCityAndCat_FAIL);
		
	}

	
}
