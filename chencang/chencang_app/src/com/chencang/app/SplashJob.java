//liuchong@baixing.com
package com.chencang.app;

import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.util.Pair;

import com.baixing.activity.BaseActivity;
import com.baixing.data.CityAndCategorySelector;
import com.baixing.data.GlobalDataManager;
import com.baixing.entity.CityDetail;
import com.baixing.util.LocationService;
import com.baixing.util.MobileConfig;
import com.baixing.util.PerformEvent.Event;
import com.baixing.util.PerformanceTracker;
import com.baixing.util.Util;
import com.baixing.util.ViewUtil;

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
		PerformanceTracker.stamp(Event.E_DoSplash_Begin);//, PerformanceTracker.getFileName(), PerformanceTracker.getLineNumber(), System.currentTimeMillis());
		if (isJobStarted || isJobDone)
		{
			return;  //Avoid deprecate start.
		}
		
		isJobStarted = true;
		
		LocationService.getInstance().start(parentActivity, GlobalDataManager.getInstance().getLocationManager());

		try {
			if (parentActivity.checkConnection() == false) {
				ViewUtil.showToast(parentActivity, "网络连接异常", false);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		new Thread(new LoadConfigTask()).start();
		PerformanceTracker.stamp(Event.E_Call_LoadConfigTask);//, PerformanceTracker.getFileName(), PerformanceTracker.getLineNumber(), System.currentTimeMillis());
	}
	
	
	protected final int MSG_LOAD_CITY_LIST 			= 1;
//	protected final int MSG_LOAD_HISTORY_STORED 	= 2;
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
//			case MSG_LOAD_HISTORY_STORED:
//				record2 = 1;
//				break;
			case MSG_LOAD_ALLCATEGORY_LIST:
				record3 = 1;
                //todo load first category config 这块有问题
				break;
			default:
				break;
			}
			
			if(1 == record1 /*&& 1 == record2*/ && 1 == record3){
				isJobDone = true;
				PerformanceTracker.stamp(Event.E_End_Init_Data);//, PerformanceTracker.getFileName(), PerformanceTracker.getLineNumber(), System.currentTimeMillis());E_Begin_ReadCategory
				PerformanceTracker.stamp(Event.E_Init_Image_Mgr);
				GlobalDataManager.getInstance().getImageManager();
				PerformanceTracker.stamp(Event.E_Init_Image_Mgr_Done);
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
			PerformanceTracker.stamp(Event.E_Begin_ReadCategory);//, PerformanceTracker.getFileName(), PerformanceTracker.getLineNumber(), System.currentTimeMillis());
			GlobalDataManager.getInstance().loadCategorySync();
			myHandler.sendEmptyMessage(MSG_LOAD_ALLCATEGORY_LIST);
			PerformanceTracker.stamp(Event.E_End_ReadCategory);//, PerformanceTracker.getFileName(), PerformanceTracker.getLineNumber(), System.currentTimeMillis());
			Pair<String, String> category = CityAndCategorySelector.getInstance().parseCategory();
			if(category != null){
				Log.e("ABC", "reset cat to " + category.toString());
				GlobalDataManager.getInstance().setCategoryEnglishName(category.first);
				GlobalDataManager.getInstance().setCategoryName(category.second);
			}
			
			String lastCategory = (String)Util.loadDataFromLocate(GlobalDataManager.getInstance().getApplicationContext(), "lastCategory", String.class);
			if(lastCategory != null && lastCategory.length() > 0){
				String[] cates = lastCategory.split(",");
				if(cates != null && cates.length == 2){
					Log.e("ABC", "reset cat to last category" + cates[0] + "," + cates[1]);
					GlobalDataManager.getInstance().setCategoryEnglishName(cates[0]);
					GlobalDataManager.getInstance().setCategoryName(cates[1]);											
				}
			}
		}
	}
	
	class LoadConfigTask implements Runnable {

		public void run() {
			try
			{
				PerformanceTracker.stamp(Event.E_SyncMobileConfig_Begin);//, PerformanceTracker.getFileName(), PerformanceTracker.getLineNumber(), System.currentTimeMillis());
				MobileConfig.getInstance().syncMobileConfig();
			}
			catch(Throwable t)
			{
				
			}
			finally
			{
				PerformanceTracker.stamp(Event.E_Begin_Init_Data);//, PerformanceTracker.getFileName(), PerformanceTracker.getLineNumber(), System.currentTimeMillis());
				new Thread(new ReadCityListThread()).start();
//				new Thread(new ReadInfoThread()).start();
				new Thread(new ReadCateListThread()).start();
			}
		}
		
	}
	
	class ReadCityListThread implements Runnable {
		
		@Override
		public void run() {
			PerformanceTracker.stamp(Event.E_Begin_ReadCity);//, PerformanceTracker.getFileName(), PerformanceTracker.getLineNumber(), System.currentTimeMillis());
			GlobalDataManager.getInstance().loadCitySync();

			String pt = Util.getConfigName("city");
			if(pt != null && !pt.equals("all")){
				List<CityDetail> cities = GlobalDataManager.getInstance().getListCityDetails();
				for(int i = 0; i < cities.size(); ++ i){
					if(cities.get(i).englishName.equals(pt)){
						GlobalDataManager.getInstance().setCityEnglishName(pt);
						GlobalDataManager.getInstance().setCityName(cities.get(i).name);
						break;
					}
				}
			}
			
			myHandler.sendEmptyMessage(MSG_LOAD_CITY_LIST);
			PerformanceTracker.stamp(Event.E_End_ReadCity);//, PerformanceTracker.getFileName(), PerformanceTracker.getLineNumber(), System.currentTimeMillis());			
		}
	}

	public String[] listRemark = new String[]{};//new ArrayList<String>();

//	class ReadInfoThread implements Runnable {
//
//		@SuppressWarnings("unchecked")
//		@Override
//		public void run() { 
//			PerformanceTracker.stamp(Event.E_Begin_ReadPersonalInfo);//, PerformanceTracker.getFileName(), PerformanceTracker.getLineNumber(), System.currentTimeMillis());
//			GlobalDataManager.getInstance().loadPersonalSync();
//			myHandler.sendEmptyMessage(MSG_LOAD_HISTORY_STORED);
//			PerformanceTracker.stamp(Event.E_End_ReadPersonalInfo);//, PerformanceTracker.getFileName(), PerformanceTracker.getLineNumber(), System.currentTimeMillis());
//		}
//
//	}
}
