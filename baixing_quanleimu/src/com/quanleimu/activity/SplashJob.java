package com.quanleimu.activity;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.util.Pair;
import android.widget.Toast;

import com.quanleimu.entity.AllCates;
import com.quanleimu.entity.CityDetail;
import com.quanleimu.entity.CityList;
import com.quanleimu.entity.GoodsDetail;
import com.quanleimu.jsonutil.JsonUtil;
import com.quanleimu.util.Communication;
import com.quanleimu.util.Helper;
import com.quanleimu.util.LocationService;
import com.quanleimu.util.MobileConfig;
import com.quanleimu.util.Util;

public class SplashJob {

	public static interface JobDoneListener {
		public void onJobDone();
	}
	
	JobDoneListener jobListener;
	
	private BaseActivity parentActivity;
	
	private boolean isJobDone;
	private boolean isJobStarted;
	
	SplashJob(BaseActivity activity, JobDoneListener listener)
	{
		this.parentActivity = activity;
		this.jobListener = listener;
	}
	
	public void doSplashWork()
	{
		if (isJobStarted || isJobDone)
		{
			return;  //Avoid deprecate start.
		}
		
		isJobStarted = true;
		
		LocationService.getInstance().start(parentActivity, QuanleimuApplication.mDemoApp);
		QuanleimuApplication.udid = Util.getDeviceUdid(parentActivity);

		QuanleimuApplication.version = Util.getVersion(parentActivity);

		try {
			if (parentActivity.checkConnection() == false) {
				Toast.makeText(parentActivity, "网络连接异常", 3).show();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		MobileConfig.getInstance().syncMobileConfig();
		
		new Thread(new ReadCityListThread()).start();
		new Thread(new ReadInfoThread()).start();
		new Thread(new ReadCateListThread()).start();
	}
	
	
	protected final int MSG_LOAD_CITY_LIST 			= 1;
	protected final int MSG_LOAD_HISTORY_STORED 	= 2;
	protected final int MSG_LOAD_ALLCATEGORY_LIST 	= 3;
	
	
	Handler myHandler = new Handler() {
		private int record1 = 0;//flag city list
		private int record2 = 0;//flag history/stored 
		private int record3 = 0;//flag for allcate list


		
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LOAD_CITY_LIST:
				record1 = 1;
				break;
			case MSG_LOAD_HISTORY_STORED:
				record2 = 1;
				break;
			case MSG_LOAD_ALLCATEGORY_LIST:
				record3 = 1;
                //todo load first category config 这块有问题
				break;
			default:
				break;
			}
			
			if(1 == record1 && 1 == record2 && 1 == record3){
				isJobDone = true;
				QuanleimuApplication.getImageLoader();// = new LazyImageLoader();
				jobListener.onJobDone();
			}
		}
	};
	
	public boolean isJobDone()
	{
		return isJobDone;
	}

	class ReadCateListThread implements Runnable {

		@Override
		public void run() {
			Pair<Long, String> pair = Util.loadJsonAndTimestampFromLocate(parentActivity, "saveFirstStepCate");
			
			if (pair.second == null || pair.second.length() == 0){
				pair = Util.loadDataAndTimestampFromAssets(parentActivity, "cateJson.txt");
			}
			
			String json = pair.second;
			if (json != null && json.length() > 0) {
				AllCates allCates = JsonUtil.getAllCatesFromJson(Communication.decodeUnicode(json));
				QuanleimuApplication.getApplication().setListFirst(allCates.getChildren());
			}
			
			myHandler.sendEmptyMessage(MSG_LOAD_ALLCATEGORY_LIST);
		}
	}
	
	class ReadCityListThread implements Runnable {
		
		@Override
		public void run() {
			CityList cityList = new CityList();
			// 1. load from locate.
			Pair<Long, String> pair = Util.loadJsonAndTimestampFromLocate(parentActivity, "cityjson");
			
			// 2. load from asset
			if (pair.second == null || pair.second.length() == 0)
			{	
				pair = Util.loadDataAndTimestampFromAssets(parentActivity, "cityjson.txt");
			}
			
			if (pair.second == null || pair.second.length() == 0) {
				cityList = null;
			} else {
				cityList = JsonUtil.parseCityListFromJson((pair.second));
				QuanleimuApplication.getApplication().updateCityList(cityList);
			}
			myHandler.sendEmptyMessage(MSG_LOAD_CITY_LIST);
		}
	}

	public String[] listRemark = new String[]{};//new ArrayList<String>();

	class ReadInfoThread implements Runnable {

		@SuppressWarnings("unchecked")
		@Override
		public void run() { 
			// 获取搜索记录
			String[] objRemark = (String[]) Util.loadDataFromLocate(parentActivity, "listRemark", String[].class);
			QuanleimuApplication.getApplication().updateRemark(objRemark);

			GoodsDetail[] objStore = (GoodsDetail[]) Util.loadDataFromLocate(parentActivity, "listMyStore", GoodsDetail[].class);
			QuanleimuApplication.getApplication().updateFav(objStore);
			
			byte[] personalMark = Util.loadData(parentActivity, "personMark");//.loadDataFromLocate(parentActivity, "personMark");
			if(personalMark != null){
				QuanleimuApplication.getApplication().setPersonMark(new String(personalMark));
			}
			myHandler.sendEmptyMessage(MSG_LOAD_HISTORY_STORED);
		}

	}
}
